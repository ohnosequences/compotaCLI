Nice.scalaProject

//Nice.fatArtifactSettings

name := "nispero-cli"

organization := "ohnosequences"

libraryDependencies += "com.bacoder.jgit" % "org.eclipse.jgit" % "3.1.0-201309071158-r"

libraryDependencies += "org.scala-sbt" % "launcher-interface" % "0.13.0" % "provided"

libraryDependencies += "ohnosequences" % "aws-scala-tools_2.10" % "0.6.3"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies +=  "org.clapper"  %% "avsl" % "1.0.1"

dependencyOverrides += "commons-codec" % "commons-codec" % "1.6"

