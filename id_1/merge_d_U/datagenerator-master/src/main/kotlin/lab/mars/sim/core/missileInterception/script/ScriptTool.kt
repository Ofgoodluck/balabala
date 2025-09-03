package lab.mars.sim.core.missileInterception.script

import java.io.File

object ScriptTool {

    var scriptPath = ".\\src\\main\\resources\\scriptFilePath"

    val filePathMap = HashMap<String, String>()
    fun readScriptFilePath(filePath: String) {
        val filePathString = File(filePath).readLines()
        for (filePathPair in filePathString) {
            val item = splitAndRemoveNonValidItem(filePathPair, "=")
            filePathMap[item[0]] = item[1]
        }
    }

    val xxDevicePortMap = HashMap<DeviceType, Int>()
    val xxDeviceIPMap = HashMap<DeviceType, String>()

    fun handleXXDynamicResourceConfig(filePath: String) {
        val xxDynamicResourceStatList = preHandleStat(File(filePath).readLines())
        for (stat in xxDynamicResourceStatList) {
            val items = splitAndRemoveNonValidItem(stat, ",")
            xxDeviceIPMap[DeviceType.valueOf(items[0])] = items[1]
            xxDevicePortMap[DeviceType.valueOf(items[0])] = items[2].toInt()
        }
    }

    fun handleXXAddress(filePath: String) : HashMap<Int, String> {
        val xxAddresses = HashMap<Int, String>()
        val xxAddressStatList = preHandleStat(File(filePath).readLines())
        for (xxAddress in xxAddressStatList) {
            val items = splitAndRemoveNonValidItem(xxAddress, ",")
            // 0是nodeId, 1是IP字符串
            xxAddresses[items[0].toInt()] = items[1]
        }
        return xxAddresses
    }

    fun handleCyberspace(filePath: String) : ArrayList<CyberspaceEvent> {
        val xxCyberspaceEvents = ArrayList<CyberspaceEvent>()
        val xxCyberspaceStatList = preHandleStat(File(filePath).readLines())
        var i = 0
        while (i < xxCyberspaceStatList.size) {
            val items = splitAndRemoveNonValidItem(xxCyberspaceStatList[i])
            val time = items[0].toInt()
            var nodeNum = items[2].toInt()
            val topologyPairs = HashMap<Int, HashSet<Int>>()
            ++i
            while (i < xxCyberspaceStatList.size && nodeNum-- > 0) {
                val nodeInfo = splitAndRemoveNonValidItem(xxCyberspaceStatList[i])
                val inDegree = HashSet<Int>()
                var j = 2
                while (j < nodeInfo.size) {
                    inDegree.add(nodeInfo[j].toInt())
                    ++j
                }
                topologyPairs[nodeInfo[0].toInt()] = inDegree
                ++i
            }
            xxCyberspaceEvents.add(CyberspaceEvent(TimeRange(time), topologyPairs))
        }
        return xxCyberspaceEvents
    }

    private var platformNumber = 0
    val warHeadProbability = HashMap<Int, Float>()
    val deviceList = HashMap<Int, HashSet<DeviceType>>()
    fun handleXXInformation(filePath: String) {
        val xxInformationStatList = preHandleStat(File(filePath).readLines())
        for (stat in xxInformationStatList) {
            val items = splitAndRemoveNonValidItem(stat, ",")
            val node = items[0].toInt()
            warHeadProbability[node] = items[1].toFloat()
            deviceList[node] = HashSet<DeviceType>()
            var i = 2
            while (i < items.size) {
                val res = items[i].toInt()
                if (res != 0) {
                    deviceList[node]!!.add(deviceTypeMap[i]!!)
                }
                ++i
            }
        }
        platformNumber = deviceList.size
    }

    fun getPlatformNumber(): Int {
        return platformNumber
    }

    fun handleXXActiveRadarFrequency(filePath: String) : ArrayList<ActiveRadarEvent> {
        val xxActiveRadarEvents = ArrayList<ActiveRadarEvent>()
        val xxActiveRadarStatList = preHandleStat(File(filePath).readLines())
        for (stat in xxActiveRadarStatList) {
            val items = splitAndRemoveNonValidItem(stat, ",")
            val time = getTimeRange(items[0])
            val frequency = frequencyMapping(items[1])
            val affectedPlatform = items[2].toInt()
            xxActiveRadarEvents.add(ActiveRadarEvent(time, frequency, affectedPlatform))
        }
        return xxActiveRadarEvents
    }

    fun handleXXEnvironment(filePath: String) : ArrayList<EnvironmentEvent> {
        val environmentEventList = ArrayList<EnvironmentEvent>()
        val xxEnvironmentStatList = preHandleStat(File(filePath).readLines())
        for (stat in xxEnvironmentStatList) {
            val items = splitAndRemoveNonValidItem(stat, ",")
            val timeRange = getTimeRange(items[0])
            val environmentEType = environmentEventTypeMapping(items[1])
            val affectedPlatform = items[2].toInt()
            environmentEventList.add(EnvironmentEvent(timeRange, environmentEType, affectedPlatform))
        }
        return environmentEventList
    }

    fun handleXXCoordination(filePath: String) : ArrayList<CoordinationEvent> {
        val coordinationEventList = ArrayList<CoordinationEvent>()
        val xxCoordinationStatList = preHandleStat(File(filePath).readLines())
        for (stat in xxCoordinationStatList) {
            val items = splitAndRemoveNonValidItem(stat, ",")
            val timeRange = getTimeRange(items[0])
            val coordinationType = coordinationTypeMapping(items[1])
            val idx = items[2].toInt()
            val leader = items[3].toInt()
            val memberArrayList = ArrayList<Int>()
            if (items.size > 4) {
                splitAndRemoveNonValidItem(items[4], ";").forEach {
                    memberArrayList.add(it.toInt())
                }
            }
            val coordinationEvent = CoordinationEvent(idx, timeRange, coordinationType, leader, memberArrayList.toTypedArray())
            coordinationEventList.add(coordinationEvent)
        }
        return coordinationEventList
    }

    private fun isNoValidStatement(stat: String) : Boolean {
        return stat.isEmpty() || stat.trim()[0] == '#'
    }

    private fun splitAndRemoveNonValidItem(words: String, willRemoveWord: String = " "): List<String> {
        val pureWord = ArrayList<String>()
        val wordList = words.split(willRemoveWord)
        for (word in wordList) {
            if (word.isEmpty() || word.trim().isEmpty()) continue
            pureWord.add(word.trim())
        }
        return pureWord
    }

    private fun preHandleStat(statList: List<String>) : List<String> {
        val retStatList = ArrayList<String>()
        for (stat in statList) {
            if (isNoValidStatement(stat)) continue
            retStatList.add(stat)
        }
        return retStatList
    }

    private fun getTimeRange(timeRangeStat: String) : TimeRange {
        val timeRangeItem = splitAndRemoveNonValidItem(timeRangeStat, "-")
        return if (timeRangeItem.size > 1) {
            TimeRange(timeRangeItem[0].toInt(), timeRangeItem[1].toInt())
        } else {
            TimeRange(timeRangeItem[0].toInt())
        }
    }

    private fun frequencyMapping(frequencyString: String) : Frequency {
        var retFrequency = Frequency.Alpha
        when (frequencyString)  {
            "Alpha" -> retFrequency = Frequency.Alpha
            "Beta" -> retFrequency = Frequency.Beta
            "Gamma" -> retFrequency = Frequency.Gamma
        }
        return retFrequency
    }

    private val deviceTypeMap = hashMapOf<Int, DeviceType>(
        Pair(2, DeviceType.FlightController),
        Pair(3, DeviceType.ESMRadar),
        Pair(4, DeviceType.ActiveRadar),
        Pair(5, DeviceType.GuidanceRadar),
        Pair(6, DeviceType.WarHead)
    )

    private fun environmentEventTypeMapping(environmentTypeString: String) : EnvironmentEType {
        var retEnvironmentType = EnvironmentEType.PositionDrift
        when (environmentTypeString) {
            "PositionDrift" -> retEnvironmentType = EnvironmentEType.PositionDrift
            "PositionReturn" -> retEnvironmentType = EnvironmentEType.PositionReturn
            "ESMHasTarget" -> retEnvironmentType = EnvironmentEType.ESMHasTarget
            "ActiveHasTarget" -> retEnvironmentType = EnvironmentEType.ActiveHasTarget
            "PlatformDisabled" -> retEnvironmentType = EnvironmentEType.PlatformDisabled
            "RandomDisabled" -> retEnvironmentType = EnvironmentEType.RandomDisabled
            "HitTarget" -> retEnvironmentType = EnvironmentEType.HitTarget
        }
        return retEnvironmentType
    }

    private fun coordinationTypeMapping(coordinationTypeString: String) : CoordinationType {
        var retCoordinationType = CoordinationType.FormationKeep
        when (coordinationTypeString) {
            "FormationKeep" -> retCoordinationType = CoordinationType.FormationKeep
            "ESMPositioning" -> retCoordinationType = CoordinationType.ESMPositioning
            "ActiveRadarPositioning" -> retCoordinationType = CoordinationType.ActiveRadarPositioning
            "Guidance" -> retCoordinationType = CoordinationType.Guidance
            "Dismiss" -> retCoordinationType = CoordinationType.Dismiss
        }
        return retCoordinationType
    }

}
