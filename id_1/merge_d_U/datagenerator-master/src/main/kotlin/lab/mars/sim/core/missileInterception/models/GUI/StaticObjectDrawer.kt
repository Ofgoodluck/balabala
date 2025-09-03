package lab.mars.sim.core.missileInterception.models.GUI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.graphics.MeshDrawable
import lab.mars.sim.core.graphics.ShapeType
import lab.mars.sim.core.graphics.StaticModelDrawable
import lab.mars.sim.core.graphics.TextDrawable
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.util.Return
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.agentSimArch.utility.VectorFactory


/**
 * Created by imrwz on 7/10/2017.
 */
class StaticObjectDrawer : ObjectDrawer {
    private var scalar = 0.35f

    constructor(id: String, assetPack: String, assetNode: String) : super(id, assetPack, assetNode)
    constructor(id: String, assetPack: String, assetNode: String, scalar: Float) : super(id, assetPack, assetNode) {
        this.scalar = scalar
    }

    override fun Initial(state: MutableMap<String, Any>, stepDuration: Float, remaining: Float): Return {
        val agent = Global.findAgent(_id)
        val currentPosition = agent.getState<Any>(AgentStatics.GenericAgentStateName.CurrentPosition) as Vector3
        val currentDirection = agent.getState<Any>(AgentStatics.GenericAgentStateName.CurrentDirection) as Vector3
        val drawable = StaticModelDrawable(_assetPack, _assetNode)
        state["${_id}_model"] = drawable
        drawable.translation.set(currentPosition)
        drawable.scale.set(scalar, scalar, scalar)
        val quaternion = Quaternion()
        quaternion.setEulerAngles(currentDirection.x, currentDirection.y, currentDirection.z)
        drawable.rotation.set(quaternion)
        val text = TextDrawable {
            it.fontScale(0.1f)
            it.setColor(Color.RED)
            it.draw("${_id}(x=${currentPosition.x},y=${currentPosition.y},z=${currentPosition.z})", 0f, 0.1f)
        }
        state["${_id}_model_text"] = text
        if (_id == "Carrier1") {
            text.translation.set(VectorFactory.add(currentPosition, Vector3(0f, 50f, 0f)))
        } else {
            text.translation.set(VectorFactory.add(currentPosition, Vector3(0f, 30f, 0f)))
        }
        val textLine = MeshDrawable {
            it.newPart(ShapeType.Lines).color(Color.RED).line(
                currentPosition.x,
                currentPosition.y,
                currentPosition.z,
                text.translation.x,
                text.translation.y,
                text.translation.z
            )
        }
        state["${_id}_model_text_line"] = textLine
        return Return.Finish
    }

    override fun ReDraw(state: MutableMap<String, Any>, stepDuration: Float, remaining: Float): Return {
        val cc = state["CAMERA_INPUT_CONTROLLER"] as CameraInputController
        val text = state["${_id}_model_text"]!! as TextDrawable
        val camera = cc.camera
        return Return.Finish
    }
}