# Project Source of Truth --- Emergency-Aware Fog Computing for Intelligent Traffic Management

> **Purpose:** This file is the shared project contract for the human
> team, ChatGPT, and Cursor. It records the current research direction,
> decisions, assumptions, open questions, implementation constraints,
> and next actions.
>
> **Status:** First traffic prototype runs and prints both application loops. EAAS is not started. Per-task latency distributions are not yet collected.
>
> **Last updated:** 2026-11-06\
> **Repository:** `Neel-stack-deb/Smart-Traffic-Management-System-using-Fog-Computing` (`main`)\
> **Working title:** Emergency-Aware Adaptive Task Scheduling in
> Fog-Enabled Intelligent Traffic Management\
> **Short name (provisional):** EAAS --- Emergency-Aware Adaptive
> Scheduler

------------------------------------------------------------------------

## 0. How to use this file

This is the canonical source of truth. Before proposing or implementing
a change, ChatGPT and Cursor must read this file.

### Authority rules

1.  This file describes **current project decisions**, not universal
    facts. Research claims must be supported by sources in the
    literature register.
2.  If code and this file disagree, inspect the code and tests first; do
    not silently assume either is correct. Report the discrepancy and
    update this file after the decision is made.
3.  Do not invent completed work, experimental results, papers, APIs,
    simulator capabilities, or performance improvements.
4.  Distinguish:
    -   **Verified:** checked against primary source, source code, or
        reproducible experiment.
    -   **Provisional:** plausible direction based on the current
        research report; needs verification.
    -   **Open:** not decided.
    -   **Rejected:** explicitly out of scope.
5.  Any implementation milestone is complete only when code, tests,
    experiment evidence (where applicable), and documentation exist.
6.  Keep the simulator's upstream code intact where practical. Prefer a
    separate project layer, adapters, subclasses, and configuration over
    invasive edits to simulator internals.
7.  Every meaningful implementation task should produce:
    -   working code,
    -   tests or a reproducible validation,
    -   a concise `docs/` note explaining the underlying
        systems/research concept,
    -   an update to this file if architecture, status, assumptions, or
        decisions changed.
8.  Never report simulated results as real-world traffic, ambulance,
    safety, or clinical outcomes.

### Required update format

When changing this file, update: - `Last updated` - `Current status` -
`Decisions` - `Milestones` - `Open questions` - `Change log`

Do not delete unresolved questions merely to make the project appear
settled.

------------------------------------------------------------------------

## 1. Project overview

### 1.1 Problem domain

Intelligent traffic-management systems can generate time-sensitive
events from roadside cameras, vehicle detectors, connected vehicles, and
intersection infrastructure. Examples include:

-   emergency-vehicle detection or approach notifications,
-   accident or hazard alerts,
-   traffic-density and congestion estimation,
-   signal-state recommendations,
-   background surveillance analytics and reporting.

Sending every event to a distant cloud can introduce additional network
delay and backhaul traffic. Fog/edge resources near intersections may
process some events closer to their sources. However, fog nodes have
finite and heterogeneous compute/network capacity. Under bursts or
overload, naive placement can still cause queueing, missed deadlines,
and unfair resource use.

### 1.2 Research direction

Study **how task admission, prioritization, and fog/cloud destination
selection affect the service quality of time-critical traffic events
under constrained and changing resources**.

The candidate contribution is an emergency-aware, resource- and
network-aware scheduling policy evaluated in a controlled fog
simulation.

### 1.3 What this project is not (current scope)

-   Not a production traffic-control deployment.
-   Not a computer-vision model training project.
-   Not a claim that the system autonomously controls real traffic
    lights.
-   Not a claim that simulated ambulance response improvements translate
    directly into real-world lives saved.
-   Not initially an RL project or a large multi-objective metaheuristic
    project.
-   Not initially a full V2X/5G protocol simulator.

The first study should isolate **computational task
scheduling/offloading**. Traffic-signal optimization and realistic
vehicle movement are separate concerns.

------------------------------------------------------------------------

## 2. Current status

### 2.1 Overall

| Area | Status | Notes |
|---|---|---|
| Research scan | Initial report available; verification required | Covers fog traffic systems, scheduling, iFogSim/iFogSim2, SUMO, and metrics |
| Research gap | Provisional | Must be checked against closest primary papers before novelty claims |
| Simulator choice | iFogSim2 checkout validated; research feasibility gate open | Pinned upstream submodule; build and untouched example verified. Task-level scheduling hook and metric validity remain to be tested |
| Repository | Initialized and documented | `main` has the setup docs. The prototype, the later docs, and this status file are local and are not yet committed |
| Build environment | Verified | JDK 22.0.2, `javac --release 21`; the last compile included 446 sources and the same two upstream warnings |
| Example execution | Verified | `VRGameFog` remains the default script target. `TrafficManagementPrototype` also exited 0 and printed both traffic loops |
| Architecture | First traffic prototype executed | `TrafficManagementPrototype` places three intersection processors, one regional coordinator, and cloud analytics. Both loops printed. EAAS is not in this run |
| Scheduler | Candidate EAAS only; not started | Provisional hook is a `FogDevice` subclass plus a non-time-shared cloudlet scheduler. Formula not finalized. The review prototype does not include EAAS |
| Baselines | Proposed, not implemented | Cloud-only, static fog, FCFS, priority/EDF variants |
| Experiments | Not started | No project results exist |
| PPT | Upcoming deliverable | Claims must distinguish proposal from results |

### 2.2 Current research report

A Deep Research report titled **"Executive Summary(1)"** is available in
the ChatGPT Library. It is the initial research input for this file.

The report's broad direction is useful, but its bibliographic records,
exact claims, reported percentages, simulator API details, and novelty
statements must be independently verified before they are used in a
paper or presentation. In particular, do not cite the report itself as
primary evidence.

------------------------------------------------------------------------

### 2.3 Decisions recorded

| Decision | Current state | Rationale / condition |
|---|---|---|
| Simulator baseline | iFogSim2 pinned at `5f68d3947e450d8d2b4af42670be819206be68c9` | Build and untouched example are verified; project-specific scheduling and metric feasibility remain open |
| Upstream integrity | Preserve simulator source unchanged by default | Keep research-owned topology, workload, policy, and instrumentation in the research repository unless a measured extension point requires a minimal patch |
| Java compatibility | Compile with `javac --release 21` using JDK 22.0.2 on the inspected machine | Java 17 fails on `List.getLast()`; do not downgrade target to 17 |
| First workload | Build a minimal synthetic traffic-event scenario before EAAS | Establish correct event flow and measurements before adding a new policy |
| Scheduler order | Implement comparable baselines before EAAS | Avoid tuning or evaluating the proposed scheduler against an unvalidated environment |
| SUMO | Optional for the initial scheduling study | Add only if vehicle movement, intersection dynamics, route time, or traffic-light behavior becomes part of the research question |
| Existing smoke test | `VRGameFog` is an environment smoke test only | It validates build/run wiring, not traffic workload behavior or EAAS performance |
| EAAS integration hook | Provisional, not implemented | Per-task decisions belong in a research `FogDevice` subclass at `processTupleArrival`, with a separate cloudlet scheduler for CPU order. Placement stays the static baseline. Validate with a subclass experiment before any upstream edit |
| First-review topology | Implemented and executed once | `org.traffic.prototype.TrafficManagementPrototype` exited 0 on 6 November 2026. The log showed three `intersection_processor` placements, `regional_coordinator`, `cloud_analytics`, and both loop lines. `SIGNAL_COMMAND` is an actuator broadcast. Sizes and intervals remain assumptions. One launch is not a repeated experiment |

These decisions can change only when new source inspection, experiments,
or primary research evidence justify the change. Record the reason in the
change log.

## 3. Verified repository and simulator baseline

This section records the initial Cursor inspection. These are verified
workspace facts as of 6 November 2026; they do not imply that the
proposed traffic application or scheduler has been implemented.

### 3.1 Research repository

- Repository: `Neel-stack-deb/Smart-Traffic-Management-System-using-Fog-Computing`
- Default branch: `main`
- Latest inspected commit: `883ee455d4ba471dc1671b33d2870ebb17c64143`
- iFogSim submodule commit: `5f68d3947e450d8d2b4af42670be819206be68c9`
- Upstream remote inside the submodule: `https://github.com/Cloudslab/iFogSim.git`
- Upstream checkout identifies itself as **iFogSim2 (The New Version)**; `git describe` reports `v2.0.0-12-g5f68d39` and `CloudSim.java` reports CloudSim version `7.0`.

The research repository and upstream simulator are deliberately kept
separate. Do not commit project-specific changes into the simulator
submodule unless a documented extension point makes that unavoidable.

### 3.2 Build environment and reproducibility

- Machine used for validation: Windows, JDK 22.0.2 at `C:\Program Files\Java\jdk-22`.
- `JAVA_HOME` was unset at inspection time.
- There is no Maven or Gradle build descriptor in the upstream checkout.
- The simulator has 445 Java source files and 11 dependency JARs under `simulator/jars`; `commons-math3-3.5.jar` is nested one directory below `jars/`.
- `javac --release 17` fails because `Cloudlet` uses `List.getLast()`. `javac --release 21` succeeds on JDK 22.0.2.
- Compilation completed with exit code 0 and two upstream deprecation/removal warnings for `Integer(int)` in `ControllerComponent`; these warnings were not changed.
- `.vscode/settings.json` points Cursor's Java language server at `simulator/src`, `simulator/jars/**/*.jar`, and the local JDK. Class files are written to ignored `build/classes`.
- `scripts/run-example.ps1` sets the process working directory to `simulator/`, which is required by examples that open relative paths such as `dataset/`.

Reproduce from the research repository root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\compile.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-example.ps1
```

The second command defaults to `org.fog.test.perfeval.VRGameFog`. To run
another discovered example, pass `-MainClass <fully.qualified.ClassName>`.

### 3.3 What the validated example demonstrates

`org.fog.test.perfeval.VRGameFog` is an EEG beam-tractor game example,
not a traffic-management simulation. It creates a hierarchy:

```text
cloud
  └── proxy-server
       ├── d-0
       │    └── 5 phones (EEG sensor + DISPLAY actuator each)
       └── d-1
            └── 5 phones (EEG sensor + DISPLAY actuator each)
```

Its application modules are `client`, `concentration_calculator`, and
`connector`. The monitored loop is:

```text
EEG → client → concentration_calculator → client → DISPLAY
```

`CLOUD` is false in the example; `ModulePlacementEdgewards` places
modules edgeward where possible, while `ModuleMapping` pins `connector`
to the cloud. The example is useful as a first end-to-end reference for
topology construction, sensors/actuators, application graphs, module
placement, simulation control, and standard result reporting.

**Important scheduling distinction:** source inspection now confirms
that `ModulePlacementEdgewards` runs only from its constructor, before
`CloudSim.startSimulation`. Per-tuple device choice on this path is
`FogDevice.processTupleArrival`: execute if the destination module is
local, `sendUp` to the parent, or `sendDown` to every child. CPU order
is `CloudletSchedulerTimeShared` inside the module. Those are three
different mechanisms. The full trace is `docs/TUPLE_LIFECYCLE_ANALYSIS.md`.
Extending a `ModulePlacement` policy is not sufficient for per-task EAAS.

The observed process exits with code 0 and prints the results block.
`Controller` schedules the stop event at `Config.MAX_SIMULATION_TIME`
(2000 in this checkout), prints results, and invokes `System.exit(0)`.
Consequently, the `VRGame finished!` line after `startSimulation()` is
not a reliable completion signal and is not expected to print.

One observed launch reported loop delay `229.28720172748376`, cloud
execution cost `825644.0000000246`, and network usage `197268.0`. A
separate successful launch produced slightly different numeric values.
These are example-run observations only, not golden values or project
results. `EXECUTION TIME` is wall-clock milliseconds, not simulated
time. Do not use these metrics as evidence for the traffic research
hypotheses.

### 3.4 Current repository documentation

The following documents are already committed under `docs/`:

- `docs/ENVIRONMENT_SETUP.md` — environment, compilation, run commands, and observed output.
- `docs/REPOSITORY_ANALYSIS.md` — checkout identity, source tree, packages, and Java compatibility evidence.
- `docs/IFOGSIM_CONCEPTS.md` — CloudSim event kernel, fog topology, application graph, placement, controller, and metric classes.
- `docs/AVAILABLE_EXAMPLES.md` — examples discovered in source, with only `VRGameFog` marked as executed.
- `docs/TUPLE_LIFECYCLE_ANALYSIS.md` — source trace of tuple execution, scheduling, routing, metrics, and provisional EAAS hooks.
- `docs/TRAFFIC_APPLICATION_DESIGN.md` — first-review topology. The placement and both loops were observed in one run; numeric parameters remain assumptions.
- `docs/FIRST_TRAFFIC_SIMULATION.md` — how to run `TrafficManagementPrototype` and the console output of the successful launch.

### 3.5 What this validation does not establish

- No simulator source file was changed during environment setup.
- No smart-traffic application or traffic-event workload has been implemented.
- No EAAS scheduler, custom scheduling policy, or baseline policy has been implemented.
- A candidate per-task hook (`FogDevice.processTupleArrival` plus a custom cloudlet scheduler) has been identified from source. It has not been validated by a subclass experiment, and no scheduler has been implemented.
- The example's application-loop delay is not automatically equivalent to the end-to-end latency definition required by this project.
- Required per-task timestamps, queue wait, deadline misses, fairness, and scheduler overhead have not been validated.
- `DCNSFog` and the other listed examples are discovered but were not executed in this setup.
- SUMO is not installed or integrated as part of this validation.

**Interpretation:** the environment/bootstrap portion of simulator feasibility
has passed. The research-specific simulator feasibility gate remains
open until a small synthetic traffic-event application and its metrics
are validated.

------------------------------------------------------------------------

## 4. Research framing

### 4.1 Candidate problem statement

In a multi-intersection fog-enabled traffic system, heterogeneous
traffic events arrive with different priorities and deadlines. Fog nodes
differ in compute capacity and may experience fluctuating load and
network delay. Static placement or arrival-order scheduling may fail to
protect critical events during bursts.

**We investigate whether a lightweight adaptive scheduling and
offloading policy that accounts for event criticality, deadline slack,
estimated execution time, node load, and communication delay can reduce
emergency-event response latency and deadline misses relative to clearly
defined baseline policies, without unacceptable penalties to energy,
network usage, or background-task performance.**

This statement is intentionally conditional. The experiments must
establish whether the policy actually helps.

### 4.2 Research questions (provisional)

-   **RQ1 --- Critical-task performance:** How do scheduling and
    placement policies affect end-to-end latency and deadline-miss ratio
    for emergency-class tasks?
-   **RQ2 --- Resource trade-offs:** What are the effects on energy
    consumption, network usage, throughput, and resource utilization?
-   **RQ3 --- Load sensitivity:** How does performance change as task
    arrival rate, emergency-event fraction, and fog-node heterogeneity
    increase?
-   **RQ4 --- Robustness:** How does the policy behave under fog
    overload, node unavailability, and network-delay variation?
-   **RQ5 --- Mechanism:** Which policy inputs (priority, deadline
    slack, compute load, network delay) contribute measurable benefit?

### 4.3 Hypotheses to test --- not results

-   **H1:** An adaptive policy can reduce emergency-task p95 latency
    and/or deadline-miss ratio versus FCFS under bursty mixed-priority
    workloads.
-   **H2:** The benefit is largest under moderate-to-high contention,
    not necessarily under light load.
-   **H3:** Aggressive emergency prioritization can harm normal-task
    latency or completion; the policy must expose and measure this
    trade-off.
-   **H4:** Resource- and network-aware destination selection can
    outperform priority-only dispatch when fog nodes are heterogeneous
    or unevenly loaded.

Do not insert numerical improvement targets into the abstract or PPT
until pilot experiments establish realistic effect sizes. Any target
used for planning must be labeled a target, not a result.

------------------------------------------------------------------------

## 5. System model (candidate)

### 5.1 Logical architecture

``` text
Roadside sensors / cameras / vehicle-event sources
                    |
                    v
          Intersection edge gateway
                    |
                    v
           Nearby fog node(s)
          /       |        \
   Neighbor fog  Regional  Local actuator/
      node        fog      decision endpoint
          \       |        /
                    v
               Cloud tier
       (long-horizon analytics / archive)
```

This is a logical model. The actual simulator topology may be simpler. A
proxy tier is optional and should only be included if it supports a
specific research question.

### 5.2 Task classes

  --------------------------------------------------------------------------
  Class         Example                 Priority Deadline      Typical
                                                               treatment
  ------------- -------------- ----------------- ------------- -------------
  Critical      Emergency                   High Tight,        Prefer nearby
                vehicle                          explicitly    feasible fog
                approach /                       modeled       execution
                urgent hazard                                  
                alert                                          

  Important     Accident             Medium-high Bounded       Fog preferred
                confirmation /                                 where
                incident                                       feasible
                coordination                                   

  Routine       Traffic                   Normal Relaxed       Fog or
                density /                                      regional tier
                queue                                          
                estimation                                     

  Background    Aggregation,                 Low Relaxed or no Defer or
                historical                       hard deadline cloud
                analytics                                      
  --------------------------------------------------------------------------

These are workload classes, not claims about real operational deadlines.
Deadline values must be justified and sensitivity-tested.

### 5.3 Task model

Each task should have, at minimum:

-   `taskId`
-   `taskType`
-   `arrivalTime`
-   `inputSizeBytes`
-   `instructionLength` or required MI (depending on simulator model)
-   `priorityClass`
-   `deadline`
-   `sourceDeviceId`
-   `candidateDestinationSet`
-   `estimatedExecutionTime` per candidate node
-   `completionTime`
-   `selectedNodeId`
-   `status`
-   `dropOrFailureReason` (when relevant)

Avoid mixing application-level events, CloudSim/iFogSim tuples, and
scheduling jobs into one ambiguous type. Define adapters if the
simulator's native abstractions differ.

### 5.4 Node model

Each fog/cloud node should expose or provide estimates for:

-   processing capacity (e.g. MIPS),
-   current or estimated CPU demand/queue backlog,
-   memory capacity if modeled,
-   power/energy parameters if modeled,
-   network latency and bandwidth to relevant peers,
-   availability state,
-   tier and location/intersection association.

Do not assume every metric is natively available at the exact
granularity required. Verify the simulator implementation and add
explicit instrumentation where needed.

------------------------------------------------------------------------

## 6. Candidate scheduling policy

### 6.1 Name

**EAAS --- Emergency-Aware Adaptive Scheduler** (working name only).

### 6.2 Intended decision

For each arriving task, select one of: 1. execute on a feasible local
fog node, 2. execute on a feasible neighboring/regional fog node, 3.
offload to cloud, 4. defer in a bounded queue, 5. reject/drop if no
feasible execution can meet policy constraints.

The policy must not silently accept work that cannot be completed within
modeled resource/deadline constraints.

### 6.3 Candidate decision features

-   task priority class,
-   remaining deadline slack,
-   estimated execution time on each candidate node,
-   estimated queueing delay,
-   source-to-node and node-to-actuator communication delay,
-   node utilization/capacity,
-   network congestion or link utilization,
-   task input/output transfer size,
-   optional locality/intersection relevance.

### 6.4 Candidate feasibility calculation

For task `t` and node `n`:

``` text
estimated_finish(t, n) =
    current_time
  + estimated_queue_wait(t, n)
  + estimated_transfer_time(t, n)
  + estimated_execution_time(t, n)
  + estimated_return_or_actuation_time(t, n)

slack(t, n) = deadline(t) - estimated_finish(t, n)
```

A node is deadline-feasible if `slack(t, n) >= 0`, subject to resource
and policy constraints. If no node is feasible, the scheduler must apply
an explicit fallback policy and record the outcome.

### 6.5 Candidate score (not finalized)

A normalized score may combine: - criticality/priority, - deadline
urgency or slack, - predicted completion time, - queue/load pressure, -
communication cost, - energy cost.

Do not use raw quantities with incompatible units in a weighted sum.
Normalize features, define the direction of each term, document weights,
and perform sensitivity analysis. Prefer a lexicographic policy or
explicit feasibility gates if a weighted score obscures safety-critical
priority guarantees.

### 6.6 Required scheduler properties

-   **Priority correctness:** critical tasks must not be accidentally
    ranked below routine tasks due to arbitrary feature scaling.
-   **Bounded starvation:** lower-priority tasks should not be starved
    indefinitely; consider aging or reserved capacity.
-   **Admission control:** define what happens when all candidate nodes
    are overloaded.
-   **Preemption:** do not claim support unless the simulator and
    implementation actually preempt running work. Queue reordering is
    not preemption.
-   **Determinism:** same state and input should produce the same
    decision unless randomness is intentional and seeded.
-   **Observability:** log candidate estimates, selected destination,
    decision reason, and predicted-vs-actual outcome.
-   **Low overhead:** measure scheduler decision time if it is material
    to the simulated system.

------------------------------------------------------------------------

## 7. Baselines and comparison protocol

Implement comparable baselines under the same topology, workload,
resource limits, and random seeds.

  -----------------------------------------------------------------------
  Policy                  Definition              Why it is needed
  ----------------------- ----------------------- -----------------------
  Cloud-only              All eligible processing Establishes centralized
                          sent to cloud           baseline

  Static fog placement    Fixed module/task       Isolates value of
                          placement; no adaptive  dynamic scheduling
                          destination choice      

  FCFS                    Queue/dispatch in       Basic scheduling
                          arrival order           baseline

  Priority-only           Priority class          Isolates priority
                          determines queue order; benefit
                          destination policy held 
                          constant                

  EDF or                  Deadline-based queue    Tests real-time
  least-laxity-first      order                   scheduling alternative

  Resource-aware greedy   Select feasible node    Tests whether
                          using predicted finish  adaptation alone
                          time or load            explains gains

  EAAS                    Candidate proposed      Evaluated only after
                          policy                  definitions and
                                                  baselines are stable
  -----------------------------------------------------------------------

Important: distinguish **module placement** (where an application module
runs) from **per-task scheduling/offloading** (where an individual
task/tuple is processed). They are related but not interchangeable. The
project must state which mechanism it modifies and what the simulator
can actually represent.

------------------------------------------------------------------------

## 8. Metrics and operational definitions

Use a metric dictionary and compute from per-task/per-event logs
wherever possible.

  ------------------------------------------------------------------------
  Metric                  Definition               Reporting
  ----------------------- ------------------------ -----------------------
  End-to-end latency      Completion/actuation     Mean, median, p95, p99;
                          time minus source event  split by task class
                          arrival time             

  Emergency latency       Same measure restricted  Median and tail latency
                          to critical tasks        

  Deadline-miss ratio     Missed eligible tasks /  Per class and overall
                          completed-or-accounted   
                          eligible tasks, with     
                          drop policy explicitly   
                          defined                  

  Throughput              Successfully completed   Tasks/s
                          tasks / observation      
                          interval                 

  Completion ratio        Successfully completed   Per class
                          tasks / generated tasks  

  Drop/rejection ratio    Explicitly rejected or   Per class and reason
                          dropped tasks /          
                          generated tasks          

  Network usage           Bytes transmitted over   Total and by tier/link
                          modeled links            

  Backhaul usage          Bytes crossing           Total and per completed
                          fog-to-cloud or regional task
                          boundary                 

  Energy                  Sum of modeled node      Total and energy/task
                          energy over the          
                          experiment               

  CPU utilization         Busy processing time /   Mean, peak, per node
                          available processing     
                          time, if simulator       
                          supports valid           
                          accounting               

  Queue wait              Execution start minus    Mean and p95 per class
                          task enqueue time        

  Scheduler overhead      Time spent making        Mean and p95, if
                          scheduling decisions     measurable

  Fairness/starvation     Per-class                Report only with a
                          wait/completion          precise definition
                          distribution or          
                          starvation count         
  ------------------------------------------------------------------------

### Measurement cautions

-   Do not equate application-loop latency with per-task execution time.
-   Do not equate simulator event time with wall-clock runtime.
-   Do not infer real traffic waiting time from fog-task latency.
-   Energy results depend on the simulator's power model and
    parameterization; describe them as modeled energy.
-   CPU utilization may require custom instrumentation; confirm the
    simulator's accounting semantics.
-   Record whether failed, rejected, or unfinished tasks enter the
    deadline-miss denominator.

------------------------------------------------------------------------

## 9. Experimental methodology

### 9.1 Initial controlled experiment

Start with a small, debuggable topology: - a handful of
intersection/edge sources, - multiple heterogeneous fog nodes, - one
regional/cloud node, - at least two task classes (critical and
routine), - deterministic task traces first, followed by seeded
stochastic arrivals.

Scale only after the small experiment is correct.

### 9.2 Independent variables

Vary systematically: - number of sources/intersections, - number and
compute capacity of fog nodes, - task arrival rate, - critical-task
fraction and burst size, - task compute demand and input size, -
fog-to-fog and fog-to-cloud latency/bandwidth, - node heterogeneity, -
node failure/unavailability, - queue capacity and admission thresholds.

Use one-factor-at-a-time for initial debugging, then a designed
parameter sweep or factorial design for interaction effects.

### 9.3 Repetitions and statistics

-   Use multiple independent random seeds for stochastic workloads.
-   Start with at least 10 seeds where computationally feasible;
    increase if confidence intervals remain wide.
-   Report sample count, mean, standard deviation, and 95% confidence
    intervals; include median and tail latency for skewed distributions.
-   Use paired comparisons with identical seeds/workloads across
    policies where appropriate.
-   Select statistical tests based on distribution and pairing; do not
    automatically use a t-test.
-   Report effect sizes, not only p-values.
-   Preserve raw per-run data and analysis scripts.

### 9.4 Required scenario set

1.  **Nominal load:** routine traffic plus occasional critical events.
2.  **Emergency burst:** multiple critical tasks arrive close together.
3.  **High background contention:** routine workload saturates local fog
    capacity.
4.  **Heterogeneous fog:** nearby nodes have different processing
    capacities.
5.  **Network degradation:** fog/cloud links experience higher latency
    or lower bandwidth.
6.  **Fog-node failure:** a node becomes unavailable during workload
    execution.
7.  **Scale-up:** increase sources/tasks/nodes while preserving
    documented ratios.

### 9.5 Ablation experiments

If EAAS uses several features, remove one at a time: - no deadline
slack, - no load awareness, - no network awareness, - no priority
differentiation, - no admission control.

This tests whether each added mechanism earns its complexity.

### 9.6 SUMO decision

**Initial recommendation: optional, not a prerequisite for the first
scheduling study.**

First validate the scheduler with synthetic, controlled event streams in
iFogSim/iFogSim2. Add SUMO only if the research question requires
realistic vehicle movement, intersection-level traffic dynamics,
ambulance travel time, or traffic-light interaction.

If SUMO is added: - use it to generate mobility/traffic events, -
document the coupling mechanism and simulation-time synchronization, -
keep traffic outcomes (vehicle delay, queue length, trip time) separate
from compute-system metrics, - do not imply that an iFogSim scheduler
alone optimizes road traffic.

------------------------------------------------------------------------

## 10. Simulator strategy

### 10.1 Candidate platform

iFogSim2 is the current working platform. The research repository pins
the upstream submodule at `5f68d3947e450d8d2b4af42670be819206be68c9`
and the checkout compiles/runs the untouched `VRGameFog` example using
JDK 22.0.2 with `--release 21`.

The platform decision is **provisionally accepted for the next
feasibility stage**, not fully closed. The project-specific scheduling
hook, task-level measurement validity, and minimal custom workload still
need to be demonstrated before treating iFogSim2 as fully validated.

### 10.2 Remaining simulator validation checklist

-   [x] exact repository URL, branch, release/commit, and license;
-   [x] Java/JDK and build-tool requirements;
-   [x] runnable example and expected output;
-   [x] how sensors, tuples, application edges/loops, fog devices,
    actuators, and controllers are represented;
-   [x] where tuple/task destination selection occurs (`FogDevice.processTupleArrival`);
-   [ ] whether a research subclass can use that hook without an upstream edit (not yet executed);
-   [x] how CPU scheduling, queueing, network delay, bandwidth, and energy
    are modeled, with the gaps recorded in `docs/TUPLE_LIFECYCLE_ANALYSIS.md`;
-   [ ] how to collect per-task timestamps and destination decisions (current stores are aggregate only);
-   [x] which inspected iFogSim2 features the `VRGameFog` path actually uses (not mobility, clustering, or microservice routing).

### 10.3 Extension design principles

-   Keep upstream simulator code as a dependency or clean baseline where
    possible.
-   Put project-specific topology, workload generation, scheduling
    policies, instrumentation, and experiment runners in clearly
    separated packages.
-   If a simulator hook requires subclassing or a patch, document the
    exact reason and maintain a minimal patch.
-   Do not assume that a method or feature exists because it appears in
    a paper or tutorial; inspect the checked-out source.
-   Pin the simulator commit and Java version in the repository.

### 10.4 Simulator validation gate

Before implementing EAAS, run a tiny scenario and verify: 1. events are
generated at expected simulation times; 2. tuples reach the intended
modules; 3. execution time responds to configured compute demand; 4.
network delay responds to configured link latency/bandwidth; 5.
actuator/loop timestamps are interpretable; 6. energy/resource metrics
are reproducible; 7. custom per-task logs match simulator events.

If a metric cannot be measured validly, remove it from the primary
claims or implement and validate instrumentation.

------------------------------------------------------------------------

## 11. Repository structure and organization

The current repository is a research workspace built around the pinned
upstream iFogSim2 submodule. Do not introduce Maven/Gradle or relocate
upstream sources merely to match a generic project template.

### 11.1 Existing repository layout

```text
project-root/
├── .gitmodules
├── .gitignore
├── .vscode/
│   └── settings.json
├── PROJECT_STATUS.md
├── simulator/                  # pinned upstream iFogSim2 submodule
├── src/
│   └── org/traffic/prototype/TrafficManagementPrototype.java
├── scripts/
│   ├── compile.ps1
│   └── run-example.ps1
└── docs/
    ├── ENVIRONMENT_SETUP.md
    ├── REPOSITORY_ANALYSIS.md
    ├── IFOGSIM_CONCEPTS.md
    ├── AVAILABLE_EXAMPLES.md
    ├── TUPLE_LIFECYCLE_ANALYSIS.md
    ├── TRAFFIC_APPLICATION_DESIGN.md
    └── FIRST_TRAFFIC_SIMULATION.md
```

### 11.2 Candidate project-specific additions

Add these only as implementation begins and after the actual integration
point is understood:

```text
project-root/
├── src/                         # project-owned Java code/adapters
│   ├── main/java/
│   └── test/java/
├── docs/
│   ├── research/
│   │   ├── literature-register.md
│   │   ├── research-gap.md
│   │   └── simulator-validation.md
│   ├── architecture/
│   │   └── system-model.md
│   ├── concepts/
│   └── decisions/
├── experiments/
│   ├── configs/
│   ├── workloads/
│   ├── runs/
│   └── analysis/
└── results/
    ├── raw/
    ├── processed/
    └── figures/
```

The exact Java source layout and build approach remain open until the
small traffic workload is implemented. Keep project-owned code outside
the simulator submodule where practical. Never commit bulky generated
run outputs without an explicit reason; retain configs, scripts, and
metadata sufficient to reproduce them.

------------------------------------------------------------------------

## 12. Milestones and exit criteria

### M0 --- Research verification

-   [ ] Retrieve and verify primary sources for the closest traffic/fog
    and emergency/fog scheduling studies.
-   [ ] Correct incomplete or questionable bibliographic entries.
-   [ ] Verify the reported algorithms, baselines, metrics, and
    numerical results against original papers.
-   [ ] Build a literature matrix with DOI/URL and evidence notes.
-   [ ] State the gap narrowly and include closest counterexamples.
-   [ ] Decide whether the contribution is scheduling, placement,
    admission control, or a combination.

**Exit criterion:** A defensible problem statement and novelty caveat
supported by verified primary sources.

### M1 --- Simulator feasibility

-   [x] Pin the iFogSim2 submodule at `5f68d3947e450d8d2b4af42670be819206be68c9`.
-   [x] Compile 445 simulator sources with JDK 22.0.2 using `javac --release 21`.
-   [x] Run the untouched `VRGameFog` example successfully (exit code 0).
-   [x] Document the repository, environment, concepts, and available examples.
-   [x] Trace the actual tuple/task lifecycle and identify a provisional per-task hook (`docs/TUPLE_LIFECYCLE_ANALYSIS.md`). The hook is not experimentally validated.
-   [ ] Validate project-relevant latency, compute, network, energy, and queue measurement paths.
-   [x] Implement `org.traffic.prototype.TrafficManagementPrototype` without EAAS. One run exited 0 and printed three `intersection_processor` placements, the regional and cloud modules, and both loop lines.

**Exit criterion:** A reproducible traffic-workload baseline and a
simulator-validation note with validated project-specific metrics. The
build/example/documentation sub-gate is complete; M1 as a whole remains
in progress.

### M2 --- Baselines

-   [ ] Define task schema and workload generator.
-   [ ] Implement cloud-only.
-   [ ] Implement static fog.
-   [ ] Implement FCFS.
-   [ ] Implement priority-only.
-   [ ] Implement EDF or least-laxity-first if supported fairly.
-   [ ] Add per-task structured logging.
-   [ ] Verify policy differences with unit/integration tests.

**Exit criterion:** Baselines run on identical configurations and
produce validated metrics.

### M3 --- EAAS design and implementation

-   [ ] Finalize policy and feasibility constraints.
-   [ ] Normalize score features or choose a lexicographic policy.
-   [ ] Define overload, no-feasible-node, queue-full, and failure
    behavior.
-   [ ] Implement scheduler behind a policy interface.
-   [ ] Add decision-reason logging.
-   [ ] Add unit tests for priority, deadline, overload, and
    tie-breaking behavior.
-   [ ] Add ablation switches.

**Exit criterion:** Deterministic, tested scheduler implementation with
documented policy.

### M4 --- Experimental evaluation

-   [ ] Freeze experiment configs and workload generation.
-   [ ] Run nominal, burst, contention, heterogeneity, network
    degradation, and failure scenarios.
-   [ ] Run multiple seeds and paired policy comparisons.
-   [ ] Save raw per-task and per-run outputs.
-   [ ] Generate plots and confidence intervals.
-   [ ] Run ablations and sensitivity analysis.
-   [ ] Record failed runs and deviations.

**Exit criterion:** Reproducible result set with statistical analysis
and limitations.

### M5 --- Research communication

-   [ ] Finalize related work and references.
-   [ ] Write system model and algorithm.
-   [ ] Explain experimental assumptions and simulator limitations.
-   [ ] Present results without overstating simulated outcomes.
-   [ ] Prepare PPT and paper/report draft.
-   [ ] Include artifact/reproduction instructions.

**Exit criterion:** Every claim in the presentation/report maps to a
source, design decision, or reproducible result.

------------------------------------------------------------------------

## 13. Literature register --- initial leads, not verified citations

The initial research report surfaced the following leads. They must be
checked against original publisher pages, DOI records, full papers, and
official repositories before formal citation.

  --------------------------------------------------------------------------
  Lead from initial       Relevance               Verification required
  report                                          
  ----------------------- ----------------------- --------------------------
  Gupta et al. (2017),    Foundational simulator  Exact title, author list,
  iFogSim toolkit         and fog application     venue, DOI, simulator
                          model                   capabilities and metrics

  Mahmud et al. (2022),   Simulator extension     Exact
  iFogSim2                                        title/authors/venue/DOI;
                                                  exact feature list and
                                                  implementation

  Sinqadu & Shibeshi      Closest traffic         Full bibliographic record,
  (2020), traffic         surveillance simulation workload, topology, exact
  surveillance                                    metrics/results

  Sinqadu et al. (2022),  Potentially close       Full chapter access,
  traffic scheduling      scheduling baseline     algorithm definitions,
  chapter                                         results and publication
                                                  details

  Elsayed et al. (2023),  Fog-based traffic       Exact paper identity,
  ITCMS                   congestion mitigation   simulator/version, metric
                                                  definitions and result
                                                  validity

  Mangalampalli et        Priority-aware fog      Exact publication, task
  al. (2025),             scheduling              model, objective function,
  priority-aware SA                               baseline fairness and
  scheduling                                      results

  Arora & Sharma (2026),  Recent fog-based smart  Verify
  FSTS                    transportation          existence/publication
                                                  status, architecture,
                                                  algorithm names,
                                                  quantitative claims

  Matange & Abraham       SUMO + iFogSim and      Verify paper/repository,
  (2026), iFogSUMO        ambulance scenario      co-simulation
                                                  implementation, traffic
                                                  metrics and claims

  Zekeng Ndadji (2026),   Emergency system        Verify arXiv record, model
  emergency               modeling                assumptions, reported
  response/iFogSim                                latency and simulator
                                                  limitations

  Hammouda et al. (2026), Recent multi-objective  Verify publication status,
  FOG-QIEA                fog scheduling          method, baselines and
                                                  numerical claims

  HGWOVP (year/source to  Metaheuristic           Identify primary paper and
  verify)                 scheduling lead         establish exact
                                                  acronym/method/results
  --------------------------------------------------------------------------

**Critical caution:** The initial report contains quantitative claims
(including 82% latency reduction, 75% energy reduction, 20--35%
improvements, and approximately 205 ms alert latency). These numbers are
**not accepted project facts** until checked in their original sources
and reproduced or contextualized. Do not put them in the PPT as
established facts yet.

### Literature review extraction template

For every paper, record: - complete citation and DOI/URL; -
peer-reviewed / preprint / technical report / blog; - problem
addressed; - simulator and exact version; - topology and workload; -
task model and mobility assumptions; - algorithm and objective; -
baselines; - metric definitions; - numerical results and experimental
repetitions; - limitations acknowledged by authors; - our assessment and
relevance; - what remains different in our proposed work.

------------------------------------------------------------------------

## 14. Open questions and decisions pending

1.  What is the exact and defensible gap after primary-source
    verification?
2.  Does the project focus on per-task scheduling, module placement,
    admission control, or a carefully bounded combination? Source
    inspection says these are different mechanisms. The provisional
    choice is per-task admission and forwarding, with placement kept as
    a baseline. Not closed until the subclass experiment works.
3.  Can a `FogDevice` subclass override `processTupleArrival`, and a
    research `AppModule` carry a non-time-shared scheduler, without
    editing `simulator/src`? The methods are `protected` and the
    constructors are public. This is not confirmed by a run.
4.  Are tasks modeled as tuples, cloudlets, or a project-level job
    abstraction mapped onto simulator events? The draft traffic
    application uses iFogSim tuples directly. The first prototype ran
    on that model. A separate job type is not required for the review.
5.  What is the precise definition of end-to-end latency for each task
    class?
6.  What deadline ranges can be justified from literature or scenario
    assumptions?
7.  Is a regional/proxy fog tier necessary, or does it add complexity
    without helping answer the research question? The first-review
    draft includes one regional device because the proposed workflows
    use it. Whether later experiments keep that tier is still open.
8.  Is SUMO required for the final research claim, or is it a future
    extension?
9.  Which energy model and network model are credible enough for the
    intended claims?
10. What is the smallest experiment set that can falsify the proposed
    scheduler's value?

------------------------------------------------------------------------

## 15. Explicit non-goals and scope guardrails

Until a new decision is recorded, do not: - add reinforcement learning
just because it sounds more advanced; - add blockchain, computer vision,
or a full V2X stack; - claim actual ambulance route optimization when
only compute-task dispatch is simulated; - claim traffic-light
optimization without a traffic simulator/control model; - add many tiers
or metaheuristics without a research question; - tune weights on the
test workload and then report the same workload as unbiased
evaluation; - compare algorithms using different resources, workloads,
or random seeds without explaining the design; - report only average
latency while ignoring tail latency and deadline misses; - describe a
policy as real-time unless its timing and overhead are measured in the
model.

------------------------------------------------------------------------

## 16. PPT-safe framing (current stage)

Until experiments exist, use proposal language:

**Suggested title:**\
*Emergency-Aware Adaptive Task Scheduling for Fog-Enabled Intelligent
Traffic Management*

**Motivation:**\
Traffic infrastructure produces heterogeneous events. Critical events
may need timely processing, while nearby fog resources are constrained
and cloud communication adds delay.

**Research question:**\
Can adaptive, priority- and resource-aware task scheduling improve
critical-event latency and deadline satisfaction under varying
fog/network load?

**Proposed approach:**\
Model intersection-level event sources and heterogeneous fog/cloud
resources in a validated simulator; compare established scheduling
baselines with a clearly specified adaptive policy.

**Evaluation:**\
Critical-task latency and deadline misses as primary metrics; energy,
network usage, throughput, completion ratio, and normal-task impact as
trade-off metrics.

**Honesty statement:**\
This is a simulation-based study. Real-world traffic outcomes are
outside the claim unless separately modeled and validated.

Do not present an unverified research gap as "no prior work has done
this." Say:\
"Preliminary literature review suggests a potential gap at the
intersection of emergency-class traffic workloads and adaptive fog task
scheduling; the closest studies are being verified to establish the
precise contribution."

------------------------------------------------------------------------

## 17. Instructions for Cursor

When asked to implement a task:

1.  Read this file and inspect the actual repository before editing.
2.  Summarize relevant current architecture and identify the exact
    files/classes to change.
3.  Check the simulator's actual source/API. Do not rely on guessed
    method names or paper descriptions.
4.  Propose the smallest implementation plan that satisfies the
    milestone.
5.  Preserve upstream simulator code where practical.
6.  Implement tests alongside the behavior.
7.  Run the relevant build/tests and report exact commands and outcomes.
8.  Create/update a concept note under `docs/concepts/` explaining the
    systems idea, design choices, trade-offs, and failure modes.
9.  Update this file only for confirmed changes. Mark unverified items
    as provisional.
10. In the final implementation summary, include:
    -   files changed,
    -   architecture changes,
    -   tests run and outcomes,
    -   known limitations,
    -   decisions made,
    -   next recommended task.

### Cursor must not

-   fabricate test success;
-   silently change the research scope;
-   claim a feature is supported without inspecting source or
    demonstrating it;
-   invent experimental results;
-   modify this status file to make unfinished work look complete;
-   implement EAAS before the simulator feasibility gate and baseline
    definitions are satisfied.

------------------------------------------------------------------------

## 18. Instructions for ChatGPT

When continuing this project:

-   Treat this file as the canonical project state.
-   Teach at an intermediate/advanced CS level; explain fundamentals
    only when needed to make a subtle systems concept precise.
-   Prioritize distributed systems, scheduling theory, queueing,
    real-time systems, simulation methodology, and experimental rigor.
-   Challenge weak novelty claims and unfair baselines.
-   Separate facts from hypotheses and design choices.
-   When asked for a Cursor prompt, make it repository-aware, bounded,
    test-driven, and explicit about files/docs/status updates.
-   Do not jump to implementation if the user is asking for research or
    conceptual understanding.
-   When new evidence changes the plan, explain why and update the
    relevant section.

------------------------------------------------------------------------

## 19. Change log

| Date | Change |
|---|---|
| 2026-11-06 | Created initial shared source of truth from the initial deep-research report. Marked research leads and algorithm as provisional; no traffic implementation or research results claimed. |
| 2026-11-06 | Recorded Cursor's repository inspection: iFogSim2 submodule commit pinned, JDK 22.0.2 / `--release 21` build verified, `VRGameFog` example exits 0, and setup documentation committed. Research-specific simulator feasibility remains open. |
| 2026-11-06 | Recorded the tuple-lifecycle inspection. Provisional EAAS hook is a research `FogDevice` subclass plus a custom cloudlet scheduler. EAAS is not implemented. Per-task metrics and the subclass experiment remain open. |
| 2026-11-06 | Drafted `docs/TRAFFIC_APPLICATION_DESIGN.md` from the placement and routing APIs. The design is not complete: it has not been implemented or run. `SIGNAL_COMMAND` is specified as an actuator broadcast because a downward module tuple does not execute on every copy of `intersection_processor`. |
| 2026-11-06 | Added `TrafficManagementPrototype` and ran it once. Compile used `javac --release 21` on 446 sources. The process exited 0 and printed both loop lines. EAAS was not added. Per-task distributions are still not collected. |

------------------------------------------------------------------------

## 20. Immediate next actions

1.  **Research verification:** Build a verified literature matrix from
    primary sources, beginning with the closest traffic-surveillance,
    emergency-vehicle, iFogSim2, and priority-aware scheduling papers.
2.  **Repository inspection:** Complete. The research repo is initialized, the iFogSim2 submodule is pinned, and setup documentation is committed.
3.  **Simulator feasibility:** The first traffic prototype runs. Next keep `VRGameFog` and `TrafficManagementPrototype` reproducible, then add per-task logs before any scheduler. Do not implement EAAS until those logs exist.
4.  **Research decision:** Finalize the narrow contribution only after
    steps 1--3.
5.  **Baseline-first implementation:** Build a minimal synthetic
    traffic-event workload and baseline policies before implementing
    EAAS.

**Current project principle:** Build the smallest simulation that can
rigorously disprove or support the research hypothesis. Complexity is
not a contribution; a clearly isolated, measured, reproducible
improvement is.
