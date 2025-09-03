package lab.mars.sim.core.missileInterception.launch

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.graphics.GraphicsWindow
import lab.mars.sim.core.missileInterception.GUIStatics
import lab.mars.sim.core.missileInterception.agent.*
import lab.mars.sim.core.missileInterception.models.DataCollect.DataCollector
import lab.mars.sim.core.missileInterception.models.GUI.*
import lab.mars.sim.core.missileInterception.models.Radar.RadarSimulationController
import lab.mars.sim.core.missileInterception.script.Config
import lab.mars.sim.core.missileInterception.script.DeviceType
import lab.mars.sim.core.missileInterception.script.ScriptTool
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.game.Game
import lab.mars.windr.agentSimArch.game.GameConfiguration
import lab.mars.windr.agentSimArch.game.IVCSCommittee
import lab.mars.windr.agentSimArch.utility.Mathf
import lab.mars.windr.agentSimArch.utility.VectorFactory
import lab.mars.windr.simArchGraphics.Drawer
import lab.mars.windr.simArchGraphics.GUIConfiguration
import lab.mars.windr.simArchGraphics.Graphics
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.system.exitProcess

/**
 * Created by imrwz on 7/10/2017.
 */
object Main {

    private fun generateRandomPos2D(centerPos: Vector2, theta: Float, radius: Float): Vector2 {
        val x = radius * cos(theta)
        val y = radius * sin(theta)
        return Vector2(centerPos.x + x.toFloat(), centerPos.y + y.toFloat())
    }

    private fun addShip(agentList: MutableList<Agent>, drawers: MutableList<Drawer>) {
        val carrierPosition = Vector3(150 * 1.852f, 0f, 50 * 1.852f)
        val shipPositions = Array(4) {
            val theta = Mathf.random(0f + 90f * it, 90f + 90f * it) * Mathf.Deg2Rad
            val radius = Mathf.random(10f, 20f)
            val newPos = generateRandomPos2D(Vector2(carrierPosition.x, carrierPosition.z), theta, radius)
            Vector3(newPos.x, carrierPosition.y, newPos.y)
        }
        val shipRotation = Vector3(0f, -90f, 0f)
        for (i in shipPositions.indices) {
            val thisShipId = "Destroyer$i"
            val shipAgent = ShipAgent(
                thisShipId,
                0f, shipPositions[i], shipRotation, AgentStatics.AgentType.SHIP
            )
            agentList.add(shipAgent)
            drawers.add(
                StaticObjectDrawer(
                    thisShipId,
                    GUIStatics.SimpleSceneShipAssetsPath,
                    GUIStatics.SimpleSceneShipNodeName,
                    0.008f
                )
            )
        }
        val shipAgent = ShipAgent(
            "Carrier1",
            0f, carrierPosition, VectorFactory.worldZero(), AgentStatics.AgentType.SHIP
        )
        agentList.add(shipAgent)
        drawers.add(StaticObjectDrawer("Carrier1", "assets/CARRIER.g3dj", "cv_6", 0.0001f))
    }

    private fun addEnvironment(agentList: MutableList<Agent>, drawers: MutableList<Drawer>) {
        val id = "environment"
        val environmentAgent = EnvironmentAgent(
            id,
            0f,
            VectorFactory.worldZero(),
            VectorFactory.worldZero(),
            AgentStatics.AgentType.ENV
        )
        agentList.add(environmentAgent)
        drawers.add(
            SceneDrawer(
                id,
                GUIStatics.SimpleSceneSeaModelID,
                GUIStatics.SimpleSceneSeaNodeID,
                ceil(200 * 1.852).toInt(),
                ceil(200 * 1.852).toInt(),
                400
            )
        )
        drawers.add(AllRadarDrawer())
    }

    private fun addMissile(
        id: Int,
        position: Vector3, rotation: Vector3, color: Color,
        agentList: MutableList<Agent>, drawers: MutableList<Drawer>,
        deviceList: HashSet<DeviceType>,
        warHeadProbability: Float
    ) {
        val missileAgent = MissileAgent(
            "Missile$id", 2.552f, position, rotation,  //"CurrentDirection" -> ""
            AgentStatics.AgentType.MISSILE, id,
            color, deviceList, warHeadProbability
        )
        agentList.add(missileAgent)
        drawers.add(
            ActiveObjectDrawable(
                "Missile$id",
                GUIStatics.SimpleSceneMissileAssetPath,
                GUIStatics.SimpleSceneMissileNodeName,
                Vector3(0.009f, 0.009f, 0.0015f)
            )
        )
    }


    private fun addMissiles(
        agentList: MutableList<Agent>,
        drawers: MutableList<Drawer>,
        generatedDeviceList: HashMap<Int, HashSet<DeviceType>>,
        warHeadProbability: HashMap<Int, Float>
    ) {
        val random = Random(0)
        val positionBase = Vector3(0f, 399f, 200 * 1.852f)
        val directionBase = Vector3(135f, 45f, 0f)
        val nodeNum = ScriptTool.getPlatformNumber()
        (1..nodeNum).forEach {
            val missilePosition = VectorFactory.add(
                positionBase,
                Vector3(
                    random.nextDouble(-0.1, 0.1).toFloat(),
                    random.nextDouble(-0.1, 0.1).toFloat(),
                    random.nextDouble(-0.1, 0.1).toFloat()
                )
            )
            addMissile(
                it,
                missilePosition,
                directionBase,
                Color.RED,
                agentList,
                drawers,
                generatedDeviceList[it]!!,
                warHeadProbability[it]!!
            )
        }
    }


    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // 由于Config使用init的缘故，所以下面的if代码块必须在第一次调用Config之前执行
        if (args.contains("--script_path")) {
            ScriptTool.scriptPath = args[args.indexOf("--script_path") + 1]
        }
        if (args.contains("--adhoc_endpoint")) {
            Config.adhocIp = args[args.indexOf("--adhoc_endpoint") + 1]
        }
        Config.noGui = args.contains("--no_gui")
        val stepStart = args.contains("--step_start")
        GraphicsWindow.defaultWidth = 0.5f
        GraphicsWindow.maxVertices = 1600000
        val agentList: MutableList<Agent> = ArrayList()
        val drawers: MutableList<Drawer> = ArrayList()
        addEnvironment(agentList, drawers)
        addShip(agentList, drawers)
        addMissiles(agentList, drawers, Config.DeviceList, Config.WarHeadProbability)
        drawers.add(UIDrawer())
        val committees: MutableList<IVCSCommittee> = ArrayList()
        val radarManager: IVCSCommittee = RadarSimulationController.GetInstance()
        val renderer = Graphics.start(GUIConfiguration(drawers, true, true))
        val dataCollector = DataCollector()
        committees.add(radarManager)
        committees.add(dataCollector)
        if (!Config.noGui) {
            committees.add(renderer)
        }
        val configuration = GameConfiguration(1 / 20f, committees) { agentList }
        if (!stepStart) {
            while (true) {
                print("init -> ")
                val input = readln()
                when (input) {
                    "step_start" -> {
                        dataCollector.mode = DataCollector.SimulationMode.SingleStep
                        break
                    }

                    "start" -> break
                    "quit" -> exitProcess(0)
                    else -> println("valid inputs: 'step_start' or 'start'")
                }
            }
        }
        Game.start(configuration, true)
        System.exit(0)
    }
}