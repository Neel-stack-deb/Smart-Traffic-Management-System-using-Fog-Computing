# Runs an iFogSim main class from the compiled classes in build/classes.
# Default: org.fog.test.perfeval.VRGameFog
# The working directory is simulator/, which is the root expected by examples
# that open dataset/ or topologies/ with a relative path.
param(
    [string]$MainClass = "org.fog.test.perfeval.VRGameFog"
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$Classes = Join-Path $Root "build\classes"
$Marker = Join-Path $Classes "org\fog\test\perfeval\VRGameFog.class"

if (-not (Test-Path $Marker)) {
    & (Join-Path $PSScriptRoot "compile.ps1")
}

$Jars = Get-ChildItem (Join-Path $Root "simulator\jars") -Recurse -Filter *.jar
$ClassPath = (@($Classes) + ($Jars | ForEach-Object { $_.FullName })) -join ";"

$JavaVersion = (cmd /c "java -version 2>&1" | Select-Object -First 1)
Write-Host "java: $JavaVersion"
Write-Host "main: $MainClass"
Write-Host "cwd:  $(Join-Path $Root 'simulator')"

Push-Location (Join-Path $Root "simulator")
try {
    & java -cp $ClassPath $MainClass
    $Code = $LASTEXITCODE
}
finally {
    Pop-Location
}

if ($Code -ne 0) {
    throw "Example exited with code $Code"
}

Write-Host "Example exited with code 0"
