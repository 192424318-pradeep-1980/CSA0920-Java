@echo off
echo Compiling Candy Match Saga Java Desktop Game...
if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib/*" -d bin ^
 src/main/java/com/candymatch/exceptions/*.java ^
 src/main/java/com/candymatch/*.java ^
 src/main/java/com/candymatch/game/*.java ^
 src/main/java/com/candymatch/candy/*.java ^
 src/main/java/com/candymatch/match/*.java ^
 src/main/java/com/candymatch/ai/*.java ^
 src/main/java/com/candymatch/custom/*.java ^
 src/main/java/com/candymatch/analytics/*.java ^
 src/main/java/com/candymatch/storage/*.java ^
 src/main/java/com/candymatch/ui/*.java

if %errorlevel% equ 0 (
    echo Compilation successful! Run 'run.bat' to start the Candy Match Saga game.
) else (
    echo Compilation failed! Check error messages above.
)
