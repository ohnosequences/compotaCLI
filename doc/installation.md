## Installation

### Preliminaries

#### Java

jdk7 should be installed, and `java` command should be in the path


#### Linux/Mac OS X

##### `~/bin` directory

Create `~/bin` directory and put it into your `$PATH`

##### `~/SBT

> you can skip this stem if SBT already installed in your system, but then you will have to provide correct path to sbt-launch.jar in nispero script

Put [sbt-launch.jar](http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.1/sbt-launch.jar) into you `~/bin` directory.

Create script `~/bin/sbt` with following content:

```
SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java $SBT_OPTS -jar `dirname $0`/sbt-launch.jar "$@"
```

Make it executable:

```
chmod +x ~/bin/sbt
```

##### `~/nispero cli

Put [launchconfig](https://raw.githubusercontent.com/ohnosequences/nisperoCLI/super-cli/src/main/conscript/nispero/launchconfig) into 
your `~/bin` directory.

Create script `~/bin/nispero` with following content:

```
SBT_OPTS="-Xms512M -Xmx536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java $SBT_OPTS -jar `dirname $0`/sbt-launch.jar "@launchconfig" "$@"
```

Make it executable:

```
chmod +x ~/bin/nispero
```


#### Windows

> It is highly recommended to install [Git for Windows](http://msysgit.github.io/) and then use installation instructions from Linux/Mac OS X section.


##### `%USERPROFILE%\bin` directory

Create `%USERPROFILE%\bin` directory and put it into your `%PATH%`:

```
setx PATH "%PATH%;%USERPROFILE%\bin"
```

##### SBT

> you can skip this stem if SBT already installed in your system, but then you will have to provide correct path to sbt-launch.jar in all nispero.bat script

Put [sbt-launch.jar](http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.1/sbt-launch.jar) into your `%USERPROFILE%\bin` directory.

Create script `%USERPROFILE%\bin\sbt.bat` with following content:

```
SET SBT_OPTS "-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java %SBT_OPTS% -jar %USERPROFILE%\bin\sbt-launch.jar %*
```

##### nispero cli

Put [launchconfig](https://raw.githubusercontent.com/ohnosequences/nisperoCLI/super-cli/src/main/conscript/nispero/launchconfig) into 
your `%USERPROFILE%\bin` directory.

Create script `%USERPROFILE%\bin\nispero.bat` with following content:

```
SET SBT_OPTS  "-Xms512M -Xmx536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
java %SBT_OPTS% -jar %USERPROFILE%\bin\sbt-launch.jar "@launchconfig" %*
```



