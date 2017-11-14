package com.knoldus
package service

import java.util.UUID
import javax.inject.Inject

import com.datastax.driver.core.{Row, Session}
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
        getEntityInfoFromRow(minColumnValue, maxColumnValue, result)
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
  private def getEntityInfoFromRow(minColumnValue: Int, maxColumnValue: Int, result: Row) = {
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

}
