package com.knoldus
package model

import com.datastax.driver.core
import com.datastax.driver.core.Cluster

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
    val createDatabase =
      s"""CREATE KEYSPACE IF NOT EXISTS $databaseName WITH REPLICATION = { 'class' : 'SimpleStrategy'
         |, 'replication_factor' : 3 };""".stripMargin
    session.execute(createDatabase)
    val query = s"""CREATE TABLE IF NOT EXISTS $databaseName.$DirectPredicate($Predicate text PRIMARY KEY, $Location text)"""
    session.execute(query)
    session.close()
  }

}
