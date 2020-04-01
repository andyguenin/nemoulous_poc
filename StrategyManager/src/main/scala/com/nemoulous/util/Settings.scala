package com.nemoulous.util

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

case class Settings(
                   args: Array[String]
                   ) {
  val config = ConfigFactory.load(args.head)

  private def getOrElse[T](path: String, c: Config => String => T, default: => T): T = {
    if(config.hasPath(path)) {
      c(config)(path)
    } else {
      default
    }
  }

  val dbUsername = getOrElse("db.username", _.getString, "postgres")
  private[this] val dbPassword = getOrElse("db.password", _.getString, "")
  val schema = getOrElse("db.schema", _.getString, "nemoulous")
  val database = getOrElse("db.databaseName", _.getString, "nemoulous")
  val serverName = getOrElse("db.serverName", _.getString, "nemoulous")
  val portNumber = getOrElse("db.portNumber", _.getInt, 5432)


  val kafkaBootstrapServers = getOrElse("akka.kafka.consumer.kafka-clients.bootstrap.servers", _.getString, "localhost:9092")

  lazy val ds = {
    val internalConfig = {
      import java.io.PrintWriter
      val props = new Properties()
      props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
      props.setProperty("dataSource.user", dbUsername)
      props.setProperty("dataSource.password", dbPassword)
      props.setProperty("dataSource.databaseName", database)
      props.setProperty("dataSource.portNumber", portNumber.toString)
      props.setProperty("dataSource.serverName", serverName)
      props.put("dataSource.logWriter", new PrintWriter(System.out))
      val config = new HikariConfig(props)
      config.setSchema(schema)
      config
    }
    new HikariDataSource(internalConfig)
  }

}
