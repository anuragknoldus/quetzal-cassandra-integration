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

  object domain extends StringColumn

  def createTable: Seq[ResultSet] = CassandraDatabase.create()


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
