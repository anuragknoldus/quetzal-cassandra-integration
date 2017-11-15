package com.knoldus
package service

import com.google.inject.Inject
import com.knoldus.model.{Cluster, Triple}
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}

@Inject
class TripleManipulations()(predicateHashing: PredicateHashing) {


  def registerDPHTable(spark: SparkSession): Unit = {
    spark.read.format("org.apache.spark.sql.cassandra").option("keyspace", databaseName)
      .option("table", DPH).load().createOrReplaceTempView(DPH)
  }

  def fetchLocation(predicate: String): String = {
    val location = predicateHashing.getPredicateDetails(predicate)
    location match {
      case Some(predicateLocation) => predicateLocation
      case None => null
    }
  }

  def fetchObject(subject: String, predicate: String): Option[Triple] = {
    implicit val tripleEncoder: Encoder[Triple] = Encoders.product[Triple]
    registerDPHTable(sparkSession)
    val location = fetchLocation(predicate)
    sparkSession.sql(
      s"""Select entity AS entry, prop$location AS predicate,
         | val$location AS value from $DPH where entity = '$subject'
         |and prop$location = '$predicate' limit 1""".stripMargin).as[Triple]
      .collect().headOption
  }

}
