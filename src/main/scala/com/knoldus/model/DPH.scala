package com.knoldus.model

import com.outworkers.phantom.dsl._

case class DPH(
                subject: String,
                spill: Int = 0,
                pred1: String,
                val1: String,
                pred2: String,
                val2: String,
                pred3: String,
                val3: String,
                pred4: String,
                val4: String,
                pred5: String,
                val5: String
              )

object Defaults {
  val Connector = ContactPoint.local.keySpace(KeySpace("my_keyspace")
    .ifNotExists().`with`(replication eqs SimpleStrategy.replication_factor(2)))
}

class CassandraDatabase(override val connector: CassandraConnection) extends Database[CassandraDatabase](connector) {

  object dph extends DPHData with Connector

}

