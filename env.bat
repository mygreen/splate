@echo off

if NOT "%JAVA_HOME_11%" == "" (
    set JAVA_HOME="%JAVA_HOME_11%"
)

set PATH=%PATH%;%JAVA_HOME%\bin;%M2_HOME%\bin;

