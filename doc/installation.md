## nisperoCLI


nisperoCLI is a command-line application for working with nispero-based projects: [nispero](https://github.com/ohnosequences/nispero), [metapasta](https://github.com/ohnosequences/metapasta).

### Installation

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

Installation in Windows depends on command interpreter

##### Bash: Git for Windows, MinGW

Download and install [Git for Windows](http://msysgit.github.io/)

Create %USERPROFILE%\bin directory and put it into your %PATH%:

setx PATH "%PATH%;%USERPROFILE%\bin"

###### SBT

> you can skip this stem if SBT already installed in your system, but then you will have to provide correct path to sbt-launch.jar in all nispero.bat script

Put [sbt-launch.jar](http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.1/sbt-launch.jar) into your `%USERPROFILE%\bin` directory.

Create script `%USERPROFILE%\bin\sbt.bat` with following content:

```
SET SBT_OPTS "-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java %SBT_OPTS% -jar %USERPROFILE%\bin\sbt-launch.jar %*
```

###### nisperoCLI

Put [launchconfig](https://raw.githubusercontent.com/ohnosequences/nisperoCLI/master/src/main/conscript/nispero/launchconfig) into 
your `%USERPROFILE%\bin` directory.

Create script `%USERPROFILE%\bin\nispero.bat` with following content:

```
SET SBT_OPTS  "-Xms512M -Xmx536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java %SBT_OPTS% -jar %USERPROFILE%\bin\sbt-launch.jar "@launchconfig" %*
```

##### Power shell, command line

> **warning:** because in a lot of cases conscript doesn't work manual installation with Git for Windows recommended

Lauch [conscript jar](https://github.com/n8han/conscript/releases/download/0.4.4-1/conscript-0.4.4-1.jar) and then in command line type:

```
cs ohnosequences/nisperoCLI
```

### Usage

* `nispero create <organization>/<repository>` – create a new project from template located in github repository
* `nispero create <organization>/<repository>/<tag>` – create a new project from the specific release of template located in github repository
* `nispero configure` – configure AWS account for using nispero-based applications
* `nispero configure bucket` – setup default location for artifacts
