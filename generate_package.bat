@echo off

%~d0
cd %~p0

call env.bat


rem call mvn clean
rem call mvn compile -Dmaven.test.skip=true
rem call mvn package -Dmaven.test.skip=true
call mvn clean package
rem call mvn source:jar -Dmaven.test.skip=true
rem call mvn javadoc:jar -Dmaven.test.skip=true -Dencoding=UTF-8 -Dcharset=UTF-8 -Ddecoding=UTF-8

pause
