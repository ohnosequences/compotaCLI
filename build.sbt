Nice.scalaProject

//Nice.fatArtifactSettings

name := "compota-cli"

scalaVersion := "2.11.6"

organization := "ohnosequences"

resolvers := Seq[Resolver](
  organization.value + " public maven releases"  at s3https(bucketRegion.value, "releases." + bucketSuffix.value),
  organization.value + " public maven snapshots" at s3https(bucketRegion.value, "snapshots." + bucketSuffix.value),
  Resolver.url(organization.value + " public ivy releases", url(s3https(bucketRegion.value, "releases." + bucketSuffix.value)))(ivy),
  Resolver.url(organization.value + " public ivy snapshots", url(s3https(bucketRegion.value, "snapshots." + bucketSuffix.value)))(ivy)
) ++ resolvers.value


libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.0.201506090130-r"

libraryDependencies += "ohnosequences" %% "aws-scala-tools" % "0.13.0"

//libraryDependencies += "commons-io" % "commons-io" % "2.4"
//
dependencyOverrides += "commons-logging" % "commons-logging" % "1.1.3"

dependencyOverrides += "org.apache.httpcomponents" % "httpclient" % "4.3.4"

