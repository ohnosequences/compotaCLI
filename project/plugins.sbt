resolvers ++= Seq(
  "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com",
  "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"
)


addSbtPlugin("net.databinder" % "conscript-plugin" % "0.3.5")

//nice-sbt-settings v0.4.0-RC1
addSbtPlugin( "ohnosequences" % "nice-sbt-settings" % "0.3.1")
