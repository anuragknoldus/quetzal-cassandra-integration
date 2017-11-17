package com.knoldus.integration

import com.knoldus.model._
import com.knoldus.service.{DirectPredicateHashing, Hashing, PredicateHashing, TripleOperations}

class CassandraOperationSuite  extends CassandraDatabaseCluster {

  val predicateInfo = PredicateInfo("predicate", "2")
  val hashing = new Hashing
  lazy val cluster = new CassandraCluster(queryHelper)
  lazy val predicateHashing = new PredicateHashing(cluster, hashing, queryHelper)
  lazy val directPredicateHashing = new DirectPredicateHashing(cluster, queryHelper)
  lazy val tripleOperations =  new TripleOperations()(cluster, predicateHashing, directPredicateHashing)
  val triple = Triple("Entity", predicateInfo.predicate, "Object")
  val Domain = "Chemistry"

  it should "get results" in {
    val isStored = predicateHashing.storePredicate(predicateInfo)
    val result = predicateHashing.getPredicateDetails(predicateInfo.predicate)
    val expectedOutput = Some(predicateInfo.location)
    assert(isStored)
    assert(result == expectedOutput)
  }

  it should "store Triple values" in {
    val entityInfo = directPredicateHashing.getEntityInfo(3, 3, triple)
    val response = directPredicateHashing.storeTripleToDPH(triple,Domain,entityInfo)
    assert(response)
  }

  it should "store Triple in Cassandra" in {

    val response = tripleOperations.storeTriple(triple)
    assert(response)
  }

  it should "store fresh Triple in Cassandra" in {

    val freshTriple = Triple("TestSubject", "TestPredicate", "TestObject")
    val response = tripleOperations.storeFreshTriple(freshTriple)
    assert(response)
  }

  it should "store Triple in Cassandra with different Predicate and value" in {

    val freshTriple = Triple("TestSubject", "TestPredicate1", "TestObject1")
    val response = tripleOperations.storeFreshTriple(freshTriple)
    assert(response)
  }

  it should "fetch Object from Cassandra" in {
    val response = tripleOperations.fetchObject(triple.entry, triple.predicate)
    val expectedOutput = Some(triple)
    assert(response == expectedOutput)
  }

}
