package lab.mars.sim.core.missileInterception.models.GUI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.graphics.MeshDrawable
import lab.mars.sim.core.graphics.ShapeType
import lab.mars.sim.core.graphics.StaticModelDrawable
import lab.mars.sim.core.missileInterception.GUIStatics
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.missileInterception.agent.EnvironmentAgent
import lab.mars.sim.core.missileInterception.agent.MissileAgent
import lab.mars.sim.core.missileInterception.agent.ShipAgent
import lab.mars.sim.core.util.Return
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.agentSimArch.utility.VectorFactory


/**
 * Created by imrwz on 2017/7/16.
 */
class SceneDrawer(
    id: String,
    assetPack: String,
    assetNode: String,
    val seaWidth: Int,
    val seaLength: Int,
    val sceneHeight: Int
) : ObjectDrawer(
    id, assetPack, assetNode
) {

    private lateinit var allMissiles: List<Agent>
    private lateinit var allShips: List<Agent>
    private lateinit var allAgents: List<Agent>

    private fun generateSeaPlaneTiles(state: MutableMap<String, Any>) {
        val planeWStart = -(seaWidth / 2) - 10
        val planeLStart = -(seaLength / 2) - 10
        val planeWEnd = (seaWidth / 2) + 10
        val planeLEnd = (seaLength / 2) + 10
        val planePlaceStep = 10
        for (w in (planeWStart until planeWEnd) step planePlaceStep) {
            for (l in (planeLStart until planeLEnd) step planePlaceStep) {
                val sea = StaticModelDrawable(GUIStatics.SimpleSceneSeaModelID, GUIStatics.SimpleSceneSeaNodeID)
                state["sea_${w}_${l}"] = sea
                sea.scale.set(planePlaceStep.toFloat(), 1f, planePlaceStep.toFloat())
                sea.translation.set(w.toFloat(), -0.51f, l.toFloat())
            }
        }
    }

    private fun generateOneSeaPlane(state: MutableMap<String, Any>) {
        val sea = StaticModelDrawable(GUIStatics.SimpleSceneSeaModelID, GUIStatics.SimpleSceneSeaNodeID)
        state["sea"] = sea
        sea.scale.set(seaWidth * 10f, 1f, seaLength * 10f)
        sea.translation.set(seaWidth.toFloat(), -0.51f, seaLength.toFloat())
    }

    override fun Initial(state: MutableMap<String, Any>, stepDuration: Float, remaining: Float): Return {
        allMissiles = Global.findAgents(MissileAgent::class.java)
        allShips = Global.findAgents(ShipAgent::class.java)
        allAgents = allMissiles
        val cc = state["CAMERA_INPUT_CONTROLLER"] as CameraInputController
        cc.camera.position.set(0f, 400f, 201 * 1.852f)
        cc.camera.lookAt(Vector3(150 * 1.852f, 0f, 50 * 1.852f))
        cc.camera.update()
        cc.scrollFactor = -0.1f
        cc.translateUnits = 2f
        cc.rotateAngle = 180f
        val sceneRegion = MeshDrawable()
        state["sceneRegion"] = sceneRegion
        val axis = MeshDrawable()
        state["worldAxis"] = axis
        axis.rebuildShape {
            it.newPart(ShapeType.Lines).color(Color.RED).line(0f, 0f, 0f, 1f, 0f, 0f)
                .color(Color.GREEN).line(0f, 0f, 0f, 0f, 1f, 0f).color(Color.BLUE)
                .line(0f, 0f, 0f, 0f, 0f, 1f)
        }
        sceneRegion.rebuildShape {
            it.newPart(ShapeType.Lines).color(Color.RED).box_line(
                0f,
                sceneHeight.toFloat() / 2,
                0f,
                seaWidth.toFloat(),
                sceneHeight.toFloat(),
                seaLength.toFloat()
            )
            it.newPart(ShapeType.Lines).setMaterial(Material(BlendingAttribute())).color(Color.GRAY)
            for (w in (-seaWidth / 2 until seaWidth / 2) step 10) {
                for (l in (-seaLength / 2 until seaLength / 2) step 10) {
                    it.rect_line(
                        w.toFloat() + 0.5f, 0.1f, l.toFloat() + 0.5f,
                        w.toFloat() + 10.5f, 0.1f, l.toFloat() + 0.5f,
                        w.toFloat() + 10.5f, 0.1f, l.toFloat() + 10.5f,
                        w.toFloat() + 0.5f, 0.1f, l.toFloat() + 10.5f
                    )
                }
            }
        }
        sceneRegion.translation.set(seaWidth.toFloat() / 2, 0f, seaLength.toFloat() / 2)
//        generateSeaPlaneTiles(state)
        generateOneSeaPlane(state)
        return Return.Finish
    }

    var followingMissileId = 0
    var followingShipId = 0
    var followingAgentId = 0
    var followingMode = false


    override fun ReDraw(state: MutableMap<String, Any>, stepDuration: Float, remaining: Float): Return {
        val cc = state["CAMERA_INPUT_CONTROLLER"] as CameraInputController
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            cc.scrollFactor = -0.01f
            cc.translateUnits = 2f
            cc.rotateAngle = 180f
        } else {
            cc.scrollFactor = -0.1f
            cc.translateUnits = 10f
            cc.rotateAngle = 360f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            (Global.findAgent("environment") as EnvironmentAgent).paused = true
        }
        if (Gdx.input.isKeyPressed(Input.Keys.F)) {
            followingMode = false
        }
        if (Gdx.input.isKeyPressed(Input.Keys.M)) {
            allAgents = allMissiles
            followingMode = true
        } else if (Gdx.input.isKeyPressed(Input.Keys.C)) {
            allAgents = allShips
            followingMode = false
            val carrier = allShips.findLast { it.id.toString() == "Carrier1" }!!
            val carrierPos = carrier.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            val cameraPos = carrierPos.cpy()
            cameraPos.y += 10f
            cc.camera.position.set(cameraPos)
            cc.camera.lookAt(carrierPos)
            cc.camera.update()

        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            followingAgentId -= 1
            if (followingAgentId < 0) {
                followingAgentId = allMissiles.size - 1
            }

        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            followingAgentId += 1
            if (followingAgentId >= allAgents.size) {
                followingAgentId = 0
            }
        }
        if (followingMode) {
            val agent = allAgents[followingAgentId]
            val front = agent.getState<Vector3>(AgentStatics.GenericAgentStateName.Front)
            val top = agent.getState<Vector3>(AgentStatics.GenericAgentStateName.Top)
            val pos = agent.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            followingMode = true
            cc.camera.position.set(
                VectorFactory.subtract(
                    pos,
                    VectorFactory.normalize(VectorFactory.subtract(front, pos))
                )
            )
            cc.camera.lookAt(pos)
            cc.camera.up.set(Vector3.Y)
            cc.camera.update()
        }
        return Return.Finish
    }
}