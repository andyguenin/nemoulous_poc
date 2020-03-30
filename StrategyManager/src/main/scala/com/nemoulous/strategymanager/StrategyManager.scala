package com.nemoulous.strategymanager

import com.nemoulous.util.Settings
import org.flywaydb.core.Flyway

object StrategyManager {

  def main(args: Array[String]): Unit = {
    val settings = Settings(args)
    migrate(settings)
  }

  private[this] def migrate(settings: Settings): Unit = {
    val flyway = Flyway.configure.dataSource(settings.ds).schemas(settings.schema).load()
    flyway.migrate()
  }
}
