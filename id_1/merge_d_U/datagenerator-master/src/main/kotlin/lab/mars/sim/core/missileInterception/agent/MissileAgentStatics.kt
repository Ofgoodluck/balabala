package lab.mars.sim.core.missileInterception.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import java.util.*


/**
 * Created by imrwz on 7/17/2017.
 */
object MissileAgentStatics {
    enum class MissileAgentStateName {
        Id, RadarScannedTargetList, Radar, MissilePilotStatus,
        MissileControlInput, MissilePredefinedTargets,
        ESMRadarData, ActiveRadarData, GuidanceRadarOutputData,
        WarheadProbability,
        DeviceList
    }

    val random = Random(21)

    val FormationKeepFollowerPositionOffsets = hashMapOf(
        Pair(
            1,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            2,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            3,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            4,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            5,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            6,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            7,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            8,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            9,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        ),
        Pair(
            10,
            Vector3(random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f, random.nextFloat() / 50 - 0.01f)
        )
    )


    val MissileColorsInCoordination = hashMapOf(
        Pair(1, Color.RED),
        Pair(2, Color.ORANGE),
        Pair(3, Color.YELLOW),
        Pair(4, Color.GREEN),
        Pair(5, Color.BLUE),
        Pair(6, Color.PINK),
        Pair(7, Color.MAGENTA),
        Pair(8, Color.WHITE),
        Pair(9, Color.GRAY),
        Pair(10, Color.BLACK)
    )


    enum class DeviceStatus {
        On,
        Off
    }

    data class ActiveRadarData(
        val status : DeviceStatus = DeviceStatus.Off,
        val yawOffset: Float = 0f,
        val pitchOffset: Float = 0f,
        val signalFeature: String = "",
        val credibility: Float = 0f
    ) {
        override fun toString(): String {
            return "${status.name},$yawOffset,$pitchOffset,'$signalFeature',$credibility"
        }
    }

    data class ESMRadarData(
        val status : DeviceStatus = DeviceStatus.Off,
        val yawOffset: Float = 0f,
        val pitchOffset: Float = 0f,
        val signalFeature: String = "",
        val credibility: Float = 0f
    ) {
        override fun toString(): String {
            return "${status.name},$yawOffset,$pitchOffset,'$signalFeature',$credibility"
        }
    }

    data class GuidanceRadarOutputData(
        val status : DeviceStatus = DeviceStatus.Off,
        val targetYawOffset: Float = 0f,
        val targetPitchOffset: Float = 0f,
        val targetDistance: Float = 0f,
        val targetFeature: String = ""
    ) {
        override fun toString(): String {
            return "${status.name},$targetYawOffset,$targetPitchOffset,$targetDistance,'$targetFeature'"
        }
    }
}