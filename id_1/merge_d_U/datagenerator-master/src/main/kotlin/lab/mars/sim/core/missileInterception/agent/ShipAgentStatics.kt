package lab.mars.sim.core.missileInterception.agent

import lab.mars.windr.agentSimArch.game.Global
import java.util.*

/**
 * Created by imrwz on 8/28/2017.
 */
object ShipAgentStatics {


    private val _currentTargetingUAV = Hashtable<MissileAgent, Int>()

    fun GetCurrentTargetingUAVCount(missileName: String): Int {
        val missileAgent = Global.findAgent(missileName) as MissileAgent
        return _currentTargetingUAV[missileAgent]!!
    }

    fun IncreaseTargetingUAVCount(missileName: String) {
        val missileAgent = Global.findAgent(missileName) as MissileAgent
        val currentValue = _currentTargetingUAV[missileAgent]!! + 1
        _currentTargetingUAV[missileAgent] = currentValue
    }

    fun DecreaseTargetingUAVCount(missileName: String) {
        val missileAgent = Global.findAgent(missileName) as MissileAgent
        val currentValue = _currentTargetingUAV[missileAgent]!! - 1
        _currentTargetingUAV[missileAgent] = currentValue
    }
    enum class ShipAgentStateName {
        UAVTaskAssignProbability, WorkingUAVRePlaningFlag
    }
}