package com.knoldus
package service

import java.util.UUID
import javax.inject.Inject

import com.datastax.driver.core.{ResultSet, Row, Session}
import com.knoldus.model.{CassandraCluster, EntityInfo, Triple}

@Inject
class DirectPredicateHashing(cassandraCluster: CassandraCluster) {

  lazy val session: Session = cassandraCluster.createCluster().connect(databaseName)

  /**
    * Check availability for the Entity
    *
    * @param minColumnValue Minimum Hash Value
    * @param maxColumnValue Maximum Hash Value
    * @param triple         Triple values
    * @return Entity Info
    */
  def getEntityInfo(minColumnValue: Int, maxColumnValue: Int, triple: Triple): EntityInfo = {

    val selectSQL =
      s"""SELECT $Spill, ${Prop + minColumnValue}, ${Prop + maxColumnValue}
         FROM $DPH WHERE $Entity = '${triple.entry}' $AllowFiltering"""


    val row = session.execute(selectSQL).one()
    Option(row) match {
      case Some(result) =>
        getEntityInfoFromRow(minColumnValue, maxColumnValue, result, None)
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
  private def getEntityInfoFromRow(minColumnValue: Int, maxColumnValue: Int, result: Row, fieldRecords: Option[Map[UUID, Boolean]]) = {
    if (fieldRecords.isDefined) {
      val id = (fieldRecords.get filter { (t) => t._2 }).keys.toList.headOption
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

  private def getEntityInfoHelper(minColumnValue: Int, maxColumnValue: Int, result: Row) = {
    val spill = result.getInt(Spill)
    val firstProp = Option(result.getString(Prop + minColumnValue)).getOrElse("")
    val secondProp = Option(result.getString(Prop + maxColumnValue)).getOrElse("")
    if (firstProp.isEmpty) {
      EntityInfo(isAvailable = true, minColumnValue, spill = spill)
    } else if (secondProp.isEmpty) {
      EntityInfo(isAvailable = true, maxColumnValue, spill = spill)
    } else {
      EntityInfo(isAvailable = true, minColumnValue, isSpill = true, spill + 1)
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
  def storeTripleToDPH(tripleInfo: Triple, domain: String, entityInfo: EntityInfo): Boolean = {
    val id = UUID.randomUUID
    val query =
      s"""Insert into $databaseName.$DPH($Id, $Entity, $Spill, $Domain, ${Prop + entityInfo.location},
         | ${Val + entityInfo.location}) values ($id, '${tripleInfo.entry}', ${entityInfo.spill}, '$domain',
         |  '${tripleInfo.predicate}', '${tripleInfo.value}')""".stripMargin
    try {
      Option(session.execute(query)).isDefined
    } catch {
      case _: Exception => false
    }
  }

  def getUpdateInfo(value: String, triple: Triple): EntityInfo = {
    val selectSQL = s"""SELECT id, $Spill, ${Prop + value} FROM $DPH WHERE $Entity = '${triple.entry}' $AllowFiltering"""
    val fieldRecords = getIdForUpdate(session.execute(selectSQL), value: String, Map())
    val row = session.execute(selectSQL).one
    Option(row) match {
      case Some(result) => getEntityInfoFromRow(value.toInt, value.toInt, result, Some(fieldRecords))
      case None => EntityInfo(isAvailable = false, value.toInt)
    }
  }

  def updateTripleToDPH(triple: Triple, entityInfo: EntityInfo): Boolean = {
    val selectSQL = s"""SELECT id, $Domain FROM $DPH WHERE $Entity = '${triple.entry}' $AllowFiltering"""
    val rs = session.execute(selectSQL)
    if (entityInfo.spill == 0 | !entityInfo.isSpill) {
      val row = rs.one
      val updateQuery =
        s"""update $databaseName.$DPH set ${Val + entityInfo.location} = '${triple.value}', ${Prop + entityInfo.location} = '${triple.predicate}' where domain = '${row.getString("domain")}' and id = ${row.getUUID("id")}""".stripMargin
      Option(session.execute(updateQuery)).isDefined
    } else {
      val listOfUUID = getIdFromResultSet(session.execute(selectSQL), Nil)
      val row = rs.one
      val record = updateSpillInDB(listOfUUID, entityInfo.spill, row.getString("domain"))
      if (record forall (x => x)) {
        storeTripleToDPH(triple, row.getString("domain"), entityInfo)
      } else {
        throw new Exception("Unable to update triple in DPH Table")
      }
    }
  }

  def updateViaId(triple: Triple, entityInfo: EntityInfo): Boolean = {

    val selectSQL = s"""SELECT $Domain FROM $DPH WHERE $Entity = '${triple.entry}' $AllowFiltering"""
    val row = session.execute(selectSQL).one()
    val updateQuery =
      s"""update $databaseName.$DPH set ${Val + entityInfo.location} = '${triple.value}', ${Prop + entityInfo.location} = '${triple.predicate}' where domain = '${row.getString("domain")}' and id = ${entityInfo.id.get}""".stripMargin
    Option(session.execute(updateQuery)).isDefined
  }

  def updateSpillInDB(listOfUUID: List[UUID], spill: Int, domain: String): List[Boolean] = {
    listOfUUID map { x =>
      val updateQuery =
        s"""update $databaseName.$DPH set spill = $spill where domain = '$domain' and id = $x""".stripMargin
      Option(session.execute(updateQuery)).isDefined
    }
  }


  def getIdForUpdate(resultSet: ResultSet, value: String, map: Map[UUID, Boolean]): Map[UUID, Boolean] = {

    if (!resultSet.isExhausted) {
      val row = resultSet.one()
      val id = row.getUUID("id")
      val prop = Option(row.getString(s"""${Prop + value}""")).getOrElse("")
      if (prop.isEmpty) {
        map ++ Map(id -> true)
        getIdForUpdate(resultSet, value, map ++ Map(id -> true))
      } else {
        getIdForUpdate(resultSet, value, map ++ Map(id -> false))
      }
    } else {
      map
    }
  }

  def getIdFromResultSet(resultSet: ResultSet, list: List[UUID]): List[UUID] = {
    if (!resultSet.isExhausted) {
      val id = resultSet.one().getUUID("id")
      getIdFromResultSet(resultSet, id :: list)
    } else {
      list
    }
  }
}
