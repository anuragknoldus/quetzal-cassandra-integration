package com.knoldus

import com.knoldus.helper.QueryHelper
import com.knoldus.model.CassandraCluster
import com.knoldus.service.{DirectPredicateHashing, Hashing, PredicateHashing, TripleOperations}
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

class TripleLoaderSpec extends FlatSpec with MockitoSugar {

  val queryHelper = new QueryHelper
  val cluster = new CassandraCluster(queryHelper)
  val hashing = new Hashing
  val predicateHashing =  new PredicateHashing(cluster, hashing, queryHelper)
  val directPredicateHashing = new DirectPredicateHashing(cluster, queryHelper)
  val tripleOperations = new TripleOperations()(cluster, predicateHashing, directPredicateHashing)
  val tripleLoader =  new TripleLoader

  "Triple loader" should "load Triple file to Cassandra" in {
    val filePath = getClass.getResource("/elssie.nt").getPath
    val result = tripleLoader.loadTripleFileToCassandra(filePath, tripleOperations)
    assert(result)
  }

}
