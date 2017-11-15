package com.knoldus
package cassandra

import com.outworkers.phantom.dsl.DatabaseProvider
import com.outworkers.phantom.dsl._

trait CassandraDatabaseProvider extends DatabaseProvider[CassandraDatabase] {
  override val database = CassandraDatabase
}