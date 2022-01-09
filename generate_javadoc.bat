@echo off

%~d0
cd %~p0

call env.bat
set JAVA_HOME="%JAVA_HOME_17%"

mkdir target

call mvnw -version > target/javadoc.log 2>&1 
call mvnw javadoc:javadoc >> target/javadoc.log 2>&1 

start target/javadoc.log

