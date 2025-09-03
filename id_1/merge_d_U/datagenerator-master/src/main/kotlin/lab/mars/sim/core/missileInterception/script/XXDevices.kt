package lab.mars.sim.core.missileInterception.script

import com.badlogic.gdx.math.Vector3
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


enum class DeviceStatus {
    On,
    Off
}

open class XXDevice(val name: String, val status : DeviceStatus)

data class IMU(val heading: Vector3, val velocityKps: Float)

typealias Position = Vector3

data class XXFlightController(val imu: IMU, val position: Position) : XXDevice(name = "平台飞控", status = DeviceStatus.On)

data class RadarDetection(val signalDirectionOffsetFromIMUDir: Vector3, val credibility: Float)

enum class Frequency {
    Alpha,
    Beta,
    Gamma,
    FullBand
}
data class XXActiveRadar(
    val freqSelection: Frequency,
    val detectionDirectionOffsetFromIMUDir: Vector3 = Vector3.Zero,
    val currentDetection: RadarDetection = RadarDetection(Vector3.Zero, 0.0f)
) : XXDevice(name = "主动雷达", status = DeviceStatus.Off)

data class XXESMRadar(
    val freqSelection: Frequency,
    val detectionDirectionOffsetFromIMUDir: Vector3 = Vector3.Zero,
    val currentDetection: RadarDetection = RadarDetection(Vector3.Zero, 0.0f)
) : XXDevice(name = "被动雷达", status = DeviceStatus.On)

data class GuidanceInput(val targetPosition: Vector3, val targetFeature: String)

data class GuidanceOutput(
    val targetDirectionOffsetFromIMUDir: Vector3,
    val targetDistanceToPlatform: Float,
    val targetFeature: String
)

data class XXGuidanceRadar(
    val targetInfoInput: GuidanceInput = GuidanceInput(Vector3.Zero, ""),
    val targetOutput: GuidanceOutput = GuidanceOutput(Vector3.Zero, 0.0f, "")
) : XXDevice(name = "引导雷达", status = DeviceStatus.On)

data class XXWarHead(
    val destructionProbability: Float
) : XXDevice(name = "引信", status = DeviceStatus.On)

enum class DeviceType {
    FlightController,
    ActiveRadar,
    ESMRadar,
    GuidanceRadar,
    WarHead,
    Time
}