package lab.mars.sim.core.missileInterception.models.DataCollect.app

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics
import lab.mars.sim.core.missileInterception.models.DataSender.writeByte
import lab.mars.sim.core.missileInterception.models.DataSender.writeFloat
import lab.mars.sim.core.missileInterception.models.DataSender.writeInt
import lab.mars.sim.core.missileInterception.models.DataSender.writeLong
import lab.mars.sim.core.missileInterception.script.*
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.agentSimArch.utility.Mathf
import kotlin.math.cos
import kotlin.math.sin

class ScanArea(val center: Vector3, val frequency: Frequency, val scheduledTime: ULong)

class ActiveRadarPositioningData(
    val coordIdx: Int,
    val leadIdx: Int,
    val followerIdx: Array<Int>,
    val dataTimeout: ULong,
    val scanAreas: ArrayList<ScanArea>
) : AppData {

    override fun toByteArray(): ByteArray {
        val writer = beginWriter(CoordinationType.ActiveRadarPositioning)
        writer.writeInt(coordIdx, leadIdx)
        writer.writeInt(followerIdx.size)
        writer.writeInt(followerIdx)
        writer.writeLong(dataTimeout.toLong())
        writer.writeInt(scanAreas.size)
        for (scanArea in scanAreas) {
            with(scanArea) {
                writer.writeFloat(center.x, center.y, center.z)
                writer.writeByte(frequency.ordinal.toByte())
                writer.writeLong(scheduledTime.toLong())
            }
        }
        return writer.toByteArray()
    }

}

private fun generateRandomPos2D(centerPos: Vector2, theta : Float, radius: Float): Vector2 {
    val x = radius * cos(theta)
    val y = radius * sin(theta)
    return Vector2(centerPos.x + x.toFloat(), centerPos.y + y.toFloat())
}

fun generateActiveRadarPositioningData(
    event : CoordinationEvent,
    dataTimeout: ULong
): ActiveRadarPositioningData {
    val carrier = Global.findAgent("Carrier1")
    val carrierPosition = carrier.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
    val scanAreas = ArrayList<ScanArea>()
    val leadRadarFrequency = Config.timeLine.getCurrentActiveRadarEvents()[event.lead]!!.frequency
    for (i in event.time.from until event.time.until step 5) {
        val center = generateRandomPos2D(Vector2(carrierPosition.x, carrierPosition.z), Mathf.random(0f, 2 * Mathf.PI), Mathf.random(0f, 1f))
        scanAreas.add(ScanArea(Vector3(center.x, 0f, center.y), leadRadarFrequency, i.toULong()))
    }
    return ActiveRadarPositioningData(event.idx, event.lead, event.followers, dataTimeout, scanAreas)
}