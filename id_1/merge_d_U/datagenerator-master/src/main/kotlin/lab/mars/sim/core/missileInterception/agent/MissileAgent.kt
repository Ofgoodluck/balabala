package lab.mars.sim.core.missileInterception.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics.AgentType
import lab.mars.sim.core.missileInterception.controller.MissileAutoPilotController
import lab.mars.sim.core.missileInterception.controller.MissileControllerStatics
import lab.mars.sim.core.missileInterception.models.Radar.Radar
import lab.mars.sim.core.missileInterception.models.Radar.RadarSimulationController
import lab.mars.sim.core.missileInterception.script.DeviceType
import lab.mars.sim.core.missileInterception.script.Frequency
import lab.mars.windr.agentSimArch.state.State
import lab.mars.windr.agentSimArch.state.StateMap
import lab.mars.windr.agentSimArch.utility.VectorFactory
import lab.mars.windr.simUtility.component.LocalTransform
import lab.mars.windr.simUtility.component.uav.UAVAutoPilotController
import lab.mars.windr.simUtility.component.uav.UAVFlightControllerParameters

/**
 * Created by imrwz on 6/19/2017.
 */
class MissileAgent(
    id: String,
    speed: Float,
    currentPosition: Vector3,
    currentDirection: Vector3,
    agentType: AgentType,
    intIdx: Int,
    color: Color,
    deviceList : HashSet<DeviceType>,
    warheadProbability : Float,
) : SceneAgent(
    id,
    speed,
    currentPosition,
    currentDirection,
    agentType,
    color
) {

    var positionReset = false
    init {
        val radar =  Radar(
            190000, false, 200000, 2f,
            10f, 100f, 0f
        )
        extendStateMap {
            StateMap<Enum<*>, State<*>>()
                .put(MissileAgentStatics.MissileAgentStateName.Id, intIdx)
                .put(MissileAgentStatics.MissileAgentStateName.Radar, radar)
                .put(MissileAgentStatics.MissileAgentStateName.RadarScannedTargetList, ArrayList<Vector3>())
                .put(MissileAgentStatics.MissileAgentStateName.MissilePilotStatus, MissileAutoPilotController.MissilePilotStatus.Normal)
                .put(MissileAgentStatics.MissileAgentStateName.MissileControlInput, ArrayList<String>())
                .put(MissileAgentStatics.MissileAgentStateName.MissilePredefinedTargets, ArrayList<Vector3>())
                .put(MissileAgentStatics.MissileAgentStateName.ESMRadarData, MissileAgentStatics.ESMRadarData())
                .put(MissileAgentStatics.MissileAgentStateName.ActiveRadarData, MissileAgentStatics.ActiveRadarData())
                .put(MissileAgentStatics.MissileAgentStateName.GuidanceRadarOutputData, MissileAgentStatics.GuidanceRadarOutputData())
                .put(MissileAgentStatics.MissileAgentStateName.DeviceList, deviceList)
                .put(MissileAgentStatics.MissileAgentStateName.WarheadProbability, warheadProbability)
        }
        setDecisionMaker<MissileAgent>(MissileScriptExecutor())
        addOldAutoPilot(id)
        val radarSimulationController = RadarSimulationController.GetInstance()
        radarSimulationController.AddNewRadar(radar, false)
        radar.SetBindedAgent(this)

    }

    private fun addOldAutoPilot(id: String) {
        addComponent<MissileAgent, MissileAutoPilotController>(MissileAutoPilotController(id))
    }

}