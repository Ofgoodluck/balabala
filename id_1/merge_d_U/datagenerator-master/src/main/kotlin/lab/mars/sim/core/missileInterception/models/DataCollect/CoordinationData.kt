package lab.mars.sim.core.missileInterception.models.DataCollect

import lab.mars.sim.core.missileInterception.script.CoordinationEvent
import lab.mars.sim.core.missileInterception.script.secondNow
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.util.HashMap


fun Array<Int>.format() : String {
    val builder = StringBuilder()
    this.forEach {
        builder.append("$it ")

    }
    return builder.toString()
}
data class CoordinationData(val type : String, val leadIdx : Int, val followerIdx : Array<Int>){
    companion object{
        val fileHeader = "时间点,协同事件类型,协同主节点,协同跟随节点列表"
    }

    override fun toString(): String {
        return "${type},${leadIdx},${followerIdx.format()}"
    }

}

fun initCoordinationDataFile(dataFolder : File) : FileOutputStream {
    val string = StringBuilder("时间点")
    string.append(",经度,纬度,高度,偏航角,俯仰角,速度,偏航控制,俯仰控制")
    string.append("\n")
    val f = File(dataFolder.absolutePath, "协同事件数据.csv")
    f.createNewFile()
    val stream = f.outputStream()
    stream.write(string.toString().encodeToByteArray())
    return stream
}

fun writeCoordinationDataFile(file : FileOutputStream, coordinationData: CoordinationData) {
    val str = "${secondNow()},$coordinationData\n"
    file.write(str.encodeToByteArray())
}

data class FormationKeepFollowerColumns(
    val predefinedDistance: Float,
    val actualDistance: Float,
    val distanceOffset: Float
) {
    override fun toString(): String {
        return "${(predefinedDistance * 1000).format(2)},${(actualDistance * 1000).format(2)},${
            (distanceOffset * 1000).format(
                2
            )
        }"
    }
}

fun initFormationKeepDataFile(dataFolder: File, event: CoordinationEvent): FileOutputStream {
    val fileName = StringBuilder("队形保持应用数据{")
    fileName.append(event.lead)
    event.followers.forEach {
        fileName.append(",$it")
    }
    fileName.append("}.csv")
    val formationKeepDataFile = File(dataFolder.absolutePath, fileName.toString())
    val string = StringBuilder()
    string.append("时间点,")
    string.append(
        "队形控制节点${event.lead}预置弹道经度," +
                "队形控制节点${event.lead}预置弹道纬度," +
                "队形控制节点${event.lead}预置弹道高度," +
                "队形控制节点${event.lead}经度偏移," +
                "队形控制节点${event.lead}纬度偏移," +
                "队形控制节点${event.lead}高度偏移"
    )
    event.followers.forEach {
        string.append(
            ",跟随节点${it}预置相对距离," +
                    "跟随节点${it}实际相对距离," +
                    "跟随节点${it}相对距离偏移"
        )
    }
    string.append("\n")
    formationKeepDataFile.createNewFile()
    val ret = formationKeepDataFile.outputStream()
    ret.write(string.toString().encodeToByteArray())
    return ret
}



data class FormationKeepLeadColumns(
    val predefinedLongitude: Float,
    val predefinedLatitude: Float,
    val predefinedAltitude: Float,
    val lonOffset: Float,
    val latOffset: Float,
    val altOffset: Float
) {
    override fun toString(): String {
        return "${(predefinedLongitude * 1000).format(2)},${(predefinedLatitude * 1000).format(2)},${
            (predefinedAltitude * 1000).format(
                2
            )
        },${(lonOffset * 1000).format(2)},${(latOffset * 1000).format(2)},${(altOffset * 1000).format(2)}"
    }
}


fun writeFormationControlData(
    file: FileOutputStream,
    leadData: FormationKeepLeadColumns,
    followersData: HashMap<Int, FormationKeepFollowerColumns>
) {
    val str = StringBuffer("${secondNow()}")
    str.append(",$leadData")
    val sorted = followersData.toList().sortedBy { it.first }.map { it.second }.toList()
    sorted.forEach {
        str.append(",$it")
    }
    str.append("\n")
    file.write(str.toString().encodeToByteArray())
}