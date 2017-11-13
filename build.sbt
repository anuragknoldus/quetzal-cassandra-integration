name := """quetzal-cassandra-integration"""

version := "1.0"

scalaVersion := "2.11.8"

organization := "com.knoldus"

//Source
val cassandraConnector = "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.5"
val sparkSql = "org.apache.spark" % "spark-sql_2.11" % "2.2.0" % "provided"
val cassandra = "org.apache.cassandra" % "cassandra-all" % "3.11.1"
val cassandraDriver =  "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.1"
val typeSafeConfig = "com.typesafe" % "config" % "1.3.2"

libraryDependencies ++= Seq(cassandraConnector, sparkSql, cassandraDriver, cassandra, typeSafeConfig)

//Test
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"

libraryDependencies ++= Seq(scalaTest)

//Overriden
val thrift = "org.apache.thrift" % "libthrift" % "0.9.2" pomOnly()
val guava = "com.google.guava" % "guava" % "19.0"
val netty = "io.netty" % "netty-all" % "4.0.44.Final"

dependencyOverrides ++= Seq(netty, thrift, guava)
