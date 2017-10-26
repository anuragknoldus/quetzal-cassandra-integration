package com.knoldus.model

import com.outworkers.phantom.dsl._

import scala.language.reflectiveCalls

case class DPH(
                subject: String,
                spill: Int = 0,
                pred1: String = null,
                val1: String = null,
                pred2: String = null,
                val2: String = null,
                pred3: String = null,
                val3: String = null,
                pred4: String = null,
                val4: String = null,
                pred5: String = null,
                val5: String = null,
                domain: String = null
              )

case class PredicateStore(
                           predicate: String,
                           location: String
                         )

object Defaults {
  val Connector: CassandraConnection = ContactPoint.local.keySpace(KeySpace("quetzal")
    .ifNotExists().`with`(replication eqs SimpleStrategy.replication_factor(2)))
}

class CassandraDatabase(override val connector: CassandraConnection) extends Database[CassandraDatabase](connector) {

  object dph extends DPHData with Connector

  object predicate extends PredicateLookUp with Connector

}

object CassandraDatabase extends CassandraDatabase(Defaults.Connector)

trait DbProvider extends DatabaseProvider[CassandraDatabase] {
  override val database: CassandraDatabase = CassandraDatabase
}
