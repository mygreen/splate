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


call mvn clean
call mvn compile -Dmaven.test.skip=true
call mvn package -Dmaven.test.skip=true
call mvn source:jar -Dmaven.test.skip=true
call mvn javadoc:jar -Dmaven.test.skip=true -Dencoding=UTF-8 -Dcharset=UTF-8 -Ddecoding=UTF-8
rem mvn javadoc:javadoc -Dencoding=UTF-8 -Dcharset=UTF-8 -Ddecoding=UTF-8 -Dmaven.test.skip=true

pause
