package lab.mars.sim.core.missileInterception.script

object Config {

    var noGui = false

    var useScript = true

    var timeLine : TimeLine

    var adhocIp = "127.0.0.1"
    val SNAppPort = 56788
    var XXAddresses : HashMap<Int, String>

    var DeviceList : HashMap<Int, HashSet<DeviceType>>

    var ActiveRadarFrequencySettings : HashMap<Int, Frequency>

    var WarHeadProbability : HashMap<Int, Float>

    val probabilityOfGuidanceXXDisabled = 0.8f

    val probabilityOfESMXXDisabled = 0.6f

    init {
        ScriptTool.readScriptFilePath(ScriptTool.scriptPath)
        ScriptTool.handleXXDynamicResourceConfig(ScriptTool.filePathMap["xxDynamicResourceConfig"]!!)
        XXAddresses = ScriptTool.handleXXAddress(ScriptTool.filePathMap["xxAddresses"]!!)
        ScriptTool.handleXXInformation(ScriptTool.filePathMap["xxInformation"]!!)
        DeviceList = ScriptTool.deviceList
        WarHeadProbability = ScriptTool.warHeadProbability
        timeLine = initializeTimeLine(useScript)
        // todo:此处仅作临时初始化，请使用timeLine.xxActiveRadarEvents处理
        ActiveRadarFrequencySettings = hashMapOf(
            Pair(2, Frequency.Alpha),
            Pair(3, Frequency.Beta),
            Pair(4, Frequency.Gamma),
            Pair(5, Frequency.Alpha)
        )
    }

}