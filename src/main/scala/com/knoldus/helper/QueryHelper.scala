package com.knoldus
package helper

import java.util.UUID

import com.datastax.driver.core.Row
import com.knoldus.model.{EntityInfo, Triple}

class QueryHelper {

  val PredicateTableCreation = s"""CREATE TABLE IF NOT EXISTS $databaseName.$DirectPredicate($Predicate text PRIMARY KEY, $Location text)"""
  val CreateDatabase: String =
    s"""CREATE KEYSPACE IF NOT EXISTS $databaseName WITH REPLICATION = { 'class' : 'SimpleStrategy'
       |, 'replication_factor' : 3 };""".stripMargin
  val dphTableCreation: (String => String) = queryString =>
    s"""CREATE TABLE IF NOT EXISTS $databaseName.$DPH($Id UUID, $Entity text, $Spill int, $Domain text, $queryString, PRIMARY KEY(id, domain))"""
  val fetchValue: ((Int, Int, String) => String) = (minColumnValue, maxColumnValue, subject) =>
    s"""SELECT $Spill, ${Prop + minColumnValue}, ${Prop + maxColumnValue} FROM $DPH WHERE $Entity = '$subject' $AllowFiltering"""
  val insertValue: ((EntityInfo, Triple, UUID, String) => String) = (entityInfo, tripleInfo, id, domain) =>
    s"""Insert into $databaseName.$DPH($Id, $Entity, $Spill, $Domain, ${Prop + entityInfo.location}, ${Val + entityInfo.location})
       | values ($id, '${tripleInfo.entry}', ${entityInfo.spill}, '$domain', '${tripleInfo.predicate}', '${tripleInfo.value}')""".stripMargin
  val fetchIDWithSpillAndProp: ((String, String) => String) = (value, subject) =>
    s"""SELECT id, $Spill, ${Prop + value} FROM $DPH WHERE $Entity = '$subject' $AllowFiltering"""
  val fetchIDWithDomain: (String => String) = subject => s"""SELECT id, $Domain FROM $DPH WHERE $Entity = '$subject' $AllowFiltering"""
  val updateQuery: ((Int, String, String, Row) => String) = (location, value, predicate, row) =>
    s"""update $databaseName.$DPH set ${Val + location} = '${value}', ${Prop + location} = '${predicate}'
       | where domain = '${row.getString("domain")}' and id = ${row.getUUID("id")}""".stripMargin
  val fetchDomain: (String => String) = subject => s"""SELECT $Domain FROM $DPH WHERE $Entity = '$subject' $AllowFiltering"""
  val updateQueryWithParticularID: ((Int, String, String, Row, UUID) => String) = (location, value, predicate, row, id) =>
    s"""update $databaseName.$DPH set ${Val + location} = '$value', ${Prop + location} = '${predicate}'
       | where domain = '${row.getString("domain")}' and id = $id""".stripMargin
  val updateMultipleRow: ((Int, String, UUID) => String) = (spill, domain, id) =>
    s"""update $databaseName.$DPH set spill = $spill where domain = '$domain' and id = $id""".stripMargin
  val insertValueInPredicate: ((String, String) => String) = (predicate, location) =>
    s"""INSERT INTO $DirectPredicate($Predicate, $Location) VALUES('$predicate', '$location')"""
  val fetchDataFromPredicate: (String => String) = predicate => s"""SELECT * FROM $DirectPredicate WHERE $Predicate = '$predicate'"""

}
