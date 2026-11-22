@echo off
REM ============================================================
REM  Conecta el celular (USB) o el emulador con los microservicios:
REM  "adb reverse" hace que localhost:8080 y localhost:8081 en el telefono
REM  lleguen a los mismos puertos del PC donde corre Spring Boot.
REM  Ejecutalo de nuevo si conectas el celular / abres el emulador despues.
REM ============================================================
set "ADB="
for %%D in ("%ANDROID_HOME%" "%ANDROID_SDK_ROOT%" "%LOCALAPPDATA%\Android\Sdk" "C:\Android\Sdk") do (
  if not defined ADB if exist "%%~D\platform-tools\adb.exe" set "ADB=%%~D\platform-tools\adb.exe"
)
if not defined ADB for /f "delims=" %%A in ('where adb 2^>nul') do if not defined ADB set "ADB=%%A"
if not defined ADB (
    echo [AVISO] No encontre adb. Abre Android Studio, ve a Settings ^> Android SDK y copia la ruta del SDK.
    echo         Luego ejecuta:  set ANDROID_HOME=ruta_del_sdk  y vuelve a abrir este archivo.
    goto fin
)
echo adb: %ADB%
"%ADB%" devices
"%ADB%" reverse tcp:8080 tcp:8080 && echo OK: actividades-service en localhost:8080
"%ADB%" reverse tcp:8081 tcp:8081 && echo OK: estudiantes-service en localhost:8081
:fin
if not "%1"=="nopause" pause
