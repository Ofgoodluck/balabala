package lab.mars.sim.core.missileInterception.agent

import lab.mars.sim.core.missileInterception.script.initializeTimeLine

/**
 * Created by imrwz on 6/16/2017.
 */
object AgentStatics {
    enum class AgentType {
        UAV, MISSILE, SHIP, ENV
    }

    enum class GenericAgentStateName {
        Name, NextTargetPosition, Speed, CurrentPosition, CurrentDirection, IsAlive, Type, Front, Right, Top, Color,
    }
}