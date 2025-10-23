@echo off
echo.
echo ================================================
echo   Family Image Gallery - Starting Application
echo ================================================
echo.

cd /d "%~dp0"

echo Compiling Java files...
javac -encoding UTF-8 -source 8 -target 8 -d target/classes src/main/java/com/familymedia/imagegallery/SimpleLogin.java src/main/java/com/familymedia/imagegallery/SimpleGallery.java 2>nul

if %errorlevel% equ 0 (
    echo Compilation successful!
    echo.
    echo Starting Family Gallery...
    echo.
    java -cp target/classes com.familymedia.imagegallery.SimpleLogin
) else (
    echo ERROR: Compilation failed!
    pause
)
