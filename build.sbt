name := "gunplay_from_scratch"

version := "1.0"

scalaVersion := "2.12.2"

val akkaHttpVersion = "10.0.7"
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % "2.5.3",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3",
  "com.iheart" %% "ficus" % "1.4.3",
  "org.dyn4j" % "dyn4j" % "3.3.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.4.2",
  "net.debasishg" %% "redisreact" % "0.9",
  "org.mindrot" % "jbcrypt" % "0.3m"
)

mainClass in assembly := Some("com.mikeyu123.gunplay.server.WebServer")

