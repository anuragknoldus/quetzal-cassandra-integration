package com.knoldus.service

class Hashing {

  def applyMod: (Int, Int) => Int = (base, value) => value % base + 1

  lazy val mod5 = applyMod(5, _: Int)

  def applyHashingOne(predicate: String): Int =
    mod5(predicate.toLowerCase.toCharArray.sum.toInt)

  def applyHashingTwo(predicate: String): Int =
    mod5(predicate.toUpperCase.toCharArray.sum.toInt)

}
