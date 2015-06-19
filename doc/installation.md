## Installation

### Java

JDK7 (or higher) should be installed, and `java` command should be in the system path.

### Git
Git should be installed in configured in orde to use private templates.

### Destination directory

Create `$HOME/bin` (`%USERPROFILE%\bin`) directory and put it into your system path.

For Windows: `setx PATH "%PATH%;%USERPROFILE%\bin"`

### SBT launch

Put [sbt-launch.jar](http://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.8/sbt-launch.jar) into
`$HOME/bin` (`%USERPROFILE%\bin` for Windows) directory.


### Compota CLI

Put [compota_launchconfig](https://raw.githubusercontent.com/ohnosequences/compotaCLI/master/compota_launchconfig) into
your `$HOME/bin` (`%USERPROFILE%\bin`) directory.

Create a script `$HOME/bin/compota` (`%USERPROFILE%\bin\compota.bat`) with the following content:

For Linux/MacOS X:

```
java -jar `dirname $0`/sbt-launch.jar "@compota_launchconfig" "$@"
```

For Windows

```
java -jar %USERPROFILE%\bin\sbt-launch.jar "@compota_launchconfig" %*
```

