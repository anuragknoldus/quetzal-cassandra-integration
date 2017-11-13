package com.knoldus
package service

import com.datastax.driver.core.Session
import com.google.inject.Inject
import com.knoldus.model.{Cluster, PredicateInfo}

@Inject
class PredicateHashing(cluster: Cluster) {

  def session: Session = cluster.createCluster().connect(databaseName)

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
      case Some(firstRow) => Some(Prop + firstRow.getString(Location))
      case None => None
    }
  }
}
