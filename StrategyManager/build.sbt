name := "StrategyManager"

enablePlugins(FlywayPlugin)

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "org.flywaydb" % "flyway-core" % "5.2.4"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.4"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.4"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.11"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % "2.6.4"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.2"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11"

libraryDependencies += "com.zaxxer" % "HikariCP" % "3.3.1"

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.30"

libraryDependencies += "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.0.4"

