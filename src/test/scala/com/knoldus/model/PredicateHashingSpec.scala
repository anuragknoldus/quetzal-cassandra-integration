package com.knoldus
package model

import com.datastax.driver.core
import com.datastax.driver.core.{ResultSet, Row, Session}
import com.knoldus.helper.QueryHelper
import com.knoldus.service.{Hashing, PredicateHashing}
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar


class PredicateHashingSpec extends FlatSpec with MockitoSugar {

  val predicateInfo = PredicateInfo("Test", "0")
  val mockedHashing: Hashing = mock[Hashing]
  val cluster: CassandraCluster = mock[CassandraCluster]
  val mockedCluster: core.Cluster = mock[core.Cluster]
  val mockedSession: Session = mock[Session]
  val mockedResultSet: ResultSet = mock[ResultSet]
  val mockedRow: Row = mock[Row]
  val mockedQueryHelper: QueryHelper = new QueryHelper
  val predicateHashing = new PredicateHashing(cluster, mockedHashing, mockedQueryHelper)

  when(cluster.createCluster()).thenReturn(mockedCluster)
  when(mockedCluster.connect(databaseName)).thenReturn(mockedSession)

  "Predicate Hashing" should "Store the Predicate into" in {
    val insertSql =
      s"""INSERT INTO direct_predicate($Predicate, $Location) VALUES('${predicateInfo.predicate}', '${predicateInfo.location}')"""
    when(mockedSession.execute(insertSql)).thenReturn(mockedResultSet)
    val response = predicateHashing.storePredicate(predicateInfo)
    assert(response)
  }

  "Predicate Hashing" should "Retrieve the Predicate into" in {
    val selectSql = """SELECT * FROM direct_predicate WHERE predicate = 'Test'"""
    when(mockedSession.execute(selectSql)).thenReturn(mockedResultSet)
    when(mockedResultSet.one()).thenReturn(mockedRow)
    when(mockedRow.getString(Location)).thenReturn("0")
    val response = predicateHashing.getPredicateDetails(predicateInfo.predicate)
    val expectedOutput = "0"
    assert(response.isDefined)
    assert(response.getOrElse("") == expectedOutput)
  }
}
