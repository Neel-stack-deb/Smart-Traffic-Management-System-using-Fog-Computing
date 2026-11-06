# iFogSim concepts used by this checkout

This note describes types that exist in `simulator/src`. It is background for the later traffic-management study. It does not add a traffic model.

## Simulation kernel

iFogSim runs on CloudSim's discrete-event engine.

- `org.cloudbus.cloudsim.core.CloudSim` initialises the simulation (`CloudSim.init`), runs it (`CloudSim.startSimulation`), and stops it (`CloudSim.stopSimulation`). The version string in that class is `"7.0"`.
- `org.cloudbus.cloudsim.core.SimEvent` carries a tag. `getTag()` returns `CloudSimTags`.
- `org.fog.utils.FogEvents` is the enum of iFogSim tags: tuple arrival, module launch, application submit, resource management, stop simulation, mobility, clustering, and related events.
- `org.cloudbus.cloudsim.Log` is the CloudSim logger. `Log.disable()` drops later `Log.print` and `Log.printLine` calls. `System.out.println` in iFogSim classes is unaffected.

Simulated time advances by processing events. It is not the `EXECUTION TIME` line in the results, which `Controller` computes from wall-clock milliseconds.

## Physical topology

A fog device is a CloudSim power datacenter with a parent and uplink latency.

- `org.fog.entities.FogDevice` extends `org.cloudbus.cloudsim.power.PowerDatacenter`.
- Each device in the VR game example is given one `PowerHost`, one processing element (`Pe`) with `PeProvisionerOverbooking`, `RamProvisionerSimple`, `BwProvisionerOverbooking`, `StreamOperatorScheduler`, and `FogLinearPowerModel`.
- `setParentId` and `setUplinkLatency` build the tree. `setLevel` stores the hierarchy level passed by the example.
- `org.fog.entities.FogDeviceCharacteristics` describes architecture, OS, VMM, and cost rates.
- `org.fog.policy.AppModuleAllocationPolicy` allocates application modules onto the device's host list.
- `org.fog.entities.Sensor` emits tuples toward a gateway device. `org.fog.entities.Actuator` receives tuples for an actuator type such as `DISPLAY`.
- `org.fog.utils.distribution.DeterministicDistribution` is the inter-transmission distribution used by the VR game sensors. The same package also contains `NormalDistribution` and `UniformDistribution`.

Northbound traffic moves toward the parent. Southbound traffic moves toward children. `FogDevice` keeps a `northTupleQueue` and a `southTupleQueue`.

## Application model

`org.fog.application.Application` is documented in source as an application in the Distributed Dataflow Model. `Application.createApplication(appId, userId)` returns an empty model. The example then adds:

- modules (`AppModule`), via `addAppModule(name, ram)`
- edges (`AppEdge`) of kind `SENSOR`, `MODULE`, or `ACTUATOR`
- tuple direction `Tuple.UP` or `Tuple.DOWN`
- selectivity, such as `FractionalSelectivity`, which says how many output tuples one input tuple produces
- loops (`AppLoop`), which are module-name paths whose end-to-end delay is averaged by `TimeKeeper`

`org.fog.entities.Tuple` extends `org.cloudbus.cloudsim.Cloudlet`. A tuple has an application id, a tuple type string, a direction, and the module names it is travelling between. CPU length and network length come from the `AppEdge` that created it.

`org.fog.entities.FogBroker` is the CloudSim user that owns the application (`application.setUserId(broker.getId())`).

## Placement and the controller

`org.fog.placement.ModuleMapping` records modules that must run on a named device. In `VRGameFog` with `CLOUD == false`, the mapping fixes `connector` on `cloud`. Other modules are placed by `org.fog.placement.ModulePlacementEdgewards`.

`ModulePlacementEdgewards` starts from the sensors and walks modules toward parents when the current device cannot hold the module. The VR game run printed that behaviour: `concentration_calculator` was placed on `d-0`, then `d-0 is shifting concentration_calculator north`, then `proxy-server is shifting concentration_calculator north`, and the module was created on `cloud`.

Other placement classes in the same package, unused by `VRGameFog`, include `ModulePlacementMapping`, `ModulePlacementOnlyCloud`, `ModulePlacementMobileEdgewards`, `ModulePlacementMobileEdgewardsCluster`, and the microservice placement logic types.

`org.fog.placement.Controller` is a CloudSim entity. On start it schedules `FogEvents.STOP_SIMULATION` at `Config.MAX_SIMULATION_TIME` (2000 in `org.fog.utils.Config`) and sends `FogEvents.RESOURCE_MGMT` to each device. When the stop event arrives it prints loop delays, tuple CPU delays, energy, cloud cost, and network usage, then calls `System.exit(0)`.

Related controllers in the tree: `MobilityController`, `ClusteringController`, `MicroservicesController`, `MicroservicesMobilityClusteringController`.

## Measurements

- `org.fog.utils.TimeKeeper` stores tuple emit times and the running average loop delay (`getLoopIdToCurrentAverage`) and average CPU time per tuple type (`getTupleTypeToAverageCpuTime`).
- `FogDevice.getEnergyConsumption()` is what `Controller.printPowerDetails` prints.
- `NetworkUsageMonitor.getNetworkUsage() / Config.MAX_SIMULATION_TIME` is the printed network usage.
- Cloud cost is `FogDevice.getTotalCost()` for the device named `cloud`.

## iFogSim2 extensions present in this tree

The README lists mobility, microservice orchestration, and dynamic distributed clustering. The classes behind those features are in this checkout:

- Mobility: `org.fog.mobilitydata` (`Location`, `RandomMobilityGenerator`, `DataParser`) and `org.fog.placement.MobilityController`.
- Clustering: `org.fog.mobilitydata.Clustering`, `org.fog.placement.ClusteringController`, and `Config.ENABLE_DYNAMIC_CLUSTERING` (default `true` in `Config.java`). `ENABLE_STATIC_CLUSTERING` defaults to `false`.
- Microservices: `org.fog.entities.MicroserviceFogDevice`, `org.fog.entities.ControllerComponent`, `org.fog.placement.MicroservicesController`, `MicroservicePlacementLogic`, `DistributedMicroservicePlacementLogic`, `ClusteredMicroservicePlacementLogic`.

`VRGameFog` does not use the mobility or microservice controllers. It uses `Controller` and `ModulePlacementEdgewards`.

## Relation to the traffic study

The traffic study will need the same four pieces this example already has: a device tree, sensors and actuators, an application graph of modules and tuple types, and a placement policy. Adaptive placement would be a new `ModulePlacement` (or a change in how `Controller` maps modules), written outside the upstream classes once the experiments start. That policy is not part of this setup.
