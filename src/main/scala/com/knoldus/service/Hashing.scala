package com.knoldus.service

class Hashing {

  val isMod: (Int, Int) => Int = (m, i) => i % m + 1

  val mod5 = isMod(5, _: Int)

  def hashCalFirst(predicate: String): Int = {
    mod5(predicate.toLowerCase.toCharArray.sum.toInt)
  }

  def hashCalSecond(predicate: String): Int = {
    mod5(predicate.toUpperCase.toCharArray.sum.toInt)
  }
}
