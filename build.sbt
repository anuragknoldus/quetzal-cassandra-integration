name := """quetzal-cassandra-integration"""

version := "1.0"

scalaVersion := "2.11.8"

organization := "com.knoldus"

//Source
val cassandraConnector = "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.0"
val sparkSql = "org.apache.spark" % "spark-sql_2.11" % "2.2.0"
val typeSafeConfig = "com.typesafe" % "config" % "1.3.2"
val jena = "org.apache.jena" % "apache-jena-libs" % "3.5.0" pomOnly()

val sourceDependencies = Seq(cassandraConnector, sparkSql, typeSafeConfig,
  jena).map(_.exclude("org.slf4j", "slf4j-log4j12"))

//Test
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"
val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
val cassandraUnit = "org.cassandraunit" % "cassandra-unit" % "3.1.3.2" % "test"

val testDependencies = Seq(scalaTest, mockito, cassandraUnit)

//Overrides
val thrift = "org.apache.thrift" % "libthrift" % "0.9.2" pomOnly()
val guava = "com.google.guava" % "guava" % "19.0"
val netty = "io.netty" % "netty-all" % "4.0.44.Final"

val overridesDependencies = Seq(netty, thrift, guava).map(_.exclude("org.slf4j", "slf4j-log4j12"))

excludeDependencies ++= Seq("org.slf4j" % "slf4j-log4j12" % "1.7.16")
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"
// https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.9.0" % "test"
libraryDependencies ++= (sourceDependencies ++ testDependencies)
dependencyOverrides ++= overridesDependencies
parallelExecution in Test := false
