package lab.mars.sim.core.missileInterception.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics.AgentType
import lab.mars.windr.agentSimArch.state.State
import lab.mars.windr.agentSimArch.state.StateMap

/**
 * Created by imrwz on 6/19/2017.
 */
class ShipAgent(
    id: String,
    speed: Float,
    currentPosition: Vector3,
    currentDirection: Vector3,
    agentType: AgentType
) : SceneAgent(id, speed, currentPosition, currentDirection, agentType, Color.GREEN) {
    init {
        extendStateMap {
            StateMap<Enum<*>?, State<*>?>()
                .put(
                    ShipAgentStatics.ShipAgentStateName.UAVTaskAssignProbability,
                    HashMap<String?, HashMap<String?, Float?>?>()
                )
                .put(ShipAgentStatics.ShipAgentStateName.WorkingUAVRePlaningFlag, false)
        }
//        setDecisionMaker<ShipAgent>(ShipDecisionMaker())
    }

    companion object {
        const val ShipPheromoneGain = 0.1f
    }
}