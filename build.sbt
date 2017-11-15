name := """quetzal-cassandra-integration"""

version := "1.0"

scalaVersion := "2.11.8"

organization := "com.knoldus"

//Source
val cassandraConnector = "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.5"
val sparkSql = "org.apache.spark" % "spark-sql_2.11" % "2.2.0"
val cassandra = "org.apache.cassandra" % "cassandra-all" % "3.11.1"
val cassandraDriver =  "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.1"
val typeSafeConfig = "com.typesafe" % "config" % "1.3.2"
val phantomDsl = "com.outworkers" % "phantom-dsl_2.11" % "2.14.5"

val sourceDependencies =  Seq(cassandraConnector, sparkSql, cassandraDriver, cassandra, typeSafeConfig, phantomDsl).map(_.exclude("org.slf4j", "slf4j-log4j12"))

//Test
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"
val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
val cassandraUnit = "org.cassandraunit" % "cassandra-unit" % "3.3.0.2" % "test"

val testDependencies= Seq(scalaTest, mockito, cassandraUnit)

//Overrides
val thrift = "org.apache.thrift" % "libthrift" % "0.9.2" pomOnly()
val guava = "com.google.guava" % "guava" % "19.0"
val netty = "io.netty" % "netty-all" % "4.0.44.Final"

val overridesDependencies= Seq(netty, thrift, guava).map(_.exclude("org.slf4j", "slf4j-log4j12"))

excludeDependencies ++= Seq("org.slf4j" % "slf4j-log4j12" % "1.7.16")
libraryDependencies ++= (sourceDependencies ++ testDependencies)
dependencyOverrides ++= overridesDependencies