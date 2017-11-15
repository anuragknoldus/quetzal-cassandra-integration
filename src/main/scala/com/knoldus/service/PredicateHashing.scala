package com.knoldus
package service

import com.datastax.driver.core.Session
import com.google.inject.Inject
import com.knoldus.model.{CassandraCluster, PredicateInfo}

@Inject
class PredicateHashing()(cassandraCluster: CassandraCluster, hashing: Hashing) {

  def session: Session = cassandraCluster.createCluster().connect(databaseName)

  def storePredicate(predicateInfo: PredicateInfo): Boolean = {

    val storePredicateQuery =
      s"""INSERT INTO $DirectPredicate($Predicate, $Location)
         VALUES('${predicateInfo.predicate}', '${predicateInfo.location}')"""
    try {
      Option(session.execute(storePredicateQuery)).isDefined
    } catch {
      case _: Exception => false
    }
    finally {
      session.close()
    }
  }

  def getPredicateDetails(predicate: String): Option[String] = {

    val selectPredicateQuery = s"""SELECT * FROM $DirectPredicate WHERE $Predicate = '$predicate'"""
    val row = session.execute(selectPredicateQuery).one()
    session.close()
    Option(row) match {
      case Some(firstRow) => Some(firstRow.getString(Location))
      case None => None
    }
  }

  def getHashValue(predicate: String): (Int, Int) = {
    val hashOne = hashing.applyHashingOne(predicate)
    val hashTwo = hashing.applyHashingTwo(predicate)
    if (hashOne < hashTwo) {
      (hashOne, hashTwo)
    } else {
      (hashTwo, hashOne)
    }
  }

}
