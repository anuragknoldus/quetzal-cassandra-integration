package com.knoldus.model

import com.outworkers.phantom.dsl._

import scala.language.reflectiveCalls

case class DPH(
                subject: String,
                spill: Int = 0,
                pred1: String = "",
                val1: String = "",
                pred2: String = "",
                val2: String = "",
                pred3: String = "",
                val3: String = "",
                pred4: String = "",
                val4: String = "",
                pred5: String = "",
                val5: String = "",
                domain: String
              )

case class PredicateStore(
                           predicate: String,
                           location: String
                         )
import scala.concurrent.{Future => ScalaFuture}
object Defaults {
  val Connector: CassandraConnection = ContactPoint.local.keySpace(KeySpace("quetzal")
    .ifNotExists().`with`(replication eqs SimpleStrategy.replication_factor(2)))
}

class CassandraDatabase(override val connector: CassandraConnection) extends Database[CassandraDatabase](connector) {

  object dph extends DPHData with Connector

  object predicate extends PredicateLookUp with Connector

  def storePredicate(predicateData: PredicateStore): ScalaFuture[ResultSet] = {
    predicate.store(predicateData).future()
  }

  def storeDPH(dphData: DPH): ScalaFuture[ResultSet] = {
    dph.store(dphData).future()
  }
}

object CassandraDatabase extends CassandraDatabase(Defaults.Connector)

trait DbProvider extends DatabaseProvider[CassandraDatabase] {
  override val database: CassandraDatabase = CassandraDatabase
}
