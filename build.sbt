enablePlugins(RiffRaffArtifact, UniversalPlugin, JDebPackaging, DebianPlugin, JavaServerAppPackaging, SystemdPlugin)

name := "cloudtrail-lambda"

version := "1.0"

scalaVersion := "2.12.3"

// https://mvnrepository.com/artifact/org.jruby/jruby-complete
libraryDependencies += "org.jruby" % "jruby-complete" % "9.1.13.0"
// https://mvnrepository.com/artifact/bsf/bsf
libraryDependencies += "bsf" % "bsf" % "2.4.0"
// https://mvnrepository.com/artifact/commons-logging/commons-logging
libraryDependencies += "commons-logging" % "commons-logging" % "1.2"


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