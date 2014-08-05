## nisperoCLI


nisperoCLI is a command-line application for working with nispero-based projects: [nispero](https://github.com/ohnosequences/nispero), [metapasta](https://github.com/ohnosequences/metapasta).

### Installation

#### Preliminaries

##### Java

jdk7 should be installed, and `java` command should be in the path


#### Linux/Mac OS 

For these operating system the installation process is easy. you need to install `cs`:

```
curl https://raw.githubusercontent.com/n8han/conscript/master/setup.sh | sh
```

and then install nisperoCLI:

```
cs ohnosequences/nisperoCLI
```

> sometimes you will have to put `~/bin` into `$PATH` otherwise you can use `~/bin/cs` instead `cs` and `~/bin/nispero` instead `nispero`

#### Windows

Installation in Windows depends on command interpretator

##### Bash: Git for Windows, MinGW

Create %USERPROFILE%\bin directory and put it into your %PATH%:

setx PATH "%PATH%;%USERPROFILE%\bin"

##### SBT

> you can skip this stem if SBT already installed in your system, but then you will have to provide correct path to sbt-launch.jar in all nispero.bat script

Put [sbt-launch.jar](http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.1/sbt-launch.jar) into your `%USERPROFILE%\bin` directory.

Create script `%USERPROFILE%\bin\sbt.bat` with following content:

```
SET SBT_OPTS "-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java %SBT_OPTS% -jar %USERPROFILE%\bin\sbt-launch.jar %*
```

##### nispero cli

Put [launchconfig](https://raw.githubusercontent.com/ohnosequences/nisperoCLI/master/src/main/conscript/nispero/launchconfig) into 
your `%USERPROFILE%\bin` directory.

Create script `%USERPROFILE%\bin\nispero.bat` with following content:

```
SET SBT_OPTS  "-Xms512M -Xmx536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java %SBT_OPTS% -jar %USERPROFILE%\bin\sbt-launch.jar "@launchconfig" %*
```

