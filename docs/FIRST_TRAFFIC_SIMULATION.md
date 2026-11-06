# First traffic simulation

Date of the recorded run: 6 November 2026.  
Class: `org.traffic.prototype.TrafficManagementPrototype`  
Simulator commit: `5f68d3947e450d8d2b4af42670be819206be68c9`  
JDK: 22.0.2, compiled with `javac --release 21`.

This is one successful launch of the first-review prototype. It does not implement EAAS. The device sizes, tuple lengths, and sensor periods are the assumptions in `docs/TRAFFIC_APPLICATION_DESIGN.md`. They are not measured traffic values.

## Topology

```text
cloud
  └── regional-fog
        ├── intersection-0
        ├── intersection-1
        └── intersection-2
```

| Device | MIPS | RAM | Uplink bandwidth | Downlink bandwidth | Uplink latency | Parent |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| `cloud` | 44800 | 40000 | 100 | 10000 | 0 | none |
| `regional-fog` | 8000 | 8000 | 10000 | 10000 | 100 | `cloud` |
| `intersection-0` | 3000 | 4000 | 10000 | 10000 | 4 | `regional-fog` |
| `intersection-1` | 3000 | 4000 | 10000 | 10000 | 4 | `regional-fog` |
| `intersection-2` | 2000 | 4000 | 10000 | 10000 | 6 | `regional-fog` |

Cloud busy/idle power is `16 * 103` and `16 * 83.25` (1648 W and 1332 W). Regional power is 200 / 100. Each intersection is 150 / 90. Cloud `ratePerMips` is 0.01. The others are 0. Host storage is 1000000 and host bandwidth is 10000, copied from the `VRGameFog` device constructor. The scheduling interval passed to `FogDevice` is 10.

The device list passed to `Controller` is `cloud`, `regional-fog`, `intersection-0`, `intersection-1`, `intersection-2`.

## Modules, sensors, and actuators

Modules use `addAppModule(name, 10)`:

- `intersection_processor` on `intersection-0`, `intersection-1`, and `intersection-2`
- `regional_coordinator` on `regional-fog`
- `cloud_analytics` on `cloud`

Placement is `ModuleMapping` plus `ModulePlacementMapping`. `ModulePlacementEdgewards` is not used.

| Endpoint | Type | Gateway | Distribution or latency |
| --- | --- | --- | --- |
| `traffic-0` | `TRAFFIC_SAMPLE` | `intersection-0` | `DeterministicDistribution(20)`, latency 1 |
| `traffic-1` | `TRAFFIC_SAMPLE` | `intersection-1` | `DeterministicDistribution(20)`, latency 1 |
| `traffic-2` | `TRAFFIC_SAMPLE` | `intersection-2` | `DeterministicDistribution(20)`, latency 1 |
| `emergency-0` | `EMERGENCY_EVENT` | `intersection-0` | `DeterministicDistribution(200)`, latency 1 |
| `signal-0` | actuator type `SIGNAL` | `intersection-0` | latency 1 |
| `signal-1` | actuator type `SIGNAL` | `intersection-1` | latency 1 |
| `signal-2` | actuator type `SIGNAL` | `intersection-2` | latency 1 |

`Config.MAX_SIMULATION_TIME` is set to 1000 in `main` before `CloudSim.startSimulation`.

## Time units

Two clocks appear in the output. They are not the same quantity.

**Simulated time** is `CloudSim.clock()`. In this CloudSim 7 pin the CPU scheduler treats that clock as seconds. `Cloudlet` documents length in million instructions and gives the example 10000 MI on 2000 MIPS takes 5 seconds (`simulator/src/org/cloudbus/cloudsim/Cloudlet.java`, lines 75–83). `Consts.MINUTE` is 60 (`Consts.java`, lines 35–36). `CloudletScheduler.getEstimatedFinishTime` adds `remainingMi / allocatedMips` to the current clock (`CloudletScheduler.java`, lines 360–362). `updateCloudletsProcessing` multiplies the time span by allocated MIPS and by `Consts.MILLION` (`CloudletScheduler.java`, lines 92–97); `getRemainingCloudletLength` divides by the same million (`Cloudlet.java`, lines 1207–1214), so the million cancels and the clock step stays in seconds.

iFogSim schedules network and device delays on that same clock. `FogDevice.sendUpFreeLink` uses `fileSize / uplinkBandwidth + uplinkLatency` with no unit conversion (`FogDevice.java`, lines 864–867). Sensor latency and `DeterministicDistribution` values are event delays in the same unit. `MAX_SIMULATION_TIME` is the simulated time of `STOP_SIMULATION`.

On that reading, the constants in this class are already in simulated seconds: regional uplink latency 100, intersection latencies 4 and 6, sensor latency 1, traffic period 20, emergency period 200, and a stop at 1000. A 3000 MI tuple on an unshared 3000 MIPS PE takes 1 simulated second. The recorded congestion mean, about 111.5, is mostly the 100-second cloud hop. The recorded emergency mean, about 31, does not cross that hop. Neither number is a real-world response time, and neither is a claim that the prototype meets an emergency deadline. The values were not divided or rescaled. Doing so would change the loop means and would no longer describe this run.

**Wall-clock time** is the `EXECUTION TIME` line. `Controller` subtracts two `Calendar` millisecond stamps (`Controller.java`, line 149). The first recorded launch printed 198, and the validation rerun printed 129. Both are milliseconds of real JVM time. They are not simulated time, and they are not loop delays. The simulated loop, CPU, energy, cost, and network lines were the same on both launches. Loop delays are differences of `CloudSim.clock()` and stay in simulated seconds.

## Application graph

| Source | Destination | Tuple type | Direction | Kind | CPU length | Network length |
| --- | --- | --- | --- | --- | ---: | ---: |
| `TRAFFIC_SAMPLE` | `intersection_processor` | `TRAFFIC_SAMPLE` | `Tuple.UP` | `SENSOR` | 3000 | 500 |
| `EMERGENCY_EVENT` | `intersection_processor` | `EMERGENCY_EVENT` | `Tuple.UP` | `SENSOR` | 2000 | 200 |
| `intersection_processor` | `regional_coordinator` | `INTERSECTION_STATUS` | `Tuple.UP` | `MODULE` | 2000 | 800 |
| `intersection_processor` | `regional_coordinator` | `EMERGENCY_ALERT` | `Tuple.UP` | `MODULE` | 1500 | 400 |
| `regional_coordinator` | `cloud_analytics` | `REGIONAL_REPORT` | `Tuple.UP` | `MODULE` | 3000 | 1200 |
| `regional_coordinator` | `SIGNAL` | `SIGNAL_COMMAND` | `Tuple.DOWN` | `ACTUATOR` | 100 | 300 |

Each of the four selectivity mappings uses `FractionalSelectivity(1.0)`:

- `TRAFFIC_SAMPLE` produces `INTERSECTION_STATUS`
- `EMERGENCY_EVENT` produces `EMERGENCY_ALERT`
- `INTERSECTION_STATUS` produces `REGIONAL_REPORT`
- `EMERGENCY_ALERT` produces `SIGNAL_COMMAND`

Loops passed to `application.setLoops`:

1. `TRAFFIC_SAMPLE`, `intersection_processor`, `regional_coordinator`, `cloud_analytics`
2. `EMERGENCY_EVENT`, `intersection_processor`, `regional_coordinator`, `SIGNAL`

## How to compile and run

From the workspace root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\compile.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-example.ps1 -MainClass org.traffic.prototype.TrafficManagementPrototype
```

`compile.ps1` compiles `simulator/src` and `src` with `javac --release 21` into `build/classes`. The default of `run-example.ps1` is still `org.fog.test.perfeval.VRGameFog`. The script looks for the class file of the requested main class and compiles if that file is missing.

`Controller` calls `System.exit(0)` after printing results, so `TrafficManagementPrototype finished!` is not printed. Exit code 0 after the results block is the completion signal.

## Observed console output

Command: `scripts/run-example.ps1 -MainClass org.traffic.prototype.TrafficManagementPrototype`  
Exit code: 0

```text
Starting TrafficManagementPrototype...
Creating intersection_processor on device intersection-1
Creating cloud_analytics on device cloud
Creating intersection_processor on device intersection-0
Creating regional_coordinator on device regional-fog
Creating intersection_processor on device intersection-2
0.0 Submitted application traffic_management
=========================================
============== RESULTS ==================
=========================================
EXECUTION TIME : 198
=========================================
APPLICATION LOOP DELAYS
=========================================
[TRAFFIC_SAMPLE, intersection_processor, regional_coordinator, cloud_analytics] ---> 111.54309333333323
[EMERGENCY_EVENT, intersection_processor, regional_coordinator, SIGNAL] ---> 31.024166666666602
=========================================
TUPLE CPU EXECUTION DELAY
=========================================
EMERGENCY_EVENT ---> 10.666666666666636
INTERSECTION_STATUS ---> 3.2500003767013554
REGIONAL_REPORT ---> 0.06696428571433444
TRAFFIC_SAMPLE ---> 1.2857165308225742
EMERGENCY_ALERT ---> 10.1875
=========================================
cloud : Energy Consumed = 1337166.3178571474
regional-fog : Energy Consumed = 149634.25
intersection-0 : Energy Consumed = 103920.0
intersection-1 : Energy Consumed = 101600.0
intersection-2 : Energy Consumed = 104430.0
Cost of execution in cloud = 7324.40000000143
Total network usage = 17349.6
```

The block above is the process output in print order. `ModulePlacementMapping.mapModules` iterates a `HashMap`, so the `Creating` lines are not in the order of the `addModuleToDevice` calls. All five placements were present.

## Evidence for the two workflows

Congestion monitoring reached `cloud_analytics`:

- `TRAFFIC_SAMPLE` has a CPU-delay entry, so intersection processors executed sensor tuples.
- `INTERSECTION_STATUS` has a CPU-delay entry, so `regional_coordinator` executed those tuples.
- `REGIONAL_REPORT` has a CPU-delay entry, so `cloud_analytics` executed those tuples.
- The first loop line names `cloud_analytics` and has a finite average, so at least one congestion tuple was closed by `FogDevice.updateTimingsOnReceipt`.

Emergency response reached a signal actuator:

- `EMERGENCY_EVENT` and `EMERGENCY_ALERT` have CPU-delay entries, so the processor and the regional coordinator executed those tuple types.
- `SIGNAL_COMMAND` has no CPU-delay entry. Actuator tuples are handed to `sendTupleToActuator` and are not submitted as cloudlets.
- The second loop line ends at `SIGNAL` and has a finite average. `Actuator.processTupleArrival` is what updates that average, so a `SIGNAL` actuator received the tuple.

The stock application-loop result proves that at least one actuator received a `SIGNAL_COMMAND`. It does not independently prove delivery to all three actuators. `Actuator.processTupleArrival` records the delay for the first actuator that finds the emit time, then deletes that emit time, so later copies do not add another sample and the printer never names `signal-0`, `signal-1`, or `signal-2`. `sendTupleToActuator` still forwards an unmatched actuator tuple to every child. That fan-out is a source-code path. This run did not log each actuator arrival, so the results block alone cannot be cited as proof that every signal received the command.

## Deviations from the design note

- No simulator source file was changed.
- `VRGameFog` was not edited. Its run command remains the default.
- The implementation uses the stock `Controller`. There is no custom logger and no CSV output.
- `EXECUTION TIME` is wall-clock milliseconds. The transcript in this file is the first launch and prints 198. The later validation rerun printed 129. Simulated time is the CloudSim clock and stops at 1000 simulated seconds. See the time-units section above. The loop figures are simulated seconds. They are not an emergency-deadline result.
- The printed CPU delays are the stock per-type smoother, not an arithmetic mean of every tuple. See `docs/TUPLE_LIFECYCLE_ANALYSIS.md`.
- The second `vmCreate` performed when `LAUNCH_MODULE` is handled did not prevent the run from finishing. Allocation messages from that call are suppressed because `main` calls `Log.disable()` before placement and startup. This launch therefore does not include a line-by-line trace of that second create.

## Known limitations

- One launch only. Do not treat the floating-point lines as a required baseline for later comparisons.
- Loop delays are running means. They are not p95, p99, or per-intersection samples.
- The congestion mean ends when `cloud_analytics` receives `REGIONAL_REPORT`, before that module's CPU time is included.
- The emergency loop mean proves one `SIGNAL` actuator received the command. It does not prove that all three actuators did.
- Emergency and traffic tuples time-share a module. Nothing in this class prioritizes an emergency.
- There is no deadline, drop reason, or scheduler-overhead log.
- Energy and cloud cost are the stock device accumulators for these assumed power and price parameters.
- The prototype does not move vehicles, change a real signal, or call SUMO.

## Validation rerun

After the exception-handling change, these commands were run from the workspace root on 6 November 2026. Simulated traffic metrics matched the transcript above. Only the wall-clock `EXECUTION TIME` changed, from 198 to 129.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\compile.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-example.ps1 -MainClass org.traffic.prototype.TrafficManagementPrototype
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-example.ps1
```

| Command | Exit code | What the log showed |
| --- | ---: | --- |
| `scripts\compile.ps1` | 0 | `javac 22.0.2`, 446 sources, 11 jars, the same two `Integer` warnings in `ControllerComponent.java` |
| `run-example.ps1 -MainClass org.traffic.prototype.TrafficManagementPrototype` | 0 | Same five `Creating` lines, both loop means `111.54309333333323` and `31.024166666666602`, same CPU, energy, cost, and network lines. `EXECUTION TIME : 129` |
| `run-example.ps1` (default `VRGameFog`) | 0 | `Submitted application vr_game`, one loop mean `229.25144629509822`, `EXECUTION TIME : 503`, cloud cost `825878.6400000222`, network usage `196145.5` |

`VRGameFog` uses `FractionalSelectivity` values below 1, so its printed means can differ across launches. The traffic application uses `FractionalSelectivity(1.0)`. Its simulated lines matched the earlier traffic launch. The `VRGameFog` figures above are this launch only.
