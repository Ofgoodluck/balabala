package lab.mars.sim.core.missileInterception.models.GUI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.graphics.*
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.util.Return
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.util.function.Consumer

/**
 * Created by imrwz on 6/20/2017.
 */
class ActiveObjectDrawable(id: String?, assetPack: String?, assetNode: String?, scalar: Vector3) : ObjectDrawer(
    id!!, assetPack!!, assetNode!!
) {
    private var _scalar = Vector3(1.0f, 1.0f, 1.0f)

    init {
        _scalar = scalar
    }

    private fun PaintRecursively(drawable: Drawable, color: Color) {
        if (drawable.parts.size != 0) {
            for (part in drawable.parts) {
                part.material.set(ColorAttribute.createDiffuse(color))
            }
        }
        if (drawable.hasChildren()) {
            for (childDrawable in drawable.children) {
                PaintRecursively(childDrawable, color)
            }
        }
    }

    override fun Initial(state: MutableMap<String, Any>, stepDuration: Float, remaining: Float): Return {
        val agent = Global.findAgent(_id)
        val currentPosition = agent.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
        val drawable = StaticModelDrawable(_assetPack, _assetNode)
        state["${_id}_model"] = drawable
        val text = TextDrawable {
            it.fontScale(0.001f)
            it.setColor(Color.RED)
            it.draw(_id, 0f, 0.2f)
        }
        state["${_id}_model_text"] = text
        text.translation.set(VectorFactory.add(currentPosition, Vector3(0f, 0.1f, 0f)))
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
        val agent = Global.findAgent(_id)
        val currentPosition = agent.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
        val currentDirection = agent.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentDirection)
        val drawable = state["${_id}_model"]!! as StaticModelDrawable
        drawable.translation.set(currentPosition)
        drawable.rotation.setEulerAngles(currentDirection.x, currentDirection.y, currentDirection.z)
        drawable.scale.set(this._scalar)
        val color = agent.getState<Color>(AgentStatics.GenericAgentStateName.Color)
        if (!(agent.getState<Any>(AgentStatics.GenericAgentStateName.IsAlive) as Boolean)) {
            PaintRecursively(drawable, Color.GRAY)
        } else {
            PaintRecursively(drawable, color)
        }
        val target = agent.getState<Any>(AgentStatics.GenericAgentStateName.NextTargetPosition) as Vector3?
        if (target == null) {
            state.remove(_id + "target")
        } else {
            if (color !== Color.WHITE) {
                var targetRay = state[_id + "target"] as MeshDrawable?
                if (targetRay == null) {
                    targetRay = MeshDrawable()
                    state[_id + "target"] = targetRay
                }
                targetRay.rebuildShape{ builder: DrawablePartBuilder ->
                    builder.newPart(ShapeType.Lines).color(color).line(
                        currentPosition.x, currentPosition.y, currentPosition.z,
                        target.x, target.y, target.z
                    )
                }
            } else {
                state.remove(_id + "target")
            }
        }

        val text = state["${_id}_model_text"]!! as TextDrawable
        text.translation.set(VectorFactory.add(currentPosition, Vector3(0f, 0.1f, 0f)))
        val textLine = state["${_id}_model_text_line"]!! as MeshDrawable
        textLine.rebuildShape {
            it.newPart(ShapeType.Lines).color(Color.RED).line(
                currentPosition.x,
                currentPosition.y,
                currentPosition.z,
                text.translation.x,
                text.translation.y,
                text.translation.z
            )
        }
        return Return.Finish
    }
}