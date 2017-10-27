package com.knoldus.model

import com.datastax.driver.core
import com.outworkers.phantom.dsl._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import scala.collection.JavaConverters._
import scala.concurrent.JavaConversions._
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


  def searchByColumn(columnName: List[String], values: List[String]): DPH = {

    val columnNameWithValues = columnName.zip(values)
    val partialWhereQuery = columnNameWithValues.map { case (columnsName, value) =>
      s"""$columnsName = '$value'"""
    }.mkString(" AND ")

    val completeSQL = s"SELECT * FROM quetzal.dphdata WHERE $partialWhereQuery LIMIT 1 ALLOW FILTERING"
    val dPHAsRow: core.Row = CassandraDatabase.session.execute(completeSQL).one()
    if (Option(dPHAsRow).isDefined) {
      convertRowToDPH(dPHAsRow)
    }
    else {
      throw new Exception("No Data Found")
    }
  }

  private def convertRowToDPH(dPHAsRow: core.Row): DPH = {

    val partialJson = dPHAsRow.getColumnDefinitions.asScala.map { columnDefinition =>
      columnDefinition.getType.getName.toString match {

        case "int" => s""""${columnDefinition.getName}":"${dPHAsRow.getInt(columnDefinition.getName)}""""
        case "varchar" => s""""${columnDefinition.getName}":"${dPHAsRow.getString(columnDefinition.getName)}""""
      }
    }.mkString(",")
    val completeJson = s"""{$partialJson}"""
    implicit val formats: DefaultFormats.type = DefaultFormats
    parse(completeJson).extract[DPH]
  }
}
