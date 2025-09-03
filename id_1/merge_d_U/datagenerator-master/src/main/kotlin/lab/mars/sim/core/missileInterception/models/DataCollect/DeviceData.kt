package lab.mars.sim.core.missileInterception.models.DataCollect

import lab.mars.sim.core.missileInterception.agent.MissileAgent
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics
import lab.mars.sim.core.missileInterception.script.DeviceType
import lab.mars.sim.core.missileInterception.script.secondNow
import lab.mars.windr.agentSimArch.game.Global
import java.io.File
import java.io.FileOutputStream

data class CurrentTimeData(val t : ULong)

data class FlightControlColumn(
    val longitude: Float,
    val latitude: Float,
    val altitude: Float,
    val yaw: Float,
    val pitch: Float,
    val velocity: Float,
    val yawControl: Int,
    val pitchControl: Int
) {
    override fun toString(): String {
        return "${(longitude * 1000).format(2)},${(latitude * 1000).format(2)},${(altitude * 1000).format(2)},${yaw},${pitch},${
            (velocity * 1000).format(
                2
            )
        },${yawControl},${pitchControl}"
    }
}


fun initFlightControlDataFile(dataFolder: File): HashMap<Int, FileOutputStream> {
    val ret = hashMapOf<Int, FileOutputStream>()
    val string = StringBuilder("时间点")
    string.append(",经度,纬度,高度,偏航角,俯仰角,速度,偏航控制,俯仰控制")
    string.append("\n")
    Global.findAgents(MissileAgent::class.java).forEach {
        val id = it.getState<Int>(MissileAgentStatics.MissileAgentStateName.Id)
        val f = File(dataFolder.absolutePath, "XX${id}_飞控设备数据.csv")
        f.createNewFile()
        val stream = f.outputStream()
        stream.write(string.toString().encodeToByteArray())
        ret[id] = stream
    }
    return ret
}

fun initESMRadarData(dataFolder: File): HashMap<Int, FileOutputStream> {
    val ret = hashMapOf<Int, FileOutputStream>()
    val string = StringBuilder("时间点")
    string.append(",开关,偏航角偏移量,俯仰角偏移量,信号特征,置信度\n")
    Global.findAgents(MissileAgent::class.java).filter {
        it.getState<HashSet<DeviceType>>(MissileAgentStatics.MissileAgentStateName.DeviceList)
            .contains(DeviceType.ESMRadar)
    }.forEach {
        val id = it.getState<Int>(MissileAgentStatics.MissileAgentStateName.Id)
        val f = File(dataFolder.absolutePath, "XX${id}_被动雷达设备数据.csv")
        f.createNewFile()
        val stream = f.outputStream()
        stream.write(string.toString().encodeToByteArray())
        ret[id] = stream
    }
    return ret
}

fun initActiveRadarData(dataFolder: File): HashMap<Int, FileOutputStream> {
    val ret = hashMapOf<Int, FileOutputStream>()
    val string = StringBuilder("时间点")
    string.append(",开关,偏航角偏移量,俯仰角偏移量,信号特征,置信度\n")
    Global.findAgents(MissileAgent::class.java).filter {
        it.getState<HashSet<DeviceType>>(MissileAgentStatics.MissileAgentStateName.DeviceList)
            .contains(DeviceType.ActiveRadar)
    }.forEach {
        val id = it.getState<Int>(MissileAgentStatics.MissileAgentStateName.Id)
        val f = File(dataFolder.absolutePath, "XX${id}_主动雷达设备数据.csv")
        f.createNewFile()
        val stream = f.outputStream()
        stream.write(string.toString().encodeToByteArray())
        ret[id] = stream
    }
    return ret
}

fun initGuidanceRadarData(dataFolder: File): HashMap<Int, FileOutputStream> {
    val ret = hashMapOf<Int, FileOutputStream>()
    val string = StringBuilder("时间点")
    string.append(",开关,偏航角偏移量,俯仰角偏移量,信号特征,置信度\n")
    Global.findAgents(MissileAgent::class.java).filter {
        it.getState<HashSet<DeviceType>>(MissileAgentStatics.MissileAgentStateName.DeviceList)
            .contains(DeviceType.GuidanceRadar)
    }.forEach {
        val id = it.getState<Int>(MissileAgentStatics.MissileAgentStateName.Id)
        val f = File(dataFolder.absolutePath, "XX${id}_引导雷达设备数据.csv")
        f.createNewFile()
        val stream = f.outputStream()
        stream.write(string.toString().encodeToByteArray())
        ret[id] = stream
    }
    return ret
}

fun writeFlightControlData(flightControlDataFile: FileOutputStream, column: FlightControlColumn) {
    val str = "${secondNow()},$column\n"
    flightControlDataFile.write(str.encodeToByteArray())
}

typealias ESMRadarData = MissileAgentStatics.ESMRadarData
typealias ActiveRadarData = MissileAgentStatics.ActiveRadarData
typealias GuidanceRadarData = MissileAgentStatics.GuidanceRadarOutputData

fun writeESMRadarData(f: FileOutputStream, column: ESMRadarData) {
    val str = "${secondNow()},$column\n"
    f.write(str.encodeToByteArray())
}
fun writeActiveRadarData(f: FileOutputStream, column: ActiveRadarData) {
    val str = "${secondNow()},$column\n"
    f.write(str.encodeToByteArray())
}
fun writeGuidanceRadarData(f: FileOutputStream, column: GuidanceRadarData) {
    val str = "${secondNow()},$column\n"
    f.write(str.encodeToByteArray())
}