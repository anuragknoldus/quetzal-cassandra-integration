package com.knoldus
package service

import java.util.UUID
import javax.inject.Inject
import com.datastax.driver.core.{ResultSet, Row, Session}
import com.knoldus.helper.QueryHelper
import com.knoldus.model.{CassandraCluster, EntityInfo, Triple}
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}

import scala.annotation.tailrec

@Inject
class DirectPredicateHashing(cassandraCluster: CassandraCluster,
                             queryHelper: QueryHelper) {

  lazy val session: Session = cassandraCluster.createCluster().connect(databaseName)

  /**
    * Check availability for the Entity
    *
    * @param minColumnValue Minimum Hash Value
    * @param maxColumnValue Maximum Hash Value
    * @param triple         Triple values
    * @return Entity Info
    */
  def getEntityInfo(minColumnValue: Int,
                    maxColumnValue: Int,
                    triple: Triple): EntityInfo = {

    val selectSQL = queryHelper.fetchValue(minColumnValue, maxColumnValue, triple.entry)
    val row = session.execute(selectSQL).one()
    Option(row) match {
      case Some(result) => getEntityInfoFromRow(minColumnValue, maxColumnValue, result, None)
      case None => EntityInfo(isAvailable = false, minColumnValue)
    }
  }

  /**
    * Fetch the Entity Info from Row
    *
    * @param minColumnValue Minimum Hash Value
    * @param maxColumnValue Maximum Hash Value
    * @param result         Row
    * @return Entity Info
    */
  private def getEntityInfoFromRow(minColumnValue: Int,
                                   maxColumnValue: Int,
                                   result: Row,
                                   fieldRecords: Option[Map[UUID, Boolean]]): EntityInfo = {

    if (fieldRecords.isDefined) {
      val id = (fieldRecords.get filter { (fieldRecord) => fieldRecord._2 }).keys.toList.headOption
      val spill = result.getInt(Spill)
      if (id.isDefined) {
        EntityInfo(isAvailable = true, minColumnValue, spill = spill, id = id)
      } else {
        getEntityInfoHelper(minColumnValue: Int, maxColumnValue: Int, result: Row)
      }
    } else {
      getEntityInfoHelper(minColumnValue, maxColumnValue, result)
    }
  }

  /**
    * Help to find the Info regarding Entity
    *
    * @param minColumnValue Min Column Value
    * @param maxColumnValue Max Column Value
    * @param result         row of the result
    * @return
    */
  private def getEntityInfoHelper(minColumnValue: Int,
                                  maxColumnValue: Int,
                                  result: Row): EntityInfo = {

    val spill = result.getInt(Spill)
    val firstProp = Option(result.getString(Prop + minColumnValue)).getOrElse(EmptyString)
    val secondProp = Option(result.getString(Prop + maxColumnValue)).getOrElse(EmptyString)

    (firstProp, secondProp) match {
      case (tupleValueOne, _) if tupleValueOne.isEmpty => EntityInfo(isAvailable = true, minColumnValue, spill = spill)
      case (_, tupleValueTwo) if tupleValueTwo.isEmpty => EntityInfo(isAvailable = true, maxColumnValue, spill = spill)
      case _ => EntityInfo(isAvailable = true, minColumnValue, isSpill = true, spill + ConstantValues.One.id)
    }
  }

  /**
    * Insert Triple into DPH Table
    *
    * @param tripleInfo Triple Values
    * @param domain     Domain Name
    * @param entityInfo Entity Info
    * @return
    */
  def storeTripleToDPH(tripleInfo: Triple,
                       domain: String,
                       entityInfo: EntityInfo): Boolean = {

    val id = UUID.randomUUID
    val query = queryHelper.insertValue(entityInfo, tripleInfo, id, domain)
    try {
      Option(session.execute(query)).isDefined
    } catch {
      case _: Exception => false
    }
  }

  /**
    * Method is used for getting the Info regarding the Entity
    *
    * @param value  Column Value
    * @param triple Triple Value
    * @return Entity Info
    */
  def getUpdateInfo(value: String,
                    triple: Triple): EntityInfo = {

    val selectSQL = queryHelper.fetchIDWithSpillAndProp(value, triple.entry)
    val fieldRecords = getIdForUpdate(session.execute(selectSQL), value: String, Map())
    val row = session.execute(selectSQL).one
    Option(row) match {
      case Some(result) => getEntityInfoFromRow(value.toInt, value.toInt, result, Some(fieldRecords))
      case None => EntityInfo(isAvailable = false, value.toInt)
    }
  }

  /**
    * Method is used for update the Triple into DPH Table
    *
    * @param triple     Triple Value
    * @param entityInfo Entity Information
    * @return
    */
  def updateTripleToDPH(triple: Triple,
                        entityInfo: EntityInfo): Boolean = {

    val selectSQL = queryHelper.fetchIDWithDomain(triple.entry)
    val resultSet = session.execute(selectSQL)
    if (entityInfo.spill == ConstantValues.Zero.id | !entityInfo.isSpill) {
      val row = resultSet.one
      val updateQuery = queryHelper.updateQuery(entityInfo.location, triple.value, triple.predicate, row)
      Option(session.execute(updateQuery)).isDefined
    } else {
      val listOfUUID = getIdFromResultSet(session.execute(selectSQL), Nil)
      val row = resultSet.one
      val record = updateSpillInDB(listOfUUID, entityInfo.spill, row.getString(Domain))
      if (record forall (x => x)) {
        storeTripleToDPH(triple, row.getString(Domain), entityInfo)
      } else {
        throw new Exception("Unable to update triple in DPH Table")
      }
    }
  }

  /**
    * Update DPH Table for the Particular ID
    *
    * @param triple     Triple Value
    * @param entityInfo Entity Information
    * @return
    */
  def updateViaId(triple: Triple,
                  entityInfo: EntityInfo): Boolean = {

    val selectSQL = queryHelper.fetchDomain(triple.entry)
    val row = session.execute(selectSQL).one()
    val updateQuery = queryHelper.updateQueryWithParticularID(entityInfo.location, triple.value, triple.predicate, row, entityInfo.id.get)
    Option(session.execute(updateQuery)).isDefined
  }

  /**
    * Update Spill Value in Dph Table
    *
    * @param listOfUUID Default or Empty List for first time
    * @param spill      Spill Value
    * @param domain     Domain Value
    * @return
    */
  def updateSpillInDB(listOfUUID: List[UUID],
                      spill: Int,
                      domain: String): List[Boolean] = {

    listOfUUID map { id =>
      val updateQuery = queryHelper.updateMultipleRow(spill, domain, id)
      Option(session.execute(updateQuery)).isDefined
    }
  }


  /**
    * Method is used for Fetching the ID which we can update when multiple rows find for single subject
    *
    * @param resultSet           Result from the Database
    * @param value               Column value
    * @param mapOfUUIDAndBoolean Default or Empty Map for first time
    * @return
    */
  @tailrec
  private def getIdForUpdate(resultSet: ResultSet,
                             value: String,
                             mapOfUUIDAndBoolean: Map[UUID, Boolean]): Map[UUID, Boolean] = {

    if (!resultSet.isExhausted) {
      val row = resultSet.one()
      val id = row.getUUID(Id)
      val prop = Option(row.getString(s"""${Prop + value}""")).getOrElse(EmptyString)
      if (prop.isEmpty) {
        mapOfUUIDAndBoolean ++ Map(id -> true)
        getIdForUpdate(resultSet, value, mapOfUUIDAndBoolean ++ Map(id -> true))
      } else {
        getIdForUpdate(resultSet, value, mapOfUUIDAndBoolean ++ Map(id -> false))
      }
    } else {
      mapOfUUIDAndBoolean
    }
  }

  /**
    * Get theList Of Id
    *
    * @param resultSet Result from the Database
    * @param idList    Default or Empty List for first time
    * @return
    */
  @tailrec
  private def getIdFromResultSet(resultSet: ResultSet,
                                 idList: List[UUID]): List[UUID] = {
    if (!resultSet.isExhausted) {
      val id = resultSet.one().getUUID(Id)
      getIdFromResultSet(resultSet, id :: idList)
    } else {
      idList
    }
  }

  def registerDPHTable(spark: SparkSession): Unit = {
    spark.read.format("org.apache.spark.sql.cassandra").option("keyspace", databaseName)
      .option("table", DPH).load().createOrReplaceTempView(DPH)
  }
}
