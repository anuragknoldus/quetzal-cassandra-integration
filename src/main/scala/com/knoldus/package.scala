package com

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

package object knoldus {

  val config: Config = ConfigFactory.load()
  val databaseName: String = config.getString("database.name")
  val userName: String = config.getString("database.username")
  val password: String = config.getString("database.password")
  val databaseEndPoint: String = config.getString("database.endpoint")
  val databaseUrl: String = config.getString("database.url")
  val dphTableSize: Int = config.getInt("database.partition")
  val sparkMaster: String = config.getString("spark.master")
  val sparkAppName: String = config.getString("spark.appName")
  val Id: String = "id"
  val Entity: String = "entity"
  val Spill: String = "spill"
  val Prop: String = "prop"
  val Val: String = "val"
  val Domain: String = "domain"
  val DPH: String = "dph"
  val Predicate: String = "predicate"
  val Location: String = "location"
  val DirectPredicate: String = "direct_predicate"
  val AllowFiltering = "ALLOW FILTERING"
  val sparkConf: SparkConf = new SparkConf()
    .setAppName(sparkAppName)
    .setMaster(sparkMaster)
  lazy val sparkSession: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
  val EmptyString = ""
  val ExceptionForPredicateTable = "Unable to Store triple in predicate_mapping Table"
  val DomainName = "DPH_DATA"

  object ConstantValues extends Enumeration {
    type Numbers = Value
    val Zero, One, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten = Value
  }

}
