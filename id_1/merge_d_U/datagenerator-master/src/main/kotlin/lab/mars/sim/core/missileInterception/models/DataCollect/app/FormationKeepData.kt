package lab.mars.sim.core.missileInterception.models.DataCollect.app

import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics
import lab.mars.sim.core.missileInterception.models.DataSender.writeByte
import lab.mars.sim.core.missileInterception.models.DataSender.writeFloat
import lab.mars.sim.core.missileInterception.models.DataSender.writeInt
import lab.mars.sim.core.missileInterception.models.DataSender.writeLong
import lab.mars.sim.core.missileInterception.script.CoordinationEvent
import lab.mars.sim.core.missileInterception.script.CoordinationType
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.io.ByteArrayOutputStream

class PredefinedPathPoint(
    val lon: Float,
    val lat: Float,
    val alt: Float,
    val yaw: Float,
    val pitch: Float,
    val dataTime: ULong
) {
    fun write(writer: ByteArrayOutputStream) {
        writer.writeFloat(lon, lat, alt, yaw, pitch)
        writer.writeLong(dataTime.toLong())
    }
}

class PredefinedFormationRule(
    val id: Int,
    val distance: Float,
    val yawOffsetLimit: Float,
    val pitchOffsetLimit: Float,
    val distanceOffsetLimit: Float,
) {
    fun write(writer: ByteArrayOutputStream) {
        writer.writeInt(id)
        writer.writeFloat(
            distance,
            yawOffsetLimit,
            pitchOffsetLimit,
            distanceOffsetLimit
        )
    }
}

private val CoordTypeByteMap = hashMapOf(
    Pair(CoordinationType.FormationKeep, 0x01.toByte()),
    Pair(CoordinationType.ESMPositioning, 0x02.toByte()),
    Pair(CoordinationType.ActiveRadarPositioning, 0x03.toByte()),
    Pair(CoordinationType.Guidance, 0x04.toByte()),
    Pair(CoordinationType.Dismiss, 0x05.toByte())
)

fun beginWriter(type: CoordinationType): ByteArrayOutputStream {
    val ret = ByteArrayOutputStream()
    ret.writeByte(CoordTypeByteMap[type]!!)
    return ret
}

class FormationKeepData(
    val coordIdx: Int,
    val formationLead: Int,
    val followers: Array<Int>,
    val predefinedPath: Array<PredefinedPathPoint>,
    val predefinedFormationRule: Array<PredefinedFormationRule>
) : AppData {
    override fun toByteArray(): ByteArray {
        val writer = beginWriter(CoordinationType.FormationKeep)
        writer.writeInt(coordIdx, formationLead)
        writer.writeInt(followers.size)
        writer.writeInt(followers)
        writer.writeInt(predefinedPath.size)
        predefinedPath.forEach { it.write(writer) }
        writer.writeInt(predefinedFormationRule.size)
        predefinedFormationRule.forEach { it.write(writer) }
        return writer.toByteArray()
    }
}

fun generateFormationKeepData(agents: HashMap<Int, Agent>, event: CoordinationEvent): FormationKeepData {
    val predefinedPath = ArrayList<PredefinedPathPoint>()
    val predefinedFormationRule = ArrayList<PredefinedFormationRule>()
    val leadAgent = agents[event.lead]!!
    val leadTargetList =
        leadAgent.getState<ArrayList<Vector3>>(MissileAgentStatics.MissileAgentStateName.MissilePredefinedTargets)
    val leadDirection = leadAgent.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentDirection)
    leadTargetList.forEachIndexed { idx, it ->
        predefinedPath.add(PredefinedPathPoint(it.x, it.z, it.y, leadDirection.x, leadDirection.y, idx.toULong()))
    }
    predefinedFormationRule.add(PredefinedFormationRule(event.lead, 0f, 1f, 1f, 0.05f))
    event.followers.forEach {
        val followerAgent = agents[it]!!
        val followerTargetList =
            followerAgent.getState<ArrayList<Vector3>>(MissileAgentStatics.MissileAgentStateName.MissilePredefinedTargets)
        val distance = VectorFactory.distance(leadTargetList.first(), followerTargetList.first())
        predefinedFormationRule.add(PredefinedFormationRule(it, distance, 0.1f, 0.1f, 0.05f))
    }
    return FormationKeepData(
        event.idx,
        event.lead,
        event.followers,
        predefinedPath.toTypedArray(),
        predefinedFormationRule.toTypedArray()
    )
}