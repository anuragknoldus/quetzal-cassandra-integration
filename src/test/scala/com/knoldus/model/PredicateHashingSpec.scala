package com.knoldus
package model

import com.datastax.driver.core
import com.datastax.driver.core.{ResultSet, Row, Session}
import com.knoldus.service.PredicateHashing
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._


class PredicateHashingSpec extends FlatSpec with MockitoSugar {

  val predicateInfo = PredicateInfo("Test", "0")
  val cluster: Cluster = mock[Cluster]
  val mockedCluster: core.Cluster = mock[core.Cluster]
  val mockedSession: Session = mock[Session]
  val mockedResultSet: ResultSet = mock[ResultSet]
  val mockedRow: Row = mock[Row]
  val predicateHashing = new PredicateHashing(cluster)

  when(cluster.createCluster()).thenReturn(mockedCluster)
  when(mockedCluster.connect(databaseName)).thenReturn(mockedSession)

  "Predicate Hashing" should "Store the Predicate into" in {

    val insertSql =
      """INSERT INTO direct_predicate(predicate, location)
        |         VALUES('Test', '0')""".stripMargin
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
    val expectedOutput = Prop + "0"
    assert(response.isDefined)
    assert(response.getOrElse("") == expectedOutput)
  }
}
