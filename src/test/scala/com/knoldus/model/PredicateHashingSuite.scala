package com.knoldus
package model

import com.knoldus.cassandra.CassandraDatabaseProvider
import com.knoldus.helper.QueryHelper
import com.knoldus.service.{Hashing, PredicateHashing}
import com.outworkers.phantom.dsl._
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.FlatSpec

class PredicateHashingSuite extends FlatSpec with CassandraDatabaseProvider{

   val beforeAll: Seq[ResultSet] =  {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra("test-cassandra.yaml", 1000000L)
    database.create()
  }

  val predicateInfo = PredicateInfo("predicate", "location")
  val queryHelper = new QueryHelper
  val cluster = new CassandraCluster(queryHelper)
  val hashing = new Hashing
  val predicateHashing = new PredicateHashing(cluster, hashing, queryHelper)

  it should "get results" in {
    val isStored = predicateHashing.storePredicate(predicateInfo)
    val result = predicateHashing.getPredicateDetails(predicateInfo.predicate)
    val expectedOutput = Some(predicateInfo.location)
    assert(isStored)
    assert(result == expectedOutput)
  }

}
