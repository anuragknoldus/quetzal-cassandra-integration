package com.knoldus.model

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

abstract class DPHData extends Table[DPHData, DPH] with RootConnector{

  object subject extends StringColumn with PartitionKey

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

  def saveToDPH(dPH: DPH): Future[ResultSet] = {
    store(dPH)
      /*.value(_.subject, dPH.subject)
      .value(_.spill, dPH.spill)
      .value(_.val1, dPH.val1)
      .value(_.pred1, dPH.pred1)
      .value(_.val2, dPH.val2)
      .value(_.pred2, dPH.pred2)
      .value(_.val3, dPH.val3)
      .value(_.pred3, dPH.pred3)
      .value(_.val4, dPH.val4)
      .value(_.pred4, dPH.pred4)
      .value(_.val5, dPH.val5)
      .value(_.pred5, dPH.pred5)*/
      .future()
  }
}
