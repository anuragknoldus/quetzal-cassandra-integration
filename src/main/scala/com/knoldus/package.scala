package com

import com.typesafe.config.{Config, ConfigFactory}

package object knoldus {

  val config: Config = ConfigFactory.load()
  val databaseName: String = config.getString("database.name")
  val userName: String = config.getString("database.username")
  val password: String = config.getString("database.password")
  val endPoint: String = config.getString("database.endpoint")
}
