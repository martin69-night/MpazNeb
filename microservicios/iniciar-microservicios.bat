@echo off
REM ============================================================
REM  Levanta LOS DOS microservicios de Camino de Estrellas, cada uno en su ventana:
REM    - actividades-service  ->  http://localhost:8080/api/actividades
REM    - estudiantes-service  ->  http://localhost:8081/api/estudiantes
REM  y conecta el celular/emulador por USB (adb reverse) para que la app los vea.
REM  Para detenerlos: cerrar las dos ventanas (o Ctrl+C en cada una).
REM ============================================================
cd /d "%~dp0"

REM --- Buscar Java 17+ (sirve el que trae Android Studio, no hay que instalar nada) ---
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto java_ok
for %%D in (
  "%ProgramFiles%\Android\Android Studio\jbr"
  "%LOCALAPPDATA%\Programs\Android Studio\jbr"
  "%ProgramFiles(x86)%\Android\Android Studio\jbr"
  "C:\Android\Android Studio\jbr"
) do (
  if exist "%%~D\bin\java.exe" (
    set "JAVA_HOME=%%~D"
    goto java_ok
  )
)
where java >nul 2>nul && goto java_path
echo [ERROR] No encontre Java. Instala Android Studio o define JAVA_HOME (Java 17 o superior).
pause
exit /b 1
:java_path
echo Usando el java del PATH.
goto java_fin
:java_ok
echo Java: %JAVA_HOME%
:java_fin

call "%~dp0conectar-celular.bat" nopause

echo.
echo Iniciando microservicios (la primera vez descargan dependencias y pueden tardar unos minutos)...
start "actividades-service (8080)" cmd /k "cd /d "%~dp0actividades-service" && gradlew.bat bootRun"
start "estudiantes-service (8081)" cmd /k "cd /d "%~dp0estudiantes-service" && gradlew.bat bootRun"
echo Listo: se abrieron dos ventanas, una por microservicio.
echo Prueba en el navegador: http://localhost:8080/api/actividades  y  http://localhost:8081/api/estudiantes
pause
