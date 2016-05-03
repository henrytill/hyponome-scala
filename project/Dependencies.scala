import sbt._

object Dependencies {

  val http4sVersion = "0.14.0a-SNAPSHOT"

  val scalazVersion = "7.2.2"

  val coreDeps = Seq(
    "ch.qos.logback"      % "logback-classic"     % "1.1.3",
    "com.h2database"      % "h2"                  % "1.4.190",
    "com.lihaoyi"        %% "pprint"              % "0.3.8" % "test",
    "com.typesafe"        % "config"              % "1.3.0",
    "com.typesafe.slick" %% "slick"               % "3.1.1",
    "commons-codec"       % "commons-codec"       % "1.10",
    "io.argonaut"        %% "argonaut"            % "6.2-SNAPSHOT" changing(),
    "io.argonaut"        %% "argonaut-scalaz"     % "6.2-SNAPSHOT" changing(),
    "io.argonaut"        %% "argonaut-monocle"    % "6.2-SNAPSHOT" changing(),
    "org.http4s"         %% "http4s-argonaut"     % http4sVersion % "test",
    "org.http4s"         %% "http4s-dsl"          % http4sVersion,
    "org.http4s"         %% "http4s-blaze-server" % http4sVersion,
    "org.http4s"         %% "http4s-blaze-client" % http4sVersion,
    "org.scalacheck"     %% "scalacheck"          % "1.12.5" % "test",
    "org.scalatest"      %% "scalatest"           % "2.2.6"  % "test",
    "org.scalaz"         %% "scalaz-core"         % scalazVersion,
    "org.scalaz"         %% "scalaz-concurrent"   % scalazVersion,
    "org.scalaz.stream"  %% "scalaz-stream"       % "0.8a",
    "org.slf4j"           % "slf4j-api"           % "1.7.5")
}
