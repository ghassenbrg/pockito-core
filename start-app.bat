@echo off
setlocal enabledelayedexpansion

set PROFILE=%~1
if "%PROFILE%"=="" set PROFILE=dev

echo Starting Pockito Core with profile: %PROFILE%

call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=%PROFILE%