package com.knoldus
package model

import java.util.UUID

import com.datastax.driver.core._

class CassandraCluster {

  def createCluster(): Cluster = {
    try {
      Cluster.builder().addContactPoint(databaseEndPoint).build()
    } catch {
      case exception: Exception => throw exception
    }
  }

  val session: Session = createCluster().connect(databaseName)

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
}