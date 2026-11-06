# Available examples

All of these classes are under `simulator/src/org/fog/test`. Each one listed with a `main` method can be passed to `scripts/run-example.ps1 -MainClass <binary name>` after `scripts/compile.ps1`. Only `org.fog.test.perfeval.VRGameFog` was executed during setup. See `docs/ENVIRONMENT_SETUP.md` for that run.

Descriptions below come from the class comments and the `appId` strings in each file. Where the comment is generic, the file name is the more specific label.

## Executed example: `VRGameFog`

Binary name: `org.fog.test.perfeval.VRGameFog`  
Comment in source: "Simulation setup for case study 1 - EEG Beam Tractor Game" (`Harshit Gupta`).  
Application id: `vr_game`.

### What it models

A headset game. Each smartphone has an EEG sensor and a display actuator. EEG samples are processed by a client module on the phone, a concentration calculator, and a connector that shares game state. The flag `CLOUD` is `false`, so placement is edgeward except for the connector, which `ModuleMapping` pins to the cloud.

### Classes that carry the model

| Class | Role in this example |
| --- | --- |
| `VRGameFog` | Builds devices, the application graph, the mapping, and the controller |
| `FogBroker` | CloudSim user id for the application |
| `Application`, `AppEdge`, `AppLoop`, `FractionalSelectivity` | Dataflow graph and the monitored loop |
| `FogDevice`, `FogDeviceCharacteristics`, `Sensor`, `Actuator` | Physical nodes and IoT endpoints |
| `Tuple` | Unit of data moving along an edge |
| `ModuleMapping`, `ModulePlacementEdgewards`, `Controller` | Placement and the simulation controller |
| `AppModuleAllocationPolicy`, `StreamOperatorScheduler`, `FogLinearPowerModel` | How a device hosts a module and accounts for power |
| `DeterministicDistribution` | EEG inter-transmission interval |
| `CloudSim`, `TimeKeeper`, `Config` | Event loop, delay accounting, stop time |

### Topology constructed by `createFogDevices`

`numOfDepts = 2` and `numOfMobilesPerDept = 5`.

| Device | Parent | `setLevel` argument | Uplink latency | MIPS | RAM | Uplink bandwidth | Downlink bandwidth |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `cloud` | none (`parentId = -1`) | 0 | not set on the cloud | 44800 | 40000 | 100 | 10000 |
| `proxy-server` | `cloud` | 1 | 100 | 2800 | 4000 | 10000 | 10000 |
| `d-0`, `d-1` | `proxy-server` | 1 | 4 | 2800 | 4000 | 10000 | 10000 |
| `m-<dept>-<index>` (10 phones) | the department gateway | 3 | 2 | 1000 | 1000 | 10000 | 270 |

The gateway call passes level `1`, the same value as the proxy. Phones are level `3`. There is no device constructed with level `2`.

Each phone `m-<id>` gets sensor `s-<id>` of type `EEG` and actuator `a-<id>` of type `DISPLAY`. Sensor-to-phone latency is `6.0`. Actuator latency is `1.0`. `EEG_TRANSMISSION_TIME` is `5`, so the sensor uses `new DeterministicDistribution(5)`. Because that value is not `10`, the EEG edge is created with CPU length 3000 and network length 500.

Power model arguments `(busyPower, idlePower)` from the source: cloud `16*103` and `16*83.25`, proxy and gateways `107.339` and `83.4333`, phones `87.53` and `82.44`.

### Application graph

Modules, each added with RAM argument `10`: `client`, `concentration_calculator`, `connector`.

Edges in `createApplication`:

| Source | Destination | Tuple type | Direction | Kind |
| --- | --- | --- | --- | --- |
| `EEG` | `client` | `EEG` | `Tuple.UP` | `AppEdge.SENSOR` |
| `client` | `concentration_calculator` | `_SENSOR` | `Tuple.UP` | `AppEdge.MODULE` |
| `concentration_calculator` | `connector` | `PLAYER_GAME_STATE` | `Tuple.UP` | periodic module edge: periodicity 100, CPU length 1000, network length 1000 |
| `concentration_calculator` | `client` | `CONCENTRATION` | `Tuple.DOWN` | `AppEdge.MODULE` |
| `connector` | `client` | `GLOBAL_GAME_STATE` | `Tuple.DOWN` | periodic module edge: periodicity 100, CPU length 28, network length 1000 |
| `client` | `DISPLAY` | `SELF_STATE_UPDATE` | `Tuple.DOWN` | `AppEdge.ACTUATOR` |
| `client` | `DISPLAY` | `GLOBAL_STATE_UPDATE` | `Tuple.DOWN` | `AppEdge.ACTUATOR` |

The periodic overload is `addAppEdge(source, destination, periodicity, tupleCpuLength, tupleNwLength, tupleType, direction, edgeType)`. The inline comments in `VRGameFog` say `period=1000ms`, but the third argument of those calls is `100`, which is the periodicity parameter.

Selectivity: client emits `0.9` `_SENSOR` tuples per `EEG` tuple; the other mappings in the file use `FractionalSelectivity(1.0)`.

Monitored loop: `EEG`, `client`, `concentration_calculator`, `client`, `DISPLAY`.

### Output to expect

`Log.printLine("Starting VRGame...")` runs before `Log.disable()`, so that line appears. Placement and "Creating ..." lines are ordinary standard output. `Controller` then prints the results banner, one loop delay, per-tuple-type CPU delays, energy for every device, cloud cost, and network usage.

`VRGame finished!` does not appear. `Log.disable()` would hide it, and `Controller` calls `System.exit(0)` on `FogEvents.STOP_SIMULATION` before `main` reaches that line. Exit code 0 after the results block is the success signal observed on this machine.

The floating-point metrics varied slightly across two successful launches. The report sections and device names did not. Details and the script transcript are in `docs/ENVIRONMENT_SETUP.md`.

## Other examples (not executed in this setup)

| Class | Source comment or application id | Notes from the source |
| --- | --- | --- |
| `org.fog.test.perfeval.DCNSFog` | "Simulation setup for case study 2 - Intelligent Surveillance". App id `dcns`. | Camera surveillance case study. Same style of `Controller` and edgeward placement as `VRGameFog`. The success line in `main` is the string `VRGame finished!`. |
| `org.fog.test.perfeval.TwoApps` | Comment still says EEG Beam Tractor Game. App ids `vr_game_0` and `vr_game_1`. | Two copies of the game. Uses `ModulePlacementMapping` only. `numOfDepts = 1`. |
| `org.fog.test.CleanFromJson` | "Simulation setup for EEG Beam Tractor Game extracting physical topology". App id `vr_game`. | Loads the physical topology through `JsonToTopology` and `PhysicalTopology` instead of building every device in code. |
| `org.fog.test.perfeval.TranslationServiceFog` | "Simulation setup for IoT Drone" (`Redowan Mahmud`). App id `Translation_Service`. | Builds fog devices and mobile users from dataset files (`createFogDevices` throws `IOException`). |
| `org.fog.test.perfeval.TranslationServiceFog_Clustering` | Comment says IoT drone with random mobility and dynamic clustering. App id `Translation_Service`. | Clustering variant. Opens a dataset reference. |
| `org.fog.test.perfeval.TranslationServiceFog_RandomMobility` | "Simulation setup for IoT Drone With Random Mobility". App id `Translation_Service`. | Mobility variant. |
| `org.fog.test.perfeval.TranslationServiceFog_RandomMobility_Clustering` | "Simulation setup for IoT Drone With Random Mobility & Dynamic Clustering". App id `Translation_Service`. | Named in the upstream README as a starter example. |
| `org.fog.test.perfeval.CardiovascularHealthMonitoringApplication` | Startup text "Starting Cardiovascular Health Monitoring Application...". App id in source is `Cardiovascular Health Monitoring Application CHM)`. | Microservice-style healthcare example (`Samodha Pallewatta`). The closing parenthesis is part of the string in the file. |
| `org.fog.test.perfeval.CardiovascularHealthMonitoringApplicationEdgeward` | Startup text "Starting Cardiovascular Health Monitoring Application...". App id `Cardiovascular Health Monitoring Application`. | Edgeward variant. Reads a dataset reference. |
| `org.fog.test.perfeval.MicroservicesAppSample1` | App id `ECG_monitoring`. | Microservice sample. The `main` success string is `VRGame finished!`. |
| `org.fog.test.perfeval.MicroserviceAppSample2` | "Simulation setup for Microservices Application". | Builds applications from `ApplicationConfig.json` via json-simple. |
| `org.fog.test.perfeval.MicroserviceApp_RandomMobility_Clustering` | "Simulation setup for Microservices Application". `createApplication("example", ...)`. | Mobility and clustering. |
| `org.fog.test.perfeval.CrowdSensing_Microservices_RandomMobility_Clustering` | "Simulation setup for Microservices Application". `createApplication("example", ...)`. | Named in the upstream README as a starter example. |

Topology files that are data, not programs, are under `simulator/topologies/` (`vr_game_topo`, `dcns_game_topo`, `routerTopology`, and copies) and `simulator/src/topologies/routerTopology`.

## How to run one

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\compile.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-example.ps1 -MainClass org.fog.test.perfeval.VRGameFog
```

Run the command from the workspace root. The script changes the working directory to `simulator/` before starting the JVM, which matters for examples that open relative dataset paths.
