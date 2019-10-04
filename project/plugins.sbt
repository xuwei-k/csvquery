addSbtPlugin("org.scalariform"  % "sbt-scalariform"      % "1.8.3")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.8")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.1.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.4.3")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"        % "1.6.0")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"        % "1.2.7")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
