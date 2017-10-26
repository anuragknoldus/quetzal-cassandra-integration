name := """quetzal-cassandra-integration"""

version := "1.0"

scalaVersion := "2.12.3"

organization := "com.knoldus"

//Source

val phantom = "com.outworkers" % "phantom-dsl_2.11" % "2.14.5"
val kafkaClient = "org.apache.kafka" % "kafka-clients" % "0.11.0.1"

//Test

val scalaTest = "org.scalatest" % "scalatest_2.12" % "3.2.0-SNAP9" % "test"

libraryDependencies ++= Seq(phantom, kafkaClient)
libraryDependencies ++= Seq(scalaTest)
