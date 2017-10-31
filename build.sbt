enablePlugins(RiffRaffArtifact, AssemblyPlugin)

name := "cloudtrail-lambda"

version := "1.0"

scalaVersion := "2.12.3"

// https://mvnrepository.com/artifact/org.jruby/jruby-complete
libraryDependencies += "org.jruby" % "jruby-complete" % "9.1.13.0"

// https://mvnrepository.com/artifact/commons-logging/commons-logging
libraryDependencies += "commons-logging" % "commons-logging" % "1.2"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
)

lazy val app = (project in file(".")).settings(
  organization := "com.theguardian",
  assemblyJarName in assembly := "cloudtrail-lambda.jar",

)

assemblyMergeStrategy in assembly := {
  case "shared.thrift" => MergeStrategy.discard
  case PathList("org","joda","time", "tz", xs @ _*)=> MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

riffRaffPackageType := assembly.value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffManifestBranch := sys.env.getOrElse("CIRCLE_BRANCH","unknown")
riffRaffManifestRevision := sys.env.getOrElse("CIRCLE_BUILD_NUM","SNAPSHOT")
riffRaffManifestVcsUrl := sys.env.getOrElse("CIRCLE_BUILD_URL", "")
riffRaffBuildIdentifier := sys.env.getOrElse("CIRCLE_BUILD_NUM", "SNAPSHOT")

riffRaffManifestProjectName := "multimedia:cloudtrail-lambda"
