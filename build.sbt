import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val catsV = "2.7.0"
val catsEffectV = "2.1.4"
val mulesV = "0.4.1"
val redis4catsV = "0.10.3"

val specs2V = "4.10.0"

val kindProjectorV = "0.11.0"
val betterMonadicForV = "0.3.1"

// Projects
lazy val `mules-redis` = project.in(file("."))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .aggregate(core,site)

lazy val core = project.in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "mules-redis"
  )

lazy val site = project.in(file("site"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)
  .dependsOn(core)
  .settings{
    import microsites._
    Seq(
      micrositeName := "mules-redis",
      micrositeDescription := "A Cache Implementation Backed by Redis",
      micrositeAuthor := "Christopher Davenport",
      micrositeGithubOwner := "ChristopherDavenport",
      micrositeGithubRepo := "mules-redis",
      micrositeBaseUrl := "/mules-redis",
      micrositeDocumentationUrl := "https://www.javadoc.io/doc/io.chrisdavenport/mules-redis_2.12",
      micrositeGitterChannelUrl := "ChristopherDavenport/libraries", // Feel Free to Set To Something Else
      micrositeFooterText := None,
      micrositeHighlightTheme := "atom-one-light",
      micrositePalette := Map(
        "brand-primary" -> "#3e5b95",
        "brand-secondary" -> "#294066",
        "brand-tertiary" -> "#2d5799",
        "gray-dark" -> "#49494B",
        "gray" -> "#7B7B7E",
        "gray-light" -> "#E5E5E6",
        "gray-lighter" -> "#F4F3F4",
        "white-color" -> "#FFFFFF"
      ),
      micrositeCompilingDocsTool := WithMdoc,
      scalacOptions in Tut --= Seq(
        "-Xfatal-warnings",
        "-Ywarn-unused-import",
        "-Ywarn-numeric-widen",
        "-Ywarn-dead-code",
        "-Ywarn-unused:imports",
        "-Xlint:-missing-interpolator,_"
      ),
      micrositePushSiteWith := GitHub4s,
      micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
      micrositeExtraMdFiles := Map(
          file("CODE_OF_CONDUCT.md")  -> ExtraMdFileConfig("code-of-conduct.md",   "page", Map("title" -> "code of conduct",   "section" -> "code of conduct",   "position" -> "100")),
          file("LICENSE")             -> ExtraMdFileConfig("license.md",   "page", Map("title" -> "license",   "section" -> "license",   "position" -> "101"))
      )
    )
  }

// General Settings
lazy val commonSettings = Seq(
  scalaVersion := "2.13.8",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.15"),

  addCompilerPlugin("org.typelevel" %% "kind-projector" % kindProjectorV cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % betterMonadicForV),

  libraryDependencies ++= Seq(
    "org.typelevel"               %% "cats-core"                  % catsV,
    "org.typelevel"               %% "cats-effect"                % catsEffectV,
    "io.chrisdavenport"           %% "mules"                      % mulesV,
    "dev.profunktor"              %% "redis4cats-effects"         % redis4catsV,

  
    "org.specs2"                  %% "specs2-core"                % specs2V       % Test,
    "com.codecommit"              %% "cats-effect-testing-specs2" % "0.3.0"       % Test,
    "io.chrisdavenport"           %% "testcontainers-specs2"      % "0.2.0-M2"    % Test,
    "org.specs2"                  %% "specs2-scalacheck"          % specs2V       % Test
  )
)

// General Settings
inThisBuild(List(
  organization := "io.chrisdavenport",
  developers := List(
    Developer("ChristopherDavenport", "Christopher Davenport", "chris@christopherdavenport.tech", url("https://github.com/ChristopherDavenport"))
  ),

  homepage := Some(url("https://github.com/ChristopherDavenport/mules-redis")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),

  pomIncludeRepository := { _ => false},
  scalacOptions in (Compile, doc) ++= Seq(
      "-groups",
      "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
      "-doc-source-url", "https://github.com/ChristopherDavenport/mules-redis/blob/v" + version.value + "€{FILE_PATH}.scala"
  )
))
