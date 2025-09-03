package lab.mars.sim.core.missileInterception.controller

import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.missileInterception.agent.MissileAgent
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics
import lab.mars.windr.agentSimArch.component.Component
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by 冉宝宝 on 7/2/2017.
 */
class MissileAutoPilotController(id: String?) : Component(id) {
    enum class MissilePilotStatus {
        BeginDrift,
        Drifting,
        EndDrift,
        Normal
    }

    private val _approachDistance = 0.01f
    private var _nextTarget: Vector3? = null
    private var _reachedTarget = false
    private lateinit var _flightControlScript: MissileFlightCalculator
    private val _updateKeySet = ArrayList<String>()
    private lateinit var _curPosition: Vector3
    private var started = false
    private lateinit var random: Random
    private var needToReloadFlightController = false

    override fun run_once() {
        if (!getState<Boolean>(AgentStatics.GenericAgentStateName.IsAlive)) {
            return
        }
        if (!started || needToReloadFlightController) {
            started = true
            needToReloadFlightController = false
            _flightControlScript = MissileFlightCalculator(bindAgent)
            random = Random(getState<Int>(MissileAgentStatics.MissileAgentStateName.Id).toLong())
        }
        val positionRest = (bindAgent as MissileAgent).positionReset
        if (positionRest) {
            (bindAgent as MissileAgent).positionReset = false
            needToReloadFlightController = true
        } else {
            Update()
        }
    }

    private fun AdjustVectors() {
        val currentPosition = getState<Any>(AgentStatics.GenericAgentStateName.CurrentPosition) as Vector3
        val front = getState<Any>(AgentStatics.GenericAgentStateName.Front) as Vector3
        val right = getState<Any>(AgentStatics.GenericAgentStateName.Right) as Vector3
        val top = getState<Any>(AgentStatics.GenericAgentStateName.Top) as Vector3
        _flightControlScript.frontV = VectorFactory.subtract(front, currentPosition)
        _flightControlScript.rightV = VectorFactory.subtract(right, currentPosition)
        _flightControlScript.topV = VectorFactory.subtract(top, currentPosition)
        _flightControlScript.MissileVectorAdjust()
    }

    private fun AutoHeightAdjust(): Boolean {
        return true
    }

    private fun AutoYaw(nextTarget: Vector3?) {
        val top = VectorFactory.subtract(getState<Any>(AgentStatics.GenericAgentStateName.Top) as Vector3, _curPosition)
        val target = VectorFactory.projectOnPlane(VectorFactory.subtract(nextTarget, _curPosition), top)
        var planeFrontVector =
            VectorFactory.subtract(getState<Any>(AgentStatics.GenericAgentStateName.Front) as Vector3, _curPosition)
        planeFrontVector = VectorFactory.projectOnPlane(planeFrontVector, top)
        val angle = VectorFactory.angle(planeFrontVector, target)
        if (angle > MissileControllerStatics.MaxApproximatelyEqualAngleDiff) {
            if (angle > 90) {
                _updateKeySet.add(MissileControllerStatics.YawRightKey)
            } else {
                if (VectorFactory.cross(target, planeFrontVector).y < 0) {
                    _updateKeySet.add(MissileControllerStatics.YawRightKey)
                } else {
                    _updateKeySet.add(MissileControllerStatics.YawLeftKey)
                }
            }
        }
        AdjustVectors()
    }

    private fun AutoPitch() {
        val A = _curPosition
        var B = getState<Vector3>(AgentStatics.GenericAgentStateName.Front)
        var C = _nextTarget
        var D = getState<Vector3>(AgentStatics.GenericAgentStateName.Right)
        D = VectorFactory.subtract(D, A)
        B = VectorFactory.subtract(B, A)
        C = VectorFactory.subtract(C, A)
        C = VectorFactory.projectOnPlane(C, D)
        B = VectorFactory.normalize(B)
        C = VectorFactory.normalize(C)
        val delta = VectorFactory.subtract(C, B)
        if (delta.len() >  0.002f) {
            if (delta.y > 0f) {
                _updateKeySet.add(MissileControllerStatics.PitchUpKey)
            } else {
                _updateKeySet.add(MissileControllerStatics.PitchDownKey)
            }
        }
        AdjustVectors()
    }

    private fun AutoFollowPath() {
        if (VectorFactory.distance(_nextTarget, _curPosition) > _approachDistance) {
            AutoYaw(_nextTarget)
            if (AutoHeightAdjust()) {
                AutoPitch()
            }
        } else {
            _reachedTarget = true
        }
    }

    fun Control() {
        if (_nextTarget != null) {
            if (_updateKeySet.isEmpty()) {
                AutoFollowPath()
            }
        }
        val controlInputs = getState<ArrayList<String>>(MissileAgentStatics.MissileAgentStateName.MissileControlInput)
        controlInputs.addAll(_updateKeySet)
        _flightControlScript.SetKeys(_updateKeySet)
        _flightControlScript.DoControl()
        _updateKeySet.clear()
    }

    fun Update() {
        _curPosition = getState(AgentStatics.GenericAgentStateName.CurrentPosition)
        val pilotStatus = getState<MissilePilotStatus>(MissileAgentStatics.MissileAgentStateName.MissilePilotStatus)
        when (pilotStatus) {
            MissilePilotStatus.BeginDrift -> {
                when (random.nextInt(4)) {
                    0 -> _updateKeySet.add(MissileControllerStatics.PitchUpKey)
                    1 -> _updateKeySet.add(MissileControllerStatics.PitchDownKey)
                    2 -> _updateKeySet.add(MissileControllerStatics.YawLeftKey)
                    3 -> _updateKeySet.add(MissileControllerStatics.YawRightKey)
                }
                setState(MissileAgentStatics.MissileAgentStateName.MissilePilotStatus, MissilePilotStatus.Drifting)
                _nextTarget = null
            }

            MissilePilotStatus.Drifting -> {
                _nextTarget = null
            }

            MissilePilotStatus.EndDrift -> {
                setState(MissileAgentStatics.MissileAgentStateName.MissilePilotStatus, MissilePilotStatus.Normal)
                val nextTarget = getState<Vector3?>(AgentStatics.GenericAgentStateName.NextTargetPosition)
                if (nextTarget != null && nextTarget != _nextTarget) {
                    _reachedTarget = false
                }
                _nextTarget = nextTarget
            }

            MissilePilotStatus.Normal -> {
                val nextTarget = getState<Vector3?>(AgentStatics.GenericAgentStateName.NextTargetPosition)
                if (nextTarget != null && nextTarget != _nextTarget) {
                    _reachedTarget = false
                }
                _nextTarget = nextTarget
            }
        }
        Control()
    }
}