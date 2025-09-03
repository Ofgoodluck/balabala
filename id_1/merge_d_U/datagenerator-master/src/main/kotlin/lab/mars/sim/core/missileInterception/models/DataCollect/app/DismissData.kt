package lab.mars.sim.core.missileInterception.models.DataCollect.app

import lab.mars.sim.core.missileInterception.models.DataSender.writeInt
import lab.mars.sim.core.missileInterception.script.CoordinationEvent
import lab.mars.sim.core.missileInterception.script.CoordinationType
import lab.mars.windr.agentSimArch.agent.Agent


class DismissData(
    val coordIdx: Int,
    val leadIdx: Int,
    val followerIdx: Array<Int>
) : AppData {
    override fun toByteArray(): ByteArray {
        val writer = beginWriter(CoordinationType.Dismiss)
        writer.writeInt(coordIdx, leadIdx)
        writer.writeInt(followerIdx.size)
        writer.writeInt(followerIdx)
        return writer.toByteArray()
    }

}
fun generateDismissData(agents: HashMap<Int, Agent>, event: CoordinationEvent): DismissData {
    return DismissData(event.idx, event.lead, event.followers)
}