package com.knoldus
package model

import java.util.UUID

import com.datastax.driver.core
import com.datastax.driver.core.{Cluster, ExecutionInfo, ResultSet}

class Cluster {

  def createCluster(): core.Cluster = {
    try {
      Cluster.builder().addContactPoint(databaseEndPoint).build()
    } catch {
      case exception: Exception => throw exception
    }
  }

  def createPredicateSchema(): Unit = {
    val session = createCluster().connect()
    val query = s"""CREATE TABLE IF NOT EXISTS $databaseName.$DirectPredicate($Predicate text PRIMARY KEY, $Location text)"""
    session.execute(query)
  }

  def createDatabase(): ExecutionInfo = {

    val session = createCluster().connect()
    try {
      val createDatabase =
        s"""CREATE KEYSPACE IF NOT EXISTS $databaseName WITH REPLICATION = { 'class' : 'SimpleStrategy'
           |, 'replication_factor' : 3 };""".stripMargin
      session.execute(createDatabase).getExecutionInfo
    } catch {
      case exception: Exception => throw exception
    } finally {
      session.close()
    }
  }

  def createPredicateTable(): ExecutionInfo = {
    val session = createCluster().connect()
    try {
      val query = s"""CREATE TABLE IF NOT EXISTS $databaseName.$DirectPredicate($Predicate text PRIMARY KEY, $Location text)"""
      val rs = session.execute(query)
      rs.getExecutionInfo
    } catch {
      case exception: Exception => throw exception
    } finally {
      session.close()
    }
  }


  def fetchFields(size: Int): String = {
    @scala.annotation.tailrec
    def fetchFieldsImpl(size: Int, defaultValue: Int = 1, fields: String = ""): String = {
      size match {
        case size if size > 1 => fetchFieldsImpl(size - 1, defaultValue + 1, fields ++ Prop + defaultValue + " " + "Text, " + Val + defaultValue + " " + "Text, ")
        case size if size == 1 => fetchFieldsImpl(size - 1, defaultValue + 1, fields ++ Prop + defaultValue + " " + "Text, " + Val + defaultValue + " " + "Text")
        case _ => fields
      }
    }

    fetchFieldsImpl(size)
  }

  def createDPHTable(): ExecutionInfo = {
    val session = createCluster().connect()
    try {
      val queryString = fetchFields(dphTableSize)
      val query = s"""CREATE TABLE IF NOT EXISTS $databaseName.$DPH($Id UUID, $Entity text, $Spill int, $Domain text, $queryString, PRIMARY KEY(id, domain))"""
      val rs = session.execute(query)
      rs.getExecutionInfo
    } catch {
      case exception: Exception => throw exception
    } finally {
      session.close()
    }
  }

  def insertIntoDPH(id: UUID, entity: String, domain: String, col_number: Int, predicate_value: String, object_value: String): ExecutionInfo = {
    val session = createCluster().connect()
    try {
      val query = s"""Insert into $databaseName.$DPH($Id, $Entity, $Spill, $Domain, ${Prop + col_number}, ${Val + col_number}) values ($id, '$entity', 0, '$domain', '$predicate_value', '$object_value')"""
      val resultSet = session.execute(query)
      resultSet.getExecutionInfo
    } catch {
      case exception: Exception => throw exception
    } finally {
      session.close()
    }
  }

  def upsertDPH(id: UUID, entity: String, domain: String, col_number: Int, predicate_value: String, object_value: String): ExecutionInfo = {
    val session = createCluster().connect()
    try {
      val query = s"""Select id, entity from $databaseName.$DPH where entity = '$entity' ALLOW FILTERING"""
      val resultSet = session.execute(query)
      if (Option(resultSet.one()).isDefined) {
        updateDPH(id, entity, domain, col_number, predicate_value, object_value, 1)
      } else {
        insertIntoDPH(id, entity, domain, col_number, predicate_value, object_value)
      }
      resultSet.getExecutionInfo
    } catch {
      case exception: Exception => throw exception
    } finally {
      session.close()
    }
  }

  def updateDPH(id: UUID, entity: String, domain: String, col_number: Int, predicate_value: String, object_value: String, spill: Int): ResultSet = {
    val session = createCluster().connect()
    try {
      val propQuery =
        s"""Select id, prop$col_number, val$col_number from $databaseName.$DPH where prop$col_number =
           |'$predicate_value' and val$col_number = '$object_value' ALLOW FILTERING""".stripMargin
      val propRow = session.execute(propQuery).one
      val query = if (Option(propRow).isDefined) {
        val persistedId = propRow.getUUID("id")
        val updateQuery = s"""update $databaseName.$DPH set spill = $spill where domain = '$domain' and id = $persistedId"""
        session.execute(updateQuery)
        s"""insert into $databaseName.$DPH($Id, $Entity, $Spill, $Domain) values ($id, '$entity', $spill, '$domain')"""
      } else {
        val id = propRow.getUUID("id")
        s"""update $databaseName.$DPH set prop$col_number = '$predicate_value' and val$col_number = '$object_value' where domain = '$domain' and id = $id"""
      }
      session.execute(query)
    } catch {
      case exception: Exception => throw exception
    } finally {
      session.close()
    }
  }

}


object Clusters {
  def main(args: Array[String]): Unit = {
    val cluster = new Cluster
    cluster.createDatabase()
    cluster.createPredicateTable()
    val uuid = java.util.UUID.randomUUID
    cluster.upsertDPH(uuid, "Sodium", "Chemistry", 1, "Property1", "Value1")
  }

}

