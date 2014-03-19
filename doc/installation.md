## Installation

### Java

jdk7 should be installed, and `java` command should be in the path

### `~/bin` directory

Create `~/bin` directory and put it into your `$PATH`

### SBT

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

### nispero cli

Put [launchconfig](https://github.com/ohnosequences/nisperoCLI/blob/super-cli/src/main/conscript/nispero/launchconfig) into 
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


