package com.knoldus.model

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

abstract class PredicateLookUp extends Table[PredicateLookUp, PredicateStore] {

  object predicate extends StringColumn with PartitionKey

  object location extends StringColumn

  def createTable: Seq[ResultSet] = CassandraDatabase.create()

  def saveToPredicateLookUp(predicateStore: PredicateStore): Future[ResultSet] = {

    insert
      .value(_.predicate, predicateStore.predicate)
      .value(_.location, predicateStore.location)
      .future()
  }
}
