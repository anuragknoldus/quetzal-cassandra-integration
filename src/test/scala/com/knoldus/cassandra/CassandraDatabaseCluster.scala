package com.knoldus
package cassandra

import com.knoldus.helper.QueryHelper
import com.knoldus.model.CassandraCluster
import com.outworkers.phantom.dsl._
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

trait CassandraDatabaseCluster extends FlatSpec
  with BeforeAndAfterAll
  with ScalaFutures
  with Matchers
  with OptionValues
  with CassandraDatabaseProvider {

  val queryHelper = new QueryHelper
  val cluster = new CassandraCluster(queryHelper)

  override def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra("test-cassandra.yaml", 1000000L)
    database.create()
    cluster.createPredicateSchema()
  }

  override def afterAll(): Unit = {
    database.truncate()
  }

}
