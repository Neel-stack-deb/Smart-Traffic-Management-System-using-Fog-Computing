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

The stock printer does not say which of the three actuators closed the average. It records the first one that runs and then removes the emit time. This launch does not prove that all three signals received a copy, even though `sendTupleToActuator` forwards an unmatched actuator tuple to every child.

## Deviations from the design note

- No simulator source file was changed.
- `VRGameFog` was not edited. Its run command remains the default.
- The implementation uses the stock `Controller`. There is no custom logger and no CSV output.
- `EXECUTION TIME` is wall-clock milliseconds, here 198 for this launch. It is not simulated time. Simulated time stops at 1000.
- The printed CPU delays are the stock per-type smoother, not an arithmetic mean of every tuple. See `docs/TUPLE_LIFECYCLE_ANALYSIS.md`.
- The second `vmCreate` performed when `LAUNCH_MODULE` is handled did not prevent the run from finishing. Allocation messages from that call are suppressed because `main` calls `Log.disable()` before placement and startup. This launch therefore does not include a line-by-line trace of that second create.

## Known limitations

- One launch only. Do not treat the floating-point lines as a required baseline for later comparisons.
- Loop delays are running means. They are not p95, p99, or per-intersection samples.
- The congestion mean ends when `cloud_analytics` receives `REGIONAL_REPORT`, before that module's CPU time is included.
- Emergency and traffic tuples time-share a module. Nothing in this class prioritizes an emergency.
- There is no deadline, drop reason, or scheduler-overhead log.
- Energy and cloud cost are the stock device accumulators for these assumed power and price parameters.
- The prototype does not move vehicles, change a real signal, or call SUMO.
