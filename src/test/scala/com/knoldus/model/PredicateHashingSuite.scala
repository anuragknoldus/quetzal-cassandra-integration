package com.knoldus
package model

import com.knoldus.cassandra.CassandraDatabaseCluster
import com.knoldus.service.{Hashing, PredicateHashing}

class PredicateHashingSuite extends CassandraDatabaseCluster {

  lazy val predicateInfo = PredicateInfo("predicate", "location")
  lazy val hashing = new Hashing
  lazy val predicateHashing = new PredicateHashing(cluster, hashing, queryHelper)

  it should "get results" in {
    cluster.createDatabase()
    val isStored = predicateHashing.storePredicate(predicateInfo)
    val result = predicateHashing.getPredicateDetails(predicateInfo.predicate)
    val expectedOutput = Some(predicateInfo.location)
    assert(isStored)
    assert(result == expectedOutput)
  }

}
