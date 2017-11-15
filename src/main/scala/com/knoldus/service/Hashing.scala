package com.knoldus
package service

class Hashing {

  def applyMod: (Int, Int) => Int = (base, value) => value % base + ConstantValues.One.id

  lazy val baseMod = applyMod(dphTableSize, _: Int)

  def applyHashingOne(predicate: String): Int =
    baseMod(predicate.toLowerCase.toCharArray.sum.toInt)

  def applyHashingTwo(predicate: String): Int =
    baseMod(predicate.toUpperCase.toCharArray.sum.toInt)
}
