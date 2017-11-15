package com.knoldus
package model

import com.knoldus.cassandra.CassandraDatabaseCluster
import com.knoldus.service.{Hashing, PredicateHashing}

class PredicateHashingSuite extends CassandraDatabaseCluster {

  val predicateInfo = PredicateInfo("predicate", "location")
  override val cluster = new CassandraCluster
  val hashing = new Hashing
  val predicateHashing = new PredicateHashing()(cluster, hashing)

  it should "get results" in {
    val isStored = predicateHashing.storePredicate(predicateInfo)
    val result = predicateHashing.getPredicateDetails(predicateInfo.predicate)
    val expectedOutput = Some(predicateInfo.location)
    assert(isStored)
    assert(result == expectedOutput)
  }
}
