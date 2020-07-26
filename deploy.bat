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

mvn -Dmaven.test.skip=true deploy source:jar -Dgpg.passphrase=%1


