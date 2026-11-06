# Environment setup

This file records how the iFogSim checkout in `simulator/` was inspected, compiled, and executed on this machine. No simulator source file was modified.

## What is installed

| Item | Observed value |
| --- | --- |
| Workspace | `SmartTrafficFog` |
| Simulator checkout | `simulator/` |
| Upstream remote | `https://github.com/Cloudslab/iFogSim.git` |
| Branch | `main` |
| Commit | `5f68d3947e450d8d2b4af42670be819206be68c9` |
| `git describe --tags --always` | `v2.0.0-12-g5f68d39` |
| Annotated product name | iFogSim2, from `simulator/README.md` |
| Embedded CloudSim version string | `7.0`, the constant `CLOUDSIM_VERSION_STRING` in `simulator/src/org/cloudbus/cloudsim/core/CloudSim.java` |
| JDK on PATH | Java 22.0.2 (`C:\Program Files\Java\jdk-22`) |
| `JAVA_HOME` | unset |
| Build tool in the upstream tree | none |

The upstream README does not name a Java release. The sources do not compile as Java 17: `javac --release 17` fails in `Cloudlet.java` because `List.getLast()` is missing. That method exists from Java 21. Compilation and execution were done with JDK 22.0.2, targeting release 21, so the class files remain runnable on JDK 21 and newer.

A gitignored IntelliJ file, `simulator/.idea/misc.xml`, records `languageLevel="JDK_23"`. JDK 23 is not installed on this machine. JDK 22.0.2 is sufficient.

## Build system

There is no `pom.xml`, `build.gradle`, or `build.xml`. Dependencies are the JAR files under `simulator/jars`, added to the classpath by hand. CloudSim itself is source inside `simulator/src/org/cloudbus`, not a separate JAR.

| JAR | Path |
| --- | --- |
| commons-io 2.17.0 | `simulator/jars/commons-io-2.17.0.jar` |
| commons-lang3 3.17.0 | `simulator/jars/commons-lang3-3.17.0.jar` |
| commons-math3 3.5 | `simulator/jars/commons-math3-3.5/commons-math3-3.5.jar` |
| commons-net 3.11.1 | `simulator/jars/commons-net-3.11.1.jar` |
| easymock 5.3.0 | `simulator/jars/easymock-5.3.0.jar` |
| geoip2 4.2.0 | `simulator/jars/geoip2-4.2.0.jar` |
| guava 18.0 | `simulator/jars/guava-18.0.jar` |
| json-simple 1.1.1 | `simulator/jars/json-simple-1.1.1.jar` |
| junit-jupiter-engine 5.11.3 | `simulator/jars/junit-jupiter-engine-5.11.3.jar` |
| opencsv 5.9 | `simulator/jars/opencsv-5.9.jar` |
| uncommons-maths 1.2.3 | `simulator/jars/uncommons-maths-1.2.3.jar` |

`commons-math3` sits in a nested directory. A classpath that only lists `simulator/jars/*.jar` will miss it. The compile script searches recursively.

## Configuration added for Cursor

These files live in the research repository, outside the upstream tree:

- `.vscode/settings.json` tells the Java language server that sources are `simulator/src`, libraries are `simulator/jars/**/*.jar`, and output is `build/classes`. It points the language server at `C:\Program Files\Java\jdk-22` on this machine. Change that path if the JDK is installed elsewhere.
- `scripts/compile.ps1` compiles every `*.java` file under `simulator/src` with `javac --release 21`. Output goes to `build/classes`, which is gitignored.
- `scripts/run-example.ps1` runs a main class with that classpath. The process working directory is `simulator/`, because several examples open `dataset/` and topology files with a relative path.

Upstream `simulator/.gitignore` already ignores `.idea/`, `out/`, and `*.iml`. Those local IDE files were left in place and were not edited.

## Compile

From the workspace root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\compile.ps1
```

Observed result on 6 November 2026, JDK 22.0.2:

- 445 source files
- 11 JARs
- exit code 0
- two warnings in `org.fog.entities.ControllerComponent` for the `Integer(int)` constructor, which Java 22 marks for removal
- additional notes that some files use deprecated or unchecked APIs

The warnings were left as they are. Changing them would edit upstream source, and they do not stop compilation.

## Run the checked example

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-example.ps1
```

That defaults to `org.fog.test.perfeval.VRGameFog`. Another example:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-example.ps1 -MainClass org.fog.test.perfeval.DCNSFog
```

`DCNSFog` was not executed during this setup. Only `VRGameFog` was.

`Controller.processEvent` calls `System.exit(0)` when it handles `FogEvents.STOP_SIMULATION`. A successful run therefore ends the JVM from inside the simulation, after the results are printed. The line `VRGame finished!` in `main` is not reached, and `Log.disable()` would suppress it anyway.

## Verified VRGameFog execution

Command: `scripts/run-example.ps1`  
JDK: 22.0.2  
Class files: `javac --release 21`  
Exit code: 0  
Date: 6 November 2026

The process printed `Starting VRGame...`, placement lines for every `m-*` client module, `Creating connector on device cloud`, `Creating concentration_calculator on device cloud`, `0.0 Submitted application vr_game`, then the results block below.

```text
=========================================
============== RESULTS ==================
=========================================
EXECUTION TIME : 561
=========================================
APPLICATION LOOP DELAYS
=========================================
[EEG, client, concentration_calculator, client, DISPLAY] ---> 229.28720172748376
=========================================
TUPLE CPU EXECUTION DELAY
=========================================
PLAYER_GAME_STATE ---> 0.6688474593380255
EEG ---> 7.128362658349889
CONCENTRATION ---> 1.7659053791902617
_SENSOR ---> 2.333750493876913
GLOBAL_GAME_STATE ---> 3.515693232431945
=========================================
cloud : Energy Consumed = 3246373.8928571185
proxy-server : Energy Consumed = 166866.59999999995
d-0 : Energy Consumed = 166866.59999999995
m-0-0 : Energy Consumed = 174652.8712599998
m-0-1 : Energy Consumed = 174546.58187999908
m-0-2 : Energy Consumed = 174591.71999999965
m-0-3 : Energy Consumed = 174513.3569049996
m-0-4 : Energy Consumed = 174566.26363749916
d-1 : Energy Consumed = 166866.59999999995
m-1-0 : Energy Consumed = 174697.49274499956
m-1-1 : Energy Consumed = 174715.48843999975
m-1-2 : Energy Consumed = 174685.1520399993
m-1-3 : Energy Consumed = 174614.0040199996
m-1-4 : Energy Consumed = 174524.56253999958
Cost of execution in cloud = 825644.0000000246
Total network usage = 197268.0
```

`EXECUTION TIME` is wall-clock milliseconds. `Controller.printTimeDetails` computes it as `Calendar.getInstance().getTimeInMillis()` minus the value stored by `TimeKeeper.setSimulationStartTime`. It is not simulated time. Simulated time stops at `Config.MAX_SIMULATION_TIME`, which is `2000`.

An earlier launch of the same class, compiled without `--release 21`, also exited 0 and printed the same sections and the same device names. The numeric loop delay, CPU delays, energy, cloud cost, and network usage were close but not identical (loop delay `229.25181791814904`, cloud cost `826574.7200000245`, network usage `198046.0`). Treat a successful run as exit code 0 plus this report shape. Do not treat one launch's floating-point lines as a unique expected value.

## What was not changed

- No file under `simulator/src` was edited.
- No JAR was added or removed.
- Maven was not introduced.
- The smart-traffic application was not implemented.
