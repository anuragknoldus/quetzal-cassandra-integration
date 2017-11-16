package com.knoldus

import com.knoldus.helper.QueryHelper
import com.knoldus.model.{CassandraCluster, Triple}
import com.knoldus.service.{DirectPredicateHashing, Hashing, PredicateHashing, TripleOperations}
import org.apache.jena.graph
import org.apache.jena.graph.Node
import org.apache.jena.riot.RDFDataMgr

import scala.collection.JavaConversions._

object TripleLoader {

  implicit def nodeToString(node: Node): String = node.toString

  def main(args: Array[String]): Unit = {

    val queryHelper = new QueryHelper
    val hashing = new Hashing
    val cassandraCluster = new CassandraCluster(queryHelper)
    val predicateHashing = new PredicateHashing(cassandraCluster, hashing, queryHelper)
    val directPredicateHashing = new DirectPredicateHashing(cassandraCluster, queryHelper)
    val tripleOperations = new TripleOperations()(cassandraCluster, predicateHashing, directPredicateHashing)
    cassandraCluster.createDPHTable()
    cassandraCluster.createPredicateSchema
    loadFileAsModel().foreach { jenaTriple =>
      val customTriple = Triple(jenaTriple.getSubject, jenaTriple.getPredicate, jenaTriple.getObject)
      tripleOperations.storeTriple(customTriple)
    }
  }

  def loadFileAsModel(path: String = ""): List[graph.Triple] = {

    val model = RDFDataMgr.loadModel("/home/akash/Downloads/elssie.nt")
    val emptyNode = Node.ANY
    model.getGraph.find(emptyNode, emptyNode, emptyNode).toList.toList
  }
}
