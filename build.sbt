import AssemblyKeys._

lazy val commonSettings = Seq(
  scalaVersion := "2.10.6"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(assemblySettings: _*).
  settings(
    name := "closestAirport",
    version := "1.0.0",
    libraryDependencies +=  "org.apache.spark" %% "spark-core" % "2.0.2" % "provided",
    libraryDependencies += "net.debasishg" %% "redisclient" % "3.3",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    mainClass in assembly := Some("org.bnjzer.closestairport.ClosestAirport")
  )

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
    case "application.conf" => MergeStrategy.concat
    case "unwanted.txt"     => MergeStrategy.discard
    case x => old(x)
  }
}
