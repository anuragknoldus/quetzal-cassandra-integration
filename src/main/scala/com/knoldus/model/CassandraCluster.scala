package com.knoldus
package model

import com.datastax.driver.core._
import com.google.inject.Inject
import com.knoldus.helper.QueryHelper

@Inject
class CassandraCluster(queryHelper: QueryHelper) {

  /**
    * This method is used for creating the Cluster for Cassandra
    *
    * @return
    */
  def createCluster(): Cluster = {
    try {
      Cluster.builder().addContactPoint(databaseEndPoint).build()
    } catch {
      case exception: Exception => throw exception
    }
  }

  /**
    * Method is used for creating a database
    *
    * @return
    */
  def createDatabase(): ExecutionInfo = {

    val session = createCluster().connect()
    try {
      val createDatabase = queryHelper.CreateDatabase
      session.execute(createDatabase).getExecutionInfo
    } catch {
      case exception: Exception => throw exception
    }
  }

  lazy val session: Session = createCluster().connect(databaseName)

  /**
    * Method is used for creating Predicate Table
    *
    */
  def createPredicateSchema(): Unit = {
    val query = queryHelper.PredicateTableCreation
    session.execute(query)
  }

  /**
    * Method is used for creating the query string for DPH table
    *
    * @param dphTableSize
    * @return
    */
  private def fetchFields(dphTableSize: Int): String = {

    @scala.annotation.tailrec
    def fetchFieldsImpl(dphTableSize: Int, defaultValue: Int = ConstantValues.One.id, fields: String = EmptyString): String = {
      dphTableSize match {
        case tableSize if tableSize > ConstantValues.One.id =>
          fetchFieldsImpl(tableSize - ConstantValues.One.id, defaultValue + ConstantValues.One.id,
            fields ++ Prop + defaultValue + " " + "Text, " + Val + defaultValue + " " + "Text, ")
        case tableSize if tableSize == ConstantValues.One.id =>
          fetchFieldsImpl(tableSize - ConstantValues.One.id, defaultValue + ConstantValues.One.id,
            fields ++ Prop + defaultValue + " " + "Text, " + Val + defaultValue + " " + "Text")
        case _ => fields
      }
    }

    fetchFieldsImpl(dphTableSize)
  }

  /**
    * Method is used for creating DPH Table
    *
    * @return
    */
  def createDPHTable(): ExecutionInfo = {
    try {
      val queryString = fetchFields(dphTableSize)
      val query = queryHelper.dphTableCreation(queryString)
      val rs = session.execute(query)
      rs.getExecutionInfo
    } catch {
      case exception: Exception => throw exception
    }
  }
}
