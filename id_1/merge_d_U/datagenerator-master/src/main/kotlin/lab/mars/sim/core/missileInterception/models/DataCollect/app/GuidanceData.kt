package lab.mars.sim.core.missileInterception.models.DataCollect.app

import lab.mars.sim.core.missileInterception.models.DataSender.writeInt
import lab.mars.sim.core.missileInterception.script.CoordinationType

class GuidanceData(val coordIdx: Int,
                   val leadIdx: Int,
                   val followerIdx: Array<Int>,
                   val targetRetrieveFromIdx : Int) : AppData {
    override fun toByteArray(): ByteArray {
        val writer = beginWriter(CoordinationType.Guidance)
        writer.writeInt(coordIdx, leadIdx)
        writer.writeInt(followerIdx.size)
        writer.writeInt(followerIdx)
        writer.writeInt(targetRetrieveFromIdx)
        return writer.toByteArray()
    }
}