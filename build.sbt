name := """quetzal-cassandra-integration"""

version := "1.0"

scalaVersion := "2.12.3"

organization := "com.knoldus"


libraryDependencies ++= Seq(
  "org.apache.cassandra" % "cassandra-all" % "3.11.1",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.0",
  "com.outworkers" % "phantom-dsl_2.11" % "2.14.5",
  "org.apache.kafka" % "kafka-clients" % "0.11.0.1",
  "org.scalatest" % "scalatest_2.12" % "3.2.0-SNAP9" % "test"
)
