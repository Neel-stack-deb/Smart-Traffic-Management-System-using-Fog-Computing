package org.traffic.prototype;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.Config;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * First-review smart-traffic scenario.
 * Static placement only. This class does not implement EAAS.
 */
public class TrafficManagementPrototype {

	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();

	public static void main(String[] args) {
		Log.printLine("Starting TrafficManagementPrototype...");

		try {
			Log.disable();
			int numUser = 1;
			Calendar calendar = Calendar.getInstance();
			boolean traceFlag = false;

			CloudSim.init(numUser, calendar, traceFlag);

			String appId = "traffic_management";
			FogBroker broker = new FogBroker("broker");

			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());

			createFogDevices(broker.getId(), appId);

			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
			moduleMapping.addModuleToDevice("intersection_processor", "intersection-0");
			moduleMapping.addModuleToDevice("intersection_processor", "intersection-1");
			moduleMapping.addModuleToDevice("intersection_processor", "intersection-2");
			moduleMapping.addModuleToDevice("regional_coordinator", "regional-fog");
			moduleMapping.addModuleToDevice("cloud_analytics", "cloud");

			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);
			controller.submitApplication(application, 0,
					new ModulePlacementMapping(fogDevices, application, moduleMapping));

			Config.MAX_SIMULATION_TIME = 1000;
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();
			CloudSim.stopSimulation();

			Log.printLine("TrafficManagementPrototype finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static void createFogDevices(int userId, String appId) {
		FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16 * 103, 16 * 83.25);
		cloud.setParentId(-1);
		cloud.setUplinkLatency(0);

		FogDevice regional = createFogDevice("regional-fog", 8000, 8000, 10000, 10000, 1, 0.0, 200, 100);
		regional.setParentId(cloud.getId());
		regional.setUplinkLatency(100);

		FogDevice intersection0 = createFogDevice("intersection-0", 3000, 4000, 10000, 10000, 2, 0.0, 150, 90);
		intersection0.setParentId(regional.getId());
		intersection0.setUplinkLatency(4);

		FogDevice intersection1 = createFogDevice("intersection-1", 3000, 4000, 10000, 10000, 2, 0.0, 150, 90);
		intersection1.setParentId(regional.getId());
		intersection1.setUplinkLatency(4);

		FogDevice intersection2 = createFogDevice("intersection-2", 2000, 4000, 10000, 10000, 2, 0.0, 150, 90);
		intersection2.setParentId(regional.getId());
		intersection2.setUplinkLatency(6);

		fogDevices.add(cloud);
		fogDevices.add(regional);
		fogDevices.add(intersection0);
		fogDevices.add(intersection1);
		fogDevices.add(intersection2);

		addTrafficSensor("traffic-0", userId, appId, intersection0.getId());
		addTrafficSensor("traffic-1", userId, appId, intersection1.getId());
		addTrafficSensor("traffic-2", userId, appId, intersection2.getId());
		addEmergencySensor("emergency-0", userId, appId, intersection0.getId());

		addSignalActuator("signal-0", userId, appId, intersection0.getId());
		addSignalActuator("signal-1", userId, appId, intersection1.getId());
		addSignalActuator("signal-2", userId, appId, intersection2.getId());
	}

	private static void addTrafficSensor(String name, int userId, String appId, int gatewayId) {
		Sensor sensor = new Sensor(name, "TRAFFIC_SAMPLE", userId, appId, new DeterministicDistribution(20));
		sensor.setGatewayDeviceId(gatewayId);
		sensor.setLatency(1.0);
		sensors.add(sensor);
	}

	private static void addEmergencySensor(String name, int userId, String appId, int gatewayId) {
		Sensor sensor = new Sensor(name, "EMERGENCY_EVENT", userId, appId, new DeterministicDistribution(200));
		sensor.setGatewayDeviceId(gatewayId);
		sensor.setLatency(1.0);
		sensors.add(sensor);
	}

	private static void addSignalActuator(String name, int userId, String appId, int gatewayId) {
		Actuator actuator = new Actuator(name, userId, appId, "SIGNAL");
		actuator.setGatewayDeviceId(gatewayId);
		actuator.setLatency(1.0);
		actuators.add(actuator);
	}

	private static FogDevice createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw, int level,
			double ratePerMips, double busyPower, double idlePower) {
		List<Pe> peList = new ArrayList<Pe>();
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000;
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower));

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double timeZone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, timeZone, cost, costPerMem, costPerStorage, costPerBw);

		FogDevice fogDevice = null;
		try {
			fogDevice = new FogDevice(nodeName, characteristics, new AppModuleAllocationPolicy(hostList), storageList,
					10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fogDevice.setLevel(level);
		return fogDevice;
	}

	@SuppressWarnings({ "serial" })
	private static Application createApplication(String appId, int userId) {
		Application application = Application.createApplication(appId, userId);

		application.addAppModule("intersection_processor", 10);
		application.addAppModule("regional_coordinator", 10);
		application.addAppModule("cloud_analytics", 10);

		application.addAppEdge("TRAFFIC_SAMPLE", "intersection_processor", 3000, 500, "TRAFFIC_SAMPLE", Tuple.UP,
				AppEdge.SENSOR);
		application.addAppEdge("EMERGENCY_EVENT", "intersection_processor", 2000, 200, "EMERGENCY_EVENT", Tuple.UP,
				AppEdge.SENSOR);
		application.addAppEdge("intersection_processor", "regional_coordinator", 2000, 800, "INTERSECTION_STATUS",
				Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("intersection_processor", "regional_coordinator", 1500, 400, "EMERGENCY_ALERT", Tuple.UP,
				AppEdge.MODULE);
		application.addAppEdge("regional_coordinator", "cloud_analytics", 3000, 1200, "REGIONAL_REPORT", Tuple.UP,
				AppEdge.MODULE);
		application.addAppEdge("regional_coordinator", "SIGNAL", 100, 300, "SIGNAL_COMMAND", Tuple.DOWN,
				AppEdge.ACTUATOR);

		application.addTupleMapping("intersection_processor", "TRAFFIC_SAMPLE", "INTERSECTION_STATUS",
				new FractionalSelectivity(1.0));
		application.addTupleMapping("intersection_processor", "EMERGENCY_EVENT", "EMERGENCY_ALERT",
				new FractionalSelectivity(1.0));
		application.addTupleMapping("regional_coordinator", "INTERSECTION_STATUS", "REGIONAL_REPORT",
				new FractionalSelectivity(1.0));
		application.addTupleMapping("regional_coordinator", "EMERGENCY_ALERT", "SIGNAL_COMMAND",
				new FractionalSelectivity(1.0));

		final AppLoop congestion = new AppLoop(new ArrayList<String>() {
			{
				add("TRAFFIC_SAMPLE");
				add("intersection_processor");
				add("regional_coordinator");
				add("cloud_analytics");
			}
		});
		final AppLoop emergency = new AppLoop(new ArrayList<String>() {
			{
				add("EMERGENCY_EVENT");
				add("intersection_processor");
				add("regional_coordinator");
				add("SIGNAL");
			}
		});
		List<AppLoop> loops = new ArrayList<AppLoop>();
		loops.add(congestion);
		loops.add(emergency);
		application.setLoops(loops);

		return application;
	}
}
