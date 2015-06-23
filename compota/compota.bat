SET mypath=%~dp0
echo %mypath:~0,-1%
java -jar %~dp0\sbt-launch.jar "@compota_launchconfig" %*