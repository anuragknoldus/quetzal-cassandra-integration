package com.knoldus
package integration

import com.knoldus.helper.QueryHelper
import com.knoldus.model.CassandraCluster
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

trait CassandraDatabaseCluster extends FlatSpec
  with BeforeAndAfterAll
  with ScalaFutures
  with Matchers
  with OptionValues {

  val queryHelper = new QueryHelper

  override def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra("test-cassandra.yaml", 1000000L)
    val cluster = new CassandraCluster(queryHelper)
    cluster.createDatabase()
    cluster.createPredicateSchema()
    cluster.createDPHTable()
  }

}
