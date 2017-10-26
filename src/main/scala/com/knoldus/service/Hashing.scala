package com.knoldus.service

import com.knoldus.model.{CassandraDatabase, DPH, PredicateStore}

class Hashing {

  def applyMod: (Int, Int) => Int = (base, value) => value % base + 1

  lazy val mod5 = applyMod(5, _: Int)

  def applyHashingOne(predicate: String): Int =
    mod5(predicate.toLowerCase.toCharArray.sum.toInt)

  def applyHashingTwo(predicate: String): Int =
    mod5(predicate.toUpperCase.toCharArray.sum.toInt)

}

object Hashing {
  def main(args: Array[String]): Unit = {

    val hashing = new Hashing
    val dPH = DPH("Larry Page", 0, "CEO", "Google", "Owner", "Alphabet",
      "Nationality", "Livepedlian", "Lives", "US", "wealth", "$200 Million")
    val predicateStore = PredicateStore("CEO", "pred1")
    CassandraDatabase.dph.createTable
    CassandraDatabase.dph.saveToDPH(dPH)
    CassandraDatabase.predicate.saveToPredicateLookUp(predicateStore)
    Thread.sleep(10000L)
    println("Hashing One " + hashing.applyHashingOne("CEO"))
    println("Hashing Two " + hashing.applyHashingTwo("CEO"))
  }
}
