@echo off

%~d0
cd %~p0

if NOT "%JAVA_HOME_11%" == "" (
    set JAVA_HOME="%JAVA_HOME_11%"
)

if NOT "%M2_HOME%" == "" (
    set M2_HOME="%M2_HOME%"
)

set PATH=%PATH%;%JAVA_HOME%\bin;%M2_HOME%\bin;


call mvn -version

call mvn clean
mkdir target
call mvn site -Dgpg.skip=true > target/site.log 2>&1

start target/site.log
