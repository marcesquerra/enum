import _root_.sbt.Keys._
import _root_.sbt.Project
import _root_.sbtrelease.ReleaseStateTransformations
import _root_.sbtrelease.ReleaseStateTransformations._
import sbtrelease._
import ReleaseStateTransformations._
import ReleaseKeys._
import xerial.sbt.Sonatype.SonatypeKeys
import com.typesafe.sbt.SbtGit.{GitKeys => git}

site.settings

ghpages.settings

site.includeScaladoc()

val nameLiteral = "enum"

organization := s"com.bryghts.${nameLiteral.toLowerCase}"

git.gitRemoteRepo := s"git@github.com:marcesquerra/$nameLiteral.git"

name := nameLiteral

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.10.4", "2.11.6")

publishMavenStyle := true

sonatypeProfileName  := "com.bryghts"

libraryDependencies ++= Seq(
    "org.scala-lang" %  "scala-reflect"      % scalaVersion.value,
    "org.specs2"     %% "specs2-scalacheck"  % "2.4.17"       % "test"
)

libraryDependencies := {
    CrossVersion.partialVersion(scalaVersion.value) match {
        // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
            libraryDependencies.value
        // in Scala 2.10, quasiquotes are provided by macro paradise
        case Some((2, 10)) =>
            libraryDependencies.value ++ Seq(
                compilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full),
                "org.scalamacros" %% "quasiquotes" % "2.0.0" cross CrossVersion.binary)
    }
}

publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
    <url>http://www.brights.com</url>
        <licenses>
            <license>
                <name>mit</name>
            </license>
        </licenses>
        <scm>
            <url>git@github.com:marcesquerra/{nameLiteral}.git</url>
            <connection>scm:git:git@github.com:marcesquerra/{nameLiteral}.git</connection>
        </scm>
        <developers>
            <developer>
                <name>Marc Esquerrà i Bayo</name>
                <email>esquerra@bryghts.com</email>
            </developer>
        </developers>
    )


releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,                    // : ReleaseStep
    inquireVersions,                              // : ReleaseStep
    runClean,                                     // : ReleaseStep
    runTest,                                      // : ReleaseStep
    setReleaseVersion,                            // : ReleaseStep
    commitReleaseVersion,                         // : ReleaseStep, performs the initial git checks
    tagRelease,                                   // : ReleaseStep
    ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
    setNextVersion,                               // : ReleaseStep
    commitNextVersion,                            // : ReleaseStep
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges                                   // : ReleaseStep, also checks that an upstream branch is properly configured
)




