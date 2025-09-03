package lab.mars.sim.core.missileInterception.models.DataCollect.app

import lab.mars.sim.core.missileInterception.models.DataSender.writeByte
import lab.mars.sim.core.missileInterception.script.CoordinationType
import java.io.ByteArrayOutputStream

class ResetApp : AppData {
    override fun toByteArray(): ByteArray {
        return ByteArray(1) {
            0
        }
    }

}