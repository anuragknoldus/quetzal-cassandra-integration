package com.knoldus.model

import com.outworkers.phantom.Table
import com.outworkers.phantom.keys.PartitionKey
import com.outworkers.phantom.dsl._

abstract class Predicate extends Table[Predicate, PredicateInfo]{

  object predicate extends StringColumn with PartitionKey
  object location extends StringColumn
}
