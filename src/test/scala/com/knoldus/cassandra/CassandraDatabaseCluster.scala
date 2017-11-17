package com.knoldus
package cassandra

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

  lazy val queryHelper = new QueryHelper
  var cluster: CassandraCluster = _

  override def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra("test-cassandra.yaml", 1000000L)
    cluster = new CassandraCluster(queryHelper)
    cluster.createDatabase()
    cluster.createPredicateSchema()
  }

}
