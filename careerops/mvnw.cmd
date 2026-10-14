@REM ----------------------------------------------------------------------------
@REM CareerOps Maven Wrapper for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal
set "DIR=%~dp0"
set "MVN_CMD=%DIR%..\.tools\maven\apache-maven-3.9.9\bin\mvn.cmd"
if not exist "%MVN_CMD%" (
    mvn %*
) else (
    "%MVN_CMD%" %*
)
