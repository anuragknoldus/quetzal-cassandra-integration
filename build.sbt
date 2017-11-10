name := """quetzal-cassandra-integration"""

version := "1.0"

scalaVersion := "2.11.8"

organization := "com.knoldus"

//Source
val cs_connector = "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.5"
val sparkSql = "org.apache.spark" % "spark-sql_2.11" % "2.2.0" % "provided"
val cassandra = "org.apache.cassandra" % "cassandra-all" % "3.11.1"
val cassandra_driver =  "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.1"

//Test
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"

libraryDependencies ++= Seq(cs_connector, sparkSql, cassandra_driver, cassandra)
libraryDependencies ++= Seq(scalaTest)
dependencyOverrides ++= Seq(
  "io.netty" % "netty-all" % "4.0.44.Final",
  "org.apache.thrift" % "libthrift" % "0.9.2" pomOnly(),
  "com.google.guava" % "guava" % "19.0"
)