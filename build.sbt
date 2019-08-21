name := "fp-akka"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0-RC1"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0-RC1"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.28"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.5.23"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.23"

libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"

libraryDependencies += "com.propensive" %% "magnolia" % "0.11.0"

libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC11-1"

libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.0-RC11-1"


resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")