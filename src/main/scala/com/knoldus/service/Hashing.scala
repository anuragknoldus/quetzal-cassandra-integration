package com.knoldus.service

import com.knoldus.model._

import scala.concurrent.ExecutionContext.Implicits.global

class Hashing {

  def applyMod: (Int, Int) => Int = (base, value) => value % base + 1

  lazy val mod5 = applyMod(5, _: Int)

  def applyHashingOne(predicate: String): Int =
    mod5(predicate.toLowerCase.toCharArray.sum.toInt)

  def applyHashingTwo(predicate: String): Int =
    mod5(predicate.toUpperCase.toCharArray.sum.toInt)

}

object Hashing extends DbProvider {

  def main(args: Array[String]): Unit = {

    val hashing = new Hashing
    val dPH = DPH("Larry Page", 0, "CEO", "Google", "Owner", "Alphabet", "Nationality", "Livepedlian", "Lives", "US", "wealth", "$200 Million", "Chemical")
    val predicateStore = PredicateStore("CEO", "pred1")
    database.dph.createTable
    database.storeDPH(dPH)
    database.storePredicate(predicateStore)
    Thread.sleep(500L)
    database.dph.searchByColumn(SearchColumns.PRED1, "CEO").foreach(tripleData => println(tripleData))
    Thread.sleep(1000L)
    println("Hashing One " + hashing.applyHashingOne("CEO"))
    println("Hashing Two " + hashing.applyHashingTwo("CEO"))
  }
}
