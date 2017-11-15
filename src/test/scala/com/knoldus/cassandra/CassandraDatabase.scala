package com.knoldus
package cassandra

import com.knoldus.model.Predicate
import com.outworkers.phantom.dsl._

class CassandraDatabase(
                         override val connector: CassandraConnection
                       ) extends Database[CassandraDatabase](connector) {
  object predicates extends Predicate with Connector
}

object CassandraDatabase extends CassandraDatabase(Connector.default)

trait DbProvider extends DatabaseProvider[CassandraDatabase] {
  override val database = CassandraDatabase
}
