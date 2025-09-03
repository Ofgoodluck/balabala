package lab.mars.sim.core.missileInterception.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics.AgentType
import lab.mars.sim.core.missileInterception.agent.AgentStatics.GenericAgentStateName
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.state.State
import lab.mars.windr.agentSimArch.state.StateMap
import lab.mars.windr.agentSimArch.utility.VectorFactory

/**
 * Created by imrwz on 6/15/2017.
 */
abstract class SceneAgent(
    id: String,
    speed: Float,
    currentPosition: Vector3,
    currentEulerDirection: Vector3,
    agentType: AgentType,
    color: Color
) : Agent(id) {


    init {
        val quaternion = Quaternion()
        quaternion.setEulerAngles(currentEulerDirection.x, currentEulerDirection.y, currentEulerDirection.z)
        val front = VectorFactory.worldFront().mul(quaternion)
        val right = VectorFactory.worldRight().mul(quaternion)
        val top = VectorFactory.worldTop().mul(quaternion)
        val stateMap = StateMap<Enum<*>, State<*>>()
        stateMap[GenericAgentStateName.NextTargetPosition] =
            State<Vector3?>(GenericAgentStateName.NextTargetPosition, null)
        extendStateMap {
            stateMap
                .put(GenericAgentStateName.Name, id)
                .put(GenericAgentStateName.Speed, speed)
                .put(GenericAgentStateName.CurrentPosition, currentPosition)
                .put(GenericAgentStateName.CurrentDirection, currentEulerDirection)
                .put(GenericAgentStateName.IsAlive, true)
                .put(GenericAgentStateName.Type, agentType)
                .put(GenericAgentStateName.Color, color)
                .put(GenericAgentStateName.Front, VectorFactory.add(front, currentPosition))
                .put(GenericAgentStateName.Right, VectorFactory.add(right, currentPosition))
                .put(GenericAgentStateName.Top, VectorFactory.add(top, currentPosition))
        }
    }

}