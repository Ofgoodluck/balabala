package lab.mars.sim.core.missileInterception.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics.AgentType

/**
 * Created by imrwz on 7/13/2017.
 */
class EnvironmentAgent(
    id: String,
    speed: Float,
    currentPosition: Vector3,
    currentEulerDirection: Vector3,
    agentType: AgentType
) : SceneAgent(
    id, speed, currentPosition, currentEulerDirection, agentType, Color.BLUE
) {
    public var paused = false
}