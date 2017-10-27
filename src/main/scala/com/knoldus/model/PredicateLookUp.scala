package com.knoldus.model

import com.outworkers.phantom.dsl._

abstract class PredicateLookUp extends Table[PredicateLookUp, PredicateStore] {

  object predicate extends StringColumn with PartitionKey

  object location extends StringColumn

  def createTable: Seq[ResultSet] = CassandraDatabase.create()

}
