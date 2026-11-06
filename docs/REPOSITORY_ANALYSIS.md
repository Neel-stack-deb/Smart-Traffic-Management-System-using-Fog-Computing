# Repository analysis

Inspection date: 6 November 2026. The simulator tree was clean (`git status` produced no tracked changes) at commit `5f68d3947e450d8d2b4af42670be819206be68c9` on `main`.

## Identity

`simulator/README.md` titles the project **iFogSim2 (The New Version)**. The only git tag is `v2.0.0`. HEAD is twelve commits after that tag (`v2.0.0-12-g5f68d39`). The history includes commit `f8a6331` with the message `iFogSim integration with cloudsim 7`. `CloudSim.java` sets `CLOUDSIM_VERSION_STRING` to `"7.0"`.

Upstream remote, still stored inside `simulator/`:

```text
origin  https://github.com/Cloudslab/iFogSim.git
```

The research repository records that checkout as a submodule so the Cloudslab history stays attached to the upstream project. Research files (this `docs/` tree, `scripts/`, and `.vscode/settings.json`) belong to `https://github.com/Neel-stack-deb/Smart-Traffic-Management-System-using-Fog-Computing.git`.

License file: `simulator/LICENSE.txt`, MIT, copyright 2025 The Cloud Computing and Distributed Systems (CLOUDS) Laboratory.

## Top-level layout of `simulator/`

| Path | Role |
| --- | --- |
| `src/org/fog` | iFogSim classes (application, entities, placement, mobility, GUI, examples) |
| `src/org/cloudbus/cloudsim` | CloudSim 7 and CloudSimSDN sources compiled with iFogSim |
| `src/images` | GUI images |
| `src/topologies` | topology file `routerTopology` |
| `jars/` | third-party JARs; there is no Maven or Gradle descriptor |
| `dataset/` | sample inputs, including `config.properties` |
| `topologies/` | saved GUI topologies (`vr_game_topo`, `dcns_game_topo`, `routerTopology`, copies) |
| `output/`, `results/` | sample or generated result directories already in the clone |
| `out/` | local IntelliJ production output; gitignored by upstream |
| `.idea/` | local IntelliJ metadata; gitignored by upstream |
| `README.md` | Eclipse and IntelliJ instructions: create a Java project and add the JARs |

Source count at inspection: 445 `.java` files under `simulator/src`.

## `org.fog` packages

Counts are `.java` files in each directory tree:

| Package | Files | Responsibility visible from the package |
| --- | --- | --- |
| `org.fog.application` | 8 | Distributed dataflow application: `Application`, `AppModule`, `AppEdge`, `AppLoop`, selectivity models |
| `org.fog.entities` | 13 | `FogDevice`, `Sensor`, `Actuator`, `Tuple`, `FogBroker`, microservice and controller components |
| `org.fog.placement` | 18 | `Controller` and placement policies, including edgeward, mapping, mobile, and microservice placement |
| `org.fog.mobilitydata` | 6 | Location, random mobility generation, clustering data, parsing |
| `org.fog.utils` | 24 | `Config`, `FogEvents`, `TimeKeeper`, `FogUtils`, distributions, JSON topology loading |
| `org.fog.scheduler` | 2 | `TupleScheduler`, `StreamOperatorScheduler` |
| `org.fog.policy` | 1 | `AppModuleAllocationPolicy` |
| `org.fog.gui` | 34 | Swing topology editor and dialogs |
| `org.fog.test` | 14 | `CleanFromJson` and the `perfeval` examples |

## `org.cloudbus.cloudsim` packages

Directories present under `simulator/src/org/cloudbus/cloudsim`:

`container`, `core`, `distributions`, `EX`, `geolocation`, `lists`, `network`, `power`, `provisioners`, `sdn`, `selectionPolicies`, `util`, `web`.

iFogSim devices are built on this code. `FogDevice` extends `org.cloudbus.cloudsim.power.PowerDatacenter`. Event tags are enums that implement `org.cloudbus.cloudsim.core.CloudSimTags`. `FogEvents` is the iFogSim tag enum.

## How an example is launched upstream

`simulator/README.md` says to import the tree into Eclipse or IntelliJ, attach the JARs, and run a class with a `main` method. The README names `TranslationServiceFog_Clustering` and `CrowdSensing_Microservices_RandomMobility_Clustering` as starting points. Both classes exist under `src/org/fog/test/perfeval`.

This workspace adds a command-line path that uses the same sources and JARs. See `docs/ENVIRONMENT_SETUP.md`.

## Java level required by the sources

The upstream README says "Verify the Java version" and does not give a number. Direct evidence from compilation:

- `javac --release 17` fails on `List.getLast()` in `org.cloudbus.cloudsim.Cloudlet`.
- `javac --release 21` succeeds on JDK 22.0.2.
- Switches such as `switch (ev.getTag())` in `FogDevice` and `Controller` use qualified enum constants (`case FogEvents.TUPLE_ARRIVAL`). `SimEvent.getTag()` returns the interface `CloudSimTags`. That form of switch is part of the Java 21 pattern-matching switch work, which matches the CloudSim 7 style of tag enums.

So the expected language level for this checkout is Java 21 or newer, even though no `pom.xml` states it.
