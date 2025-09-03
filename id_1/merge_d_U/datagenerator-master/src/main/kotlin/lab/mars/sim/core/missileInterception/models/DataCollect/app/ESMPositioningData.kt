package lab.mars.sim.core.missileInterception.models.DataCollect.app

import lab.mars.sim.core.missileInterception.models.DataSender.writeInt
import lab.mars.sim.core.missileInterception.models.DataSender.writeLong
import lab.mars.sim.core.missileInterception.script.CoordinationType

class ESMPositioningData(
    val coordIdx: Int,
    val leadIdx: Int,
    val followerIdx: Array<Int>,
    val dataTimeout : ULong
    ) : AppData {

    override fun toByteArray(): ByteArray {
        val writer = beginWriter(CoordinationType.ESMPositioning)
        writer.writeInt(coordIdx, leadIdx)
        writer.writeInt(followerIdx.size)
        writer.writeInt(followerIdx)
        writer.writeLong(dataTimeout.toLong())
        return writer.toByteArray()
    }

}