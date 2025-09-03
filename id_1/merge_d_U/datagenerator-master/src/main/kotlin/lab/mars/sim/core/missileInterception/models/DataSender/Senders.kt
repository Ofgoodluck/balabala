package lab.mars.sim.core.missileInterception.models.DataSender

import lab.mars.sim.core.missileInterception.models.DataCollect.*
import lab.mars.sim.core.missileInterception.models.DataCollect.app.AppData
import lab.mars.sim.core.missileInterception.models.DataCollect.app.ResetApp
import lab.mars.sim.core.missileInterception.script.DeviceType
import lab.mars.sim.core.missileInterception.script.ScriptTool
import lab.mars.sim.core.missileInterception.script.secondNow
import java.io.ByteArrayOutputStream

const val AppPort = 8777
const val CRMSRegisterPort = 8001 // upd service

object PacketType {
    const val Input = 0x87.toByte()
    const val Output = 0x86.toByte()
}

object DType {
    const val FlightControl = 0x01.toByte()
    const val ActiveRadar = 0x02.toByte()
    const val ESMRadar = 0x03.toByte()
    const val GuidanceRadar = 0x04.toByte()
    const val Time = 0x05.toByte()
}


private fun beginWriter(pType: Byte, dType: Byte): ByteArrayOutputStream {
    val writer = ByteArrayOutputStream()
    writer.writeByte(pType)
    writer.writeByte(dType)
    return writer
}

private fun endWriter(writer: ByteArrayOutputStream): ByteArray {
    val second = secondNow()
    writer.writeLong(second.toLong())
    return writer.toByteArray()
}

private fun FlightControlColumn.toByteArray(): ByteArray {
    val writer = beginWriter(PacketType.Input, DType.FlightControl)
    writer.writeFloat(
        this.yaw,
        this.pitch,
        0f /*roll is always zero*/,
        this.longitude,
        this.latitude,
        this.altitude,
        this.velocity
    )
    return endWriter(writer)
}

private fun ActiveRadarData.toByteArray(): ByteArray {
    val writer = beginWriter(PacketType.Input, DType.ActiveRadar)
    writer.writeFloat(this.yawOffset, this.pitchOffset, 0f)
    writer.writeFixedString(this.signalFeature, 256)
    writer.writeFloat(this.credibility)
    return endWriter(writer)
}

private fun ESMRadarData.toByteArray(): ByteArray {
    val writer = beginWriter(PacketType.Input, DType.ESMRadar)
    writer.writeFloat(this.yawOffset, this.pitchOffset, 0f)
    writer.writeFixedString(this.signalFeature, 256)
    writer.writeFloat(this.credibility)
    return endWriter(writer)
}

private fun GuidanceRadarData.toByteArray(): ByteArray {
    val writer = beginWriter(PacketType.Input, DType.GuidanceRadar)
    writer.writeFloat(this.targetYawOffset, this.targetYawOffset, 0f, this.targetDistance)
    writer.writeFixedString(this.targetFeature, 256)
    return endWriter(writer)
}

private fun CurrentTimeData.toByteArray(): ByteArray {
    val writer = beginWriter(PacketType.Input, DType.Time)
    writer.writeLong(this.t.toLong())
    return endWriter(writer)
}

class Sender(
    val id: Int,
    private val crmsDeviceRegister: UdpCommunicator,
    private val flightControl: UdpCommunicator,
    private val activeRadar: UdpCommunicator,
    private val esmRadar: UdpCommunicator,
    private val guidanceRadar: UdpCommunicator,
    private val timeDevice: UdpCommunicator,
    private val coordinationApp: AppDataSender
) {
    fun sendFlightControlData(flightControlColumn: FlightControlColumn) {
        flightControl.send(flightControlColumn.toByteArray())
    }

    fun sendActiveRadarData(activeRadarData: ActiveRadarData) {
        activeRadar.send(activeRadarData.toByteArray())
    }

    fun sendESMRadarData(esmRadarData: ESMRadarData) {
        esmRadar.send(esmRadarData.toByteArray())
    }

    fun sendGuidanceRadarData(guidanceRadarData: GuidanceRadarData) {
        guidanceRadar.send(guidanceRadarData.toByteArray())
    }

    fun sendTimeData(time: CurrentTimeData) {
        timeDevice.send(time.toByteArray())
    }

    fun sendAppData(data: AppData) {
        coordinationApp.send(data.toByteArray())
    }

    fun sendDeviceRegisterMessage(deviceList: HashSet<DeviceType>) {
        deviceList.forEach {
            // 内容
            Thread.sleep(100)
            if (!ScriptTool.xxDevicePortMap.containsKey(it)) {
                return@forEach
            }
            //格式是 <载荷类型字符串>,<仿真端IP地址>,<载荷端打开的udp server应使用的端口>
            val string =
                "${it.name},${ScriptTool.xxDeviceIPMap[it]!!},${ScriptTool.xxDevicePortMap[it]!!}"
            crmsDeviceRegister.send(string.encodeToByteArray())
        }
    }
}

fun initSenders(
    addresses: HashMap<Int, String>,
    deviceListeners: HashMap<Int, ((ByteArray) -> Unit)?>
): HashMap<Int, Sender> {
    val ret = hashMapOf<Int, Sender>()
    addresses.forEach { (idx, ip) ->
        val appIp = ip
        val fcPort = ScriptTool.xxDevicePortMap[DeviceType.FlightController]!!
        val ActiveRadarPort = ScriptTool.xxDevicePortMap[DeviceType.ActiveRadar]!!
        val ESMRadarPort = ScriptTool.xxDevicePortMap[DeviceType.ESMRadar]!!
        val GuidanceRadarPort = ScriptTool.xxDevicePortMap[DeviceType.GuidanceRadar]!!
        val TimePort = ScriptTool.xxDevicePortMap[DeviceType.Time]!!
        val sender = Sender(
            idx,
            UdpCommunicator(
                CRMSRegisterPort + 1000 + idx,
                ip, CRMSRegisterPort, null
            ),
            UdpCommunicator(
                fcPort + 1000 + idx * 10,
                ip,
                fcPort,
                deviceListeners[idx]
            ),
            UdpCommunicator(ActiveRadarPort + 1000 + idx * 10, ip, ActiveRadarPort, deviceListeners[idx]),
            UdpCommunicator(ESMRadarPort + 1000 + idx * 10, ip, ESMRadarPort, deviceListeners[idx]),
            UdpCommunicator(
                GuidanceRadarPort + 1000 + idx * 10,
                ip,
                GuidanceRadarPort,
                deviceListeners[idx]
            ),
            UdpCommunicator(TimePort + 1000 + idx * 10, ip, TimePort, deviceListeners[idx]),
            AppDataSender(appIp, AppPort)
        )
        ret[idx] = sender
        sender.sendAppData(ResetApp())
    }
    return ret
}