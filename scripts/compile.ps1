# Compiles the vendored iFogSim sources and the research sources under src/.
# Requires JDK 21 or newer (CloudSim 7 calls List.getFirst/getLast).
# Class files are written to build/classes at the workspace root.
$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$SourceRoots = @(
    (Join-Path $Root "simulator\src"),
    (Join-Path $Root "src")
)
$Out = Join-Path $Root "build\classes"
$SourceList = Join-Path $Root "build\sources.txt"

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw "javac was not found on PATH. Install JDK 21 or newer and reopen the terminal."
}

New-Item -ItemType Directory -Force -Path $Out | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path $SourceList) | Out-Null

$Jars = Get-ChildItem (Join-Path $Root "simulator\jars") -Recurse -Filter *.jar
if ($Jars.Count -eq 0) {
    throw "No JAR files found under simulator/jars."
}

$ClassPath = ($Jars | ForEach-Object { $_.FullName }) -join ";"
$SourceFiles = @()
foreach ($SourceRoot in $SourceRoots) {
    if (Test-Path $SourceRoot) {
        $SourceFiles += Get-ChildItem $SourceRoot -Recurse -Filter *.java
    }
}
$SourceFiles | ForEach-Object { $_.FullName } | Set-Content -Encoding ascii $SourceList

$JavacVersion = (cmd /c "javac -version 2>&1" | Select-Object -First 1)
Write-Host "javac: $JavacVersion"
Write-Host "sources: $((Get-Content $SourceList).Count)"
Write-Host "jars: $($Jars.Count)"

& javac --release 21 -encoding UTF-8 -d $Out -cp $ClassPath "@$SourceList"
if ($LASTEXITCODE -ne 0) {
    throw "javac failed with exit code $LASTEXITCODE"
}

Write-Host "Compiled to $Out"
