enablePlugins(RiffRaffArtifact, UniversalPlugin, JDebPackaging, DebianPlugin, JavaServerAppPackaging, SystemdPlugin)

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
  "com.amazonaws" % "aws-java-sdk-sts" % "1.11.208",
  "com.amazonaws" % "aws-java-sdk-sns" % "1.11.210"
)

//logging
libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.9.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.9.1",
  "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.7.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.4"
)

debianPackageDependencies := Seq("openjdk-8-jre-headless")
serverLoading in Debian := Some(ServerLoader.Systemd)
serviceAutostart in Debian := false

version in Debian := s"${version.value}-${sys.env.getOrElse("CIRCLE_BUILD_NUM","SNAPSHOT")}"
name in Debian := "cloudtrail-lambda"

maintainer := "Andy Gallagher <andy.gallagher@theguardian.com>"
packageSummary := ""
packageDescription := """"""
riffRaffPackageType := (packageBin in Debian).value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffManifestBranch := sys.env.getOrElse("CIRCLE_BRANCH","unknown")
riffRaffManifestRevision := sys.env.getOrElse("CIRCLE_BUILD_NUM","SNAPSHOT")
riffRaffManifestVcsUrl := sys.env.getOrElse("CIRCLE_BUILD_URL", "")
riffRaffBuildIdentifier := sys.env.getOrElse("CIRCLE_BUILD_NUM", "SNAPSHOT")
riffRaffPackageName := "cloudtrail-lambda"
riffRaffManifestProjectName := "cloudtrail-lambda"