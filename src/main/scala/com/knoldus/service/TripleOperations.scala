package com.knoldus.service

import com.google.inject.Inject
import com.knoldus.model.{CassandraCluster, EntityInfo, Triple}

@Inject
class TripleOperations()(
  cassandraCluster: CassandraCluster,
  predicateHashing: PredicateHashing,
  directPredicateHashing: DirectPredicateHashing) {

  def upsertDPH(tripleInfo: Triple, domain: String, entityInfo: EntityInfo) = {

    try {
      entityInfo match {
        case info if !info.isAvailable =>
          directPredicateHashing.storeTripleToDPH(tripleInfo, domain, entityInfo)
      }
    } catch {
      case exception: Exception =>
        throw exception
        false
    }
  }
}
