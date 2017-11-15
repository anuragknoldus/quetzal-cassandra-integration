package com.knoldus.service

import com.google.inject.Inject
import com.knoldus.model.{CassandraCluster, PredicateInfo, Triple}

@Inject
class TripleOperations()(
  cassandraCluster: CassandraCluster,
  predicateHashing: PredicateHashing,
  directPredicateHashing: DirectPredicateHashing) {

  def storeTriple(triple: Triple): Boolean = {

    predicateHashing.getPredicateDetails(triple.predicate) match {
      case Some(columnLocation) =>
        val entityInfo = directPredicateHashing.getUpdateInfo(columnLocation, triple)
        if (entityInfo.isAvailable && entityInfo.id.isEmpty) {
          directPredicateHashing.updateTripleToDPH(triple, entityInfo)
        } else if (entityInfo.isAvailable && entityInfo.id.isDefined) {
          directPredicateHashing.updateViaId(triple, entityInfo)
        } else {
          directPredicateHashing.storeTripleToDPH(triple, "Chemistry", entityInfo)
        }
      case None => storeFreshTriple(triple)
    }
  }

  def storeFreshTriple(triple: Triple): Boolean = {

    val (minColumnValue, maxColumnValue) = predicateHashing.getHashValue(triple.predicate)
    val entityInfo = directPredicateHashing.getEntityInfo(minColumnValue, maxColumnValue, triple)
    val predicateInfo = PredicateInfo(triple.predicate, entityInfo.location.toString)
    if (predicateHashing.storePredicate(predicateInfo)) {
      if (entityInfo.isAvailable) {
        val result = directPredicateHashing.updateTripleToDPH(triple, entityInfo)
        result
      } else {
        directPredicateHashing.storeTripleToDPH(triple, "Chemistry", entityInfo)
      }
    } else {
      throw new Exception("Unable to Store triple in predicate_mapping Table")
    }
  }
}

object TripleOperations {
  def main(args: Array[String]): Unit = {
    val cassandraCluster = new CassandraCluster
    val hashing = new Hashing
    val predicateHashing = new PredicateHashing(cassandraCluster, hashing)
    val directPredicateHashing = new DirectPredicateHashing(cassandraCluster)
    val tripleOperations = new TripleOperations()(cassandraCluster, predicateHashing, directPredicateHashing)
    tripleOperations.storeTriple(Triple("Entity2", "Predicate1", "ValueNext"))
  }
}