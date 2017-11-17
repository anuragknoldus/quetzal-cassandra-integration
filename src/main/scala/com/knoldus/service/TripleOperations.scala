package com.knoldus
package service

import com.google.inject.Inject
import com.knoldus.helper.QueryHelper
import com.knoldus.model.{CassandraCluster, PredicateInfo, Triple}
import com.knoldus.{DPH, sparkSession}
import org.apache.spark.sql.{Encoder, Encoders}

@Inject
class TripleOperations()(
  cassandraCluster: CassandraCluster,
  predicateHashing: PredicateHashing,
  directPredicateHashing: DirectPredicateHashing) {

  /**
    * Method used for storing the Triples into DPH Table on the basis of curtain condition
    * which we check into this method
    *
    * @param triple Triple Value
    * @return
    */
  def storeTriple(triple: Triple): Boolean = {

    predicateHashing.getPredicateDetails(triple.predicate) match {
      case Some(columnLocation) =>
        val entityInfo = directPredicateHashing.getUpdateInfo(columnLocation, triple)
        entityInfo match {
          case entityInformation if entityInformation.isAvailable && entityInformation.id.isEmpty =>
            directPredicateHashing.updateTripleToDPH(triple, entityInfo)
          case entityInformation if entityInformation.isAvailable && entityInformation.id.isDefined =>
            directPredicateHashing.updateViaId(triple, entityInfo)
          case _ => directPredicateHashing.storeTripleToDPH(triple, DomainName, entityInfo)
        }
      case None => storeFreshTriple(triple)
    }
  }

  /**
    * Method is used for Storing the Triples first time
    *
    * @param triple Triple Value
    * @return
    */
  def storeFreshTriple(triple: Triple): Boolean = {

    val (minColumnValue, maxColumnValue) = predicateHashing.getHashValue(triple.predicate)
    val entityInfo = directPredicateHashing.getEntityInfo(minColumnValue, maxColumnValue, triple)
    val predicateInfo = PredicateInfo(triple.predicate, entityInfo.location.toString)
    if (predicateHashing.storePredicate(predicateInfo)) {
      if (entityInfo.isAvailable) {
        val result = directPredicateHashing.updateTripleToDPH(triple, entityInfo)
        result
      } else {
        directPredicateHashing.storeTripleToDPH(triple, DomainName, entityInfo)
      }
    } else {
      throw new Exception(ExceptionForPredicateTable)
    }
  }

  def fetchObject(subject: String, predicate: String): Option[Triple] = {
    implicit val tripleEncoder: Encoder[Triple] = Encoders.product[Triple]
    directPredicateHashing.registerDPHTable(sparkSession)
    val location = predicateHashing.getPredicateDetails(predicate)
    location match {
      case Some(predicateLocation) => sparkSession.sql(
        s"""Select entity AS entry, prop$predicateLocation AS predicate,
           | val$predicateLocation AS value from $DPH where entity = '$subject'
           | and prop$predicateLocation = '$predicate' limit 1""".
          stripMargin).as[Triple]
        .collect().headOption
      case None => None
    }
  }
}

