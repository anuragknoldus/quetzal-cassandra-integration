package com.knoldus
package service

import com.datastax.driver.core.Session
import com.google.inject.Inject
import com.knoldus.helper.QueryHelper
import com.knoldus.model.{CassandraCluster, PredicateInfo}

@Inject
class PredicateHashing(cassandraCluster: CassandraCluster,
                       hashing: Hashing,
                       queryHelper: QueryHelper) {

  lazy val session: Session = cassandraCluster.createCluster().connect(databaseName)

  /**
    * Store the Predicate into Predicate Mapping Table
    *
    * @param predicateInfo
    * @return
    */
  def storePredicate(predicateInfo: PredicateInfo): Boolean = {

    val storePredicateQuery = queryHelper.insertValueInPredicate(predicateInfo.predicate, predicateInfo.location)
    try {
      Option(session.execute(storePredicateQuery)).isDefined
    } catch {
      case exception: Exception => throw exception
    }
  }

  /**
    * Fetch the Predicate from Predicate Mapping Table
    *
    * @param predicate
    * @return
    */
  def getPredicateDetails(predicate: String): Option[String] = {

    val selectPredicateQuery = queryHelper.fetchDataFromPredicate(predicate)
    try {
      val row = session.execute(selectPredicateQuery).one()
      Option(row) match {
        case Some(firstRow) => Some(firstRow.getString(Location))
        case None => None
      }
    } catch {
      case exception: Exception => throw exception
    }
  }

  /**
    * Method is used for getting the location info of the predicate
    *
    * @param predicate Predicate Value
    * @return (location1, location2)
    */
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
