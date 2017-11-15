package com.knoldus
package cassandra

import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.dsl._
import com.typesafe.config.{Config, ConfigFactory}

object Connector {

  val default: CassandraConnection = ContactPoint.local.keySpace(KeySpace(databaseName)
    .ifNotExists().`with`(replication eqs SimpleStrategy.replication_factor(1)))
}
