package lab.mars.sim.core.missileInterception.models.DataCollect

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.missileInterception.agent.EnvironmentAgent
import lab.mars.sim.core.missileInterception.agent.MissileAgent
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics
import lab.mars.sim.core.missileInterception.controller.MissileControllerStatics
import lab.mars.sim.core.missileInterception.models.DataCollect.app.*
import lab.mars.sim.core.missileInterception.models.DataSender.*
import lab.mars.sim.core.missileInterception.script.*
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.game.Game
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.agentSimArch.game.IVCSCommittee
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun Vector3.format(digits: Int): String {
    return "{${x.format(digits)}, ${y.format(digits)}, ${z.format(digits)}}"
}

fun Float.format(digits: Int) = "%.${digits}f".format(this)

class DataCollector : IVCSCommittee {

    enum class SimulationMode {
        SingleStep, //pause at every second
        Normal //run normally
    }

    val dataFolder: File
    lateinit var flightControlDataFile: HashMap<Int, FileOutputStream>
    lateinit var esmRadarDataFile: HashMap<Int, FileOutputStream>
    lateinit var activeRadarDataFile: HashMap<Int, FileOutputStream>
    lateinit var guidanceRadarDataFile: HashMap<Int, FileOutputStream>
    lateinit var coordinationDataFile: FileOutputStream
    lateinit var udpDataSenders: HashMap<Int, Sender>
    lateinit var SNAppSender: TCPServer
    val adhocTopologySender = AdhocSimulatorInfoClient
    var mode = SimulationMode.Normal
    var nextPauseTime = 0
    var resized = false
    private val allMissiles = HashMap<Int, Agent>()

    init {
        val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-z")
        val date = Date(System.currentTimeMillis())
        val timeNow = formatter.format(date)
        dataFolder = File(timeNow)
        dataFolder.mkdir()
        adhocTopologySender.start()
        SNAppSender = TCPServer(Config.SNAppPort, SNAppChannel::class.java)
        SNAppSender.open()
        Config.XXAddresses.forEach {
            val sender = UdpCommunicator(56789, it.value, 15467, null)
            sender.send("reset".encodeToByteArray())
            sender.terminate()
        }
    }

    override fun init() {
        flightControlDataFile = initFlightControlDataFile(dataFolder)
        esmRadarDataFile = initESMRadarData(dataFolder)
        activeRadarDataFile = initActiveRadarData(dataFolder)
        guidanceRadarDataFile = initGuidanceRadarData(dataFolder)
        coordinationDataFile = initCoordinationDataFile(dataFolder)
        udpDataSenders = initSenders(Config.XXAddresses, hashMapOf())
    }

    val writingFiles = hashMapOf<CoordinationEvent, FileOutputStream>()

    var currentTime = -1

    private fun collectDeviceData() {
        allMissiles.forEach { (id, it) ->
            val position = it.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            val direction = it.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentDirection)
            val speed = it.getState<Float>(AgentStatics.GenericAgentStateName.Speed)
            val flightControlInput =
                it.getState<ArrayList<String>>(MissileAgentStatics.MissileAgentStateName.MissileControlInput)
            var pitchControl = 0
            var yawControl = 0
            flightControlInput.forEach { key ->
                if (key == MissileControllerStatics.PitchUpKey) {
                    pitchControl += 1
                }
                if (key == MissileControllerStatics.PitchDownKey) {
                    pitchControl += 1
                }
                if (key == MissileControllerStatics.YawLeftKey) {
                    yawControl += 1
                }
                if (key == MissileControllerStatics.YawRightKey) {
                    yawControl += 1
                }
            }
            flightControlInput.clear()
            val flightControlColumn = FlightControlColumn(
                position.x,
                position.z,
                position.y,
                direction.x,
                direction.y,
                speed,
                yawControl,
                pitchControl
            )
            val sender = udpDataSenders[id]!!
            writeFlightControlData(flightControlDataFile[id]!!, flightControlColumn)
            sender.sendFlightControlData(flightControlColumn)
            if (esmRadarDataFile.containsKey(id)) {
                val data = it.getState<ESMRadarData>(MissileAgentStatics.MissileAgentStateName.ESMRadarData)
                if (data.status == MissileAgentStatics.DeviceStatus.On) {
                    writeESMRadarData(esmRadarDataFile[id]!!, data)
                    sender.sendESMRadarData(data)
                }
            }
            if (activeRadarDataFile.containsKey(id)) {
                val data = it.getState<ActiveRadarData>(MissileAgentStatics.MissileAgentStateName.ActiveRadarData)
                if (data.status == MissileAgentStatics.DeviceStatus.On) {
                    writeActiveRadarData(activeRadarDataFile[id]!!, data)
                    sender.sendActiveRadarData(data)
                }
            }
            if (guidanceRadarDataFile.containsKey(id)) {
                val data =
                    it.getState<GuidanceRadarData>(MissileAgentStatics.MissileAgentStateName.GuidanceRadarOutputData)
                if (data.status == MissileAgentStatics.DeviceStatus.On) {
                    writeGuidanceRadarData(guidanceRadarDataFile[id]!!, data)
                    sender.sendGuidanceRadarData(data)
                }
            }
            sender.sendTimeData(CurrentTimeData(currentTime.toULong()))
        }
    }

    private fun writeFormationControlDataToFile(event: CoordinationEvent) {
        if (!writingFiles.containsKey(event)) {
            initFormationKeepDataFile(dataFolder, event)
        }
        val file = writingFiles[event] ?: return
        val lead = Global.findAgent("Missile${event.lead}") as MissileAgent
        val leadPredefinedTargets =
            lead.getState<ArrayList<Vector3>>(MissileAgentStatics.MissileAgentStateName.MissilePredefinedTargets)
        val leadTarget = leadPredefinedTargets[currentTime]
        val leadPosition = lead.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
        val leadOffset = VectorFactory.subtract(leadPosition, leadTarget)
        val leadColumns = FormationKeepLeadColumns(
            leadTarget.x,
            leadTarget.z,
            leadTarget.y,
            leadOffset.x,
            leadOffset.z,
            leadOffset.y
        )
        val followerColumns = HashMap<Int, FormationKeepFollowerColumns>()
        event.followers.forEach { followerId ->
            val follower = Global.findAgent("Missile$followerId") as MissileAgent
            val followerPosition =
                follower.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            val predefinedDistance =
                MissileAgentStatics.FormationKeepFollowerPositionOffsets[followerId]!!.len()
            val actualDistance = VectorFactory.distance(followerPosition, leadPosition)
            val difference = actualDistance - predefinedDistance
            followerColumns[followerId] = FormationKeepFollowerColumns(
                predefinedDistance,
                actualDistance,
                difference
            )
        }
        writeFormationControlData(file, leadColumns, followerColumns)
    }

    private fun sendAppData(appData: AppData, lead: Int, followers: Array<Int>) {
        val sender = udpDataSenders[lead]!!
        sender.sendAppData(appData)
        followers.forEach {
            val sender = udpDataSenders[it]!!
            sender.sendAppData(appData)
        }
    }

    private fun collectCoordinationEventData() {
        val coordinationEvents = Config.timeLine.getCurrentCoordinationEvents()
        coordinationEvents.forEach { event ->
            val coordData = CoordinationData(event.type.name, event.lead, event.followers)
            writeCoordinationDataFile(coordinationDataFile, coordData)
            if (event.type == CoordinationType.FormationKeep) {
                writeFormationControlDataToFile(event)
            }
            if (currentTime == event.time.from) {
                when (event.type) {
                    CoordinationType.FormationKeep -> {
                        val data = generateFormationKeepData(allMissiles, event)
                        sendAppData(data, event.lead, event.followers)
                    }

                    CoordinationType.Dismiss -> {
                        val data = generateDismissData(allMissiles, event)
                        sendAppData(data, event.lead, event.followers)
                    }

                    CoordinationType.ESMPositioning -> {
                        val data = ESMPositioningData(event.idx, event.lead, event.followers, 2UL)
                        sendAppData(data, event.lead, event.followers)
                    }

                    CoordinationType.ActiveRadarPositioning -> {
                        val data = generateActiveRadarPositioningData(event, 2UL)
                        sendAppData(data, event.lead, event.followers)
                    }

                    CoordinationType.Guidance -> {
                        val data = GuidanceData(event.idx, event.lead, event.followers, 4)
                        sendAppData(data, event.lead, event.followers)
                    }
                }
            }
        }
    }

    private fun collectCyberspaceEventData() {
        val cyberSpaceEvent = Config.timeLine.getCurrentCyberSpaceEvent()
        if (cyberSpaceEvent != null) {
            val top = cyberSpaceEvent.topology
            val positions = recursivelyGeneratePositions(1, top)
            adhocTopologySender.updateGraph(top, positions)
            return
        }
        val envEvents = Config.timeLine.getCurrentEnvironmentEvents()
        val currentGraph = adhocTopologySender.currentGraph
        var topologyChanged = false
        envEvents.forEach {
            if (it.type == EnvironmentEType.PlatformDisabled || it.type == EnvironmentEType.HitTarget) {
                currentGraph.remove(it.affectedPlatform)
                currentGraph.forEach { _, set ->
                    set.remove(it.affectedPlatform)
                }
                topologyChanged = true
            }
        }
        if (topologyChanged) {
            val positions = recursivelyGeneratePositions(8, currentGraph)
            adhocTopologySender.updateGraph(currentGraph, positions)
        }
    }

    private fun handleSimulationStep() {
        while (true) {
            try {
                print("paused @ $currentTime -> ")
                val input = readln()
                if (input == "continue" || input == "c") {
                    mode = SimulationMode.Normal
                    break
                }
                val inputs = input.split(" ")
                if (inputs[0] == "step" || inputs[0] == "s") {
                    nextPauseTime = if (inputs.size == 1) {
                        currentTime + 1
                    } else {
                        currentTime + inputs[1].toInt()
                    }
                    break
                }

            } catch (exp: Exception) {
                println("valid inputs: step <num of seconds>")
                continue
            }
        }
    }

    private fun sendUIData(now: Int) {
        val currentEnvironmentEvents = Config.timeLine.getCurrentEnvironmentEvents().map { it.toString() }
        val currentOnLineNodes = adhocTopologySender.currentGraph.keys.toHashSet()
        val currentCoordEvents = Config.timeLine.getCurrentCoordinationEvents()
        val appLists = hashMapOf<Int, ArrayList<CoordinationType>>()
        currentCoordEvents.forEach { event ->
            if (event.type == CoordinationType.Dismiss) {
                return@forEach
            }
            if (!appLists.containsKey(event.lead)) {
                appLists[event.lead] = ArrayList()
            }
            appLists[event.lead]!!.add(event.type)
            event.followers.forEach {
                if (!appLists.containsKey(it)) {
                    appLists[it] = ArrayList()
                }
                appLists[it]!!.add(event.type)
            }
        }
        val allEventData = ArrayList<CoordGroupData>()
        Config.timeLine.getCurrentCoordinationEvents().forEach { event ->
            if (event.type == CoordinationType.Dismiss) {
                return@forEach
            }
            val generator = SNGenerators[event.type]!!
            val leadIdx = event.lead
            val leadInfo = NodeUIData(
                leadIdx,
                "online",
                generator.getLeadNotifierList(leadIdx, event.followers),
                generator.getLeadSubscriberList(leadIdx, event.followers),
                appLists[leadIdx]!!
            )
            val groupMemberInfos = arrayListOf<NodeUIData>()
            groupMemberInfos.add(leadInfo)
            event.followers.forEach {
                val followerInfo = NodeUIData(
                    it,
                    "online",
                    generator.getFollowerNotifierList(leadIdx, it),
                    generator.getFollowerSubscriberList(leadIdx, it),
                    appLists[it]!!
                )
                groupMemberInfos.add(followerInfo)
            }
            val data = CoordGroupData(event.type.name, event.idx, 0, groupMemberInfos)
            allEventData.add(data)
        }
        val data = UIData(now, currentEnvironmentEvents, currentOnLineNodes, allEventData)
        val dataJson = gson.toJson(data)
        val snFile = File(dataFolder, "time_${now}.json")
        snFile.writeText(dataJson)
        SNAppChannel.activeChannel?.send(dataJson)
    }

    override fun record(allStateMapContainer: MutableMap<String, Any>?) {
        if (allMissiles.isEmpty()) {
            val missiles = Global.findAgents(MissileAgent::class.java)
            runBlocking {
                missiles.forEach {
                    val id: Int = it.getState(MissileAgentStatics.MissileAgentStateName.Id)
                    launch {
                        val deviceList =
                            it.getState<HashSet<DeviceType>>(MissileAgentStatics.MissileAgentStateName.DeviceList)
                        udpDataSenders[id]!!.sendDeviceRegisterMessage(deviceList)
                    }
                    allMissiles[id] = it
                }
            }

        }
        if (!Config.noGui && !resized) {
            resized = true
            Gdx.graphics.setResizable(false)
            Gdx.graphics.setWindowedMode(1920, 1080)
        } else {
            Thread.sleep(5)
        }
        val now = secondNow()
        if (now == currentTime) {
            return
        }
        if (now >= Config.timeLine.endTimeSecond) {
            Game.pause()
            Thread.sleep(Long.MAX_VALUE)
            return
        }
        println(now)
        currentTime = now

        collectDeviceData()
        collectCoordinationEventData()
        collectCyberspaceEventData()
        sendUIData(currentTime)
        val envAgent = Global.findAgent("environment") as EnvironmentAgent

        if (envAgent.paused) {
            envAgent.paused = false
            mode = SimulationMode.SingleStep
            handleSimulationStep()
        } else if (mode == SimulationMode.SingleStep && now == nextPauseTime) {
            handleSimulationStep()
        }
    }


    override fun commit() {
    }
}