@echo off

%~d0
cd %~p0

call env.bat

mvn clean deploy

