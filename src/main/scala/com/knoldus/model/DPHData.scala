package com.knoldus.model

import com.outworkers.phantom.dsl._
import com.knoldus.model.SearchColumns._
import scala.concurrent.Future

abstract class DPHData extends Table[DPHData, DPH] {

  object subject extends StringColumn with PartitionKey

  object spill extends IntColumn with Index

  object pred1 extends StringColumn with Index

  object val1 extends StringColumn with Index

  object pred2 extends StringColumn with Index

  object val2 extends StringColumn with Index

  object pred3 extends StringColumn with Index

  object val3 extends StringColumn with Index

  object pred4 extends StringColumn with Index

  object val4 extends StringColumn with Index

  object pred5 extends StringColumn with Index

  object val5 extends StringColumn with Index

  def createTable: Seq[ResultSet] = CassandraDatabase.create()

  def saveToDPH(dPH: DPH): Future[ResultSet] = {

    insert
      .value(_.subject, dPH.subject)
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
      .value(_.pred5, dPH.pred5)
      .future()
  }

  def searchByColumn(columnName: SearchColumns.Value, value: String): Future[Option[DPH]] = {

    val partialQuery = select.allowFiltering

    columnName match {
      case PRED1 => partialQuery.where(_.pred1.eqs(value)).one
      case PRED2 => partialQuery.where(_.pred2.eqs(value)).one
      case PRED3 => partialQuery.where(_.pred3.eqs(value)).one
      case PRED4 => partialQuery.where(_.pred4.eqs(value)).one
      case PRED5 => partialQuery.where(_.pred5.eqs(value)).one
      case VAL1 => partialQuery.where(_.val1.eqs(value)).one
      case VAL2 => partialQuery.where(_.val2.eqs(value)).one
      case VAL3 => partialQuery.where(_.val3.eqs(value)).one
      case VAL4 => partialQuery.where(_.val4.eqs(value)).one
      case VAL5 => partialQuery.where(_.val5.eqs(value)).one
      case _ => throw new Exception("Undefined Column Name")
    }
  }
}
