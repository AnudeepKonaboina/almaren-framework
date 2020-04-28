package com.github.music.of.the.ainur.almaren.state.core

import com.github.music.of.the.ainur.almaren.State
import org.apache.spark.sql.DataFrame

private[ainur] abstract class Source() extends State {
  override def executor(df: DataFrame): DataFrame = source(df)

  def source(df: DataFrame): DataFrame
}

case class SourceSql(sql: String) extends Source {
  override def source(df: DataFrame): DataFrame = {
    logger.info(s"sql:{$sql}")
    val sqlDf = df.sparkSession.sql(sql)
    sqlDf
  }
}

case class SourceJdbc(url: String, driver: String, query: String, user: Option[String], password: Option[String], params: Map[String, String] = Map[String, String]()) extends Source {
  override def source(df: DataFrame): DataFrame = {
    logger.info(s"url:{$url}, driver:{$driver}, query:{$query}, username : {${user.getOrElse(None)}}, params:{$params}")
    val tempDf = df.sparkSession.read.format("jdbc")
      .option("url", url)
      .option("driver", driver)
      .option("dbtable", s"(${query}) MY_TABLE")
      .options(params)

    if (user.isEmpty && password.isEmpty) {
      tempDf.load()
    }
    else {
      tempDf
        .option("user", s"${user.getOrElse()}")
        .option("password", s"${password.getOrElse()}")
        .load()
    }
  }
}

case class SourceFile(format: String, path: String, params: Map[String, String] = Map[String, String]()) extends Source {
  override def source(df: DataFrame): DataFrame = {
    logger.info(s"format:{$format}, path:{$path}, params:{$params}")
    df.sparkSession.read.format(format)
      .options(params)
      .load(path)
  }
}
