package com.knoldus.model

import com.outworkers.phantom.dsl._

abstract class DPHData extends Table[DPHData, DPH] {

  object subject extends StringColumn

  object spill extends IntColumn

  object pred1 extends StringColumn

  object val1 extends StringColumn

  object pred2 extends StringColumn

  object val2 extends StringColumn

  object pred3 extends StringColumn

  object val3 extends StringColumn

  object pred4 extends StringColumn

  object val4 extends StringColumn

  object pred5 extends StringColumn

  object val5 extends StringColumn

}
