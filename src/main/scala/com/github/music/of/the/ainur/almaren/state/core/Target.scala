package com.github.music.of.the.ainur.almaren.state.core

import com.github.music.of.the.ainur.almaren.State
import com.github.music.of.the.ainur.almaren.util.Constants
import org.apache.spark.sql.{DataFrame, SaveMode}

private[almaren] abstract class Target extends State {
  override def executor(df: DataFrame): DataFrame = target(df)
  def target(df: DataFrame): DataFrame
}

case class TargetSql(sql: String) extends Target {
  override def target(df: DataFrame): DataFrame = {
    logger.info(s"sql:{$sql}")
    df.createOrReplaceTempView(Constants.TempTableName)
    val sqlDf = df.sqlContext.sql(sql)
    df
  }
}

case class TargetJdbc(url: String, driver: String, dbtable: String, saveMode:SaveMode, params:Map[String,String]) extends Target {
  override def target(df: DataFrame): DataFrame = {
    logger.info(s"url:{$url}, driver:{$driver}, dbtable:{$dbtable}, params:{$params}")
    df.write.format("jdbc")
      .option("url", url)
      .option("driver", driver)
      .option("dbtable", dbtable)
      .options(params)
      .mode(saveMode)
      .save()
    df
  }
}
