package lab.mars.sim.core.missileInterception.script

import com.badlogic.gdx.math.Vector3
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

enum class XXRole {
    ClusterLead, //簇头角色
    FormationLead, //队形控制节点
    FormationFollower, //队形跟随节点
    ESMLead, //无源融合节点
    ESMFollower, //无源探测节点
    ActiveLead,//有源探测融合节点
    GuidanceLead, //引导节点
    GuidanceFollower, //引导追随节点
    Self, //自己
    Dead
}

class RoleScript(val time: TimeRange, val role: XXRole) {
    override fun toString(): String {
        return "RoleScript(time=$time, role=$role)"
    }
}

class UDPListener(val port: UShort,)

abstract class UDPhandler(val peerIp: String, val peerPort: UShort, val selfPort: UShort, val onRecv: (data: ByteArray) -> Unit) {
    abstract fun send(data: ByteArray)
}

class DeviceSetup(val device: XXDevice, val statusPort: UShort, val statusIP: String, val controlPort: UShort)

class XX(
    val id: Int,
    devices: HashMap<DeviceType, DeviceSetup>
) {
    var coordinationEvent: CoordinationEvent? = null
    var environmentEvent: EnvironmentEvent? = null
    var outputToCRMS: ByteBuffer? = null
    var outputToApp: ByteBuffer? = null

    val devices = HashMap<DeviceType, XXDevice>()
//    val deviceListener = HashMap<DeviceType, UDPListener>()
//    val deviceSender = HashMap<DeviceType, UDPSender>()


    fun generateDataNormally(time : SimTime) {
        devices.forEach { (type, device) ->
            if (device.status == DeviceStatus.Off) {
                return@forEach
            }
            when(type) {
                DeviceType.FlightController -> normalFlightControl(time)
                DeviceType.ActiveRadar -> TODO()
                DeviceType.ESMRadar -> TODO()
                DeviceType.GuidanceRadar -> TODO()
                DeviceType.WarHead -> TODO()
                DeviceType.Time -> TODO()
            }
        }
    }


    fun normalFlightControl(time: SimTime) {
        val step = time.getStepMS()
        val flightController = devices[DeviceType.FlightController]!! as XXFlightController
        flightController.position.add(
            VectorFactory.mul(
                flightController.imu.heading,
                flightController.imu.velocityKps * (step.toFloat() / 1000f)
            )
        )
    }


}


fun createXX(
    xxDeviceDescriptions: ArrayList<Pair<Int, HashSet<DeviceType>>>,
    initialHeading: Vector3,
    velocityKps: Float,
    initialPositions: HashMap<Int, Vector3>,
    freqSelectionForActiveRadars: HashMap<Int, Frequency>,
    warheadDestructProbability: HashMap<Int, Float>,
    xxRoleScripts: HashMap<Int, LinkedList<Pair<ULong, HashSet<XXRole>>>>
): ArrayList<XX> {
    val ret = ArrayList<XX>()
//    xxDeviceDescriptions.forEach { (id, deviceTypes) ->
//        val xxDevices = hashMapOf<DeviceType, XXDevice>()
//        deviceTypes.forEach { deviceType ->
//            when (deviceType) {
//                DeviceType.FlightController -> {
//                    val flightController = XXFlightController(
//                        IMU(initialHeading, velocityKps),
//                        initialPositions[id]!!
//                    )
//                    xxDevices[DeviceType.FlightController] = flightController
//                }
//
//                DeviceType.ActiveRadar -> {
//                    val activeRadar = XXActiveRadar(DeviceStatus.Off, freqSelectionForActiveRadars[id]!!)
//                    xxDevices[DeviceType.ActiveRadar] = activeRadar
//                }
//
//                DeviceType.ESMRadar -> {
//                    val esmRadar = XXESMRadar(DeviceStatus.On, Frequency.FullBand)
//                    xxDevices[DeviceType.ESMRadar] = esmRadar
//                }
//
//                DeviceType.GuidanceRadar -> {
//                    val guidanceRadar = XXGuidanceRadar(DeviceStatus.Off)
//                    xxDevices[DeviceType.GuidanceRadar] = guidanceRadar
//                }
//
//                DeviceType.WarHead -> {
//                    val warhead = XXWarHead(warheadDestructProbability[id]!!)
//                    xxDevices[DeviceType.WarHead] = warhead
//                }
//            }
//            val xx = XX(id, xxDevices, xxRoleScripts[id]!!)
//            ret.add(xx)
//        }
//    }
    return ret
}


fun initializeXX(): ArrayList<XX> {

    val xxDestructionProbability = hashMapOf(
        Pair(1, 0.25f),
        Pair(2, 0.15f),
        Pair(3, 0.15f),
        Pair(4, 0.15f),
        Pair(5, 0.15f),
        Pair(6, 0.2f),
        Pair(7, 0.2f),
        Pair(8, 0.25f),
        Pair(9, 0.4f),
        Pair(10, 0.4f),
    )

    val xxDeviceDescriptions = hashMapOf(
        Pair(1, hashSetOf(DeviceType.FlightController, DeviceType.ESMRadar, DeviceType.WarHead)),
        Pair(2, hashSetOf(DeviceType.ESMRadar, DeviceType.ActiveRadar, DeviceType.WarHead)),
        Pair(3, hashSetOf(DeviceType.ESMRadar, DeviceType.ActiveRadar, DeviceType.GuidanceRadar, DeviceType.WarHead)),
        Pair(4, hashSetOf(DeviceType.ESMRadar, DeviceType.ActiveRadar, DeviceType.GuidanceRadar, DeviceType.WarHead)),
        Pair(5, hashSetOf(DeviceType.ESMRadar, DeviceType.ActiveRadar, DeviceType.GuidanceRadar, DeviceType.WarHead)),
        Pair(6, hashSetOf(DeviceType.ESMRadar, DeviceType.GuidanceRadar, DeviceType.WarHead)),
        Pair(7, hashSetOf(DeviceType.ESMRadar, DeviceType.GuidanceRadar, DeviceType.WarHead)),
        Pair(8, hashSetOf(DeviceType.ESMRadar, DeviceType.WarHead)),
        Pair(9, hashSetOf(DeviceType.ESMRadar, DeviceType.WarHead)),
        Pair(10, hashSetOf(DeviceType.ESMRadar, DeviceType.WarHead)),
    )
    TODO("call createXX()")
}