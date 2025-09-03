package lab.mars.sim.core.missileInterception.controller

import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.agentSimArch.utility.Mathf
import lab.mars.windr.agentSimArch.utility.VectorFactory

/**
 * Created by imrwz on 7/13/2017.
 */
class MissileFlightCalculator(private val _agentInstance: Agent) {
    lateinit var frontV: Vector3
    lateinit var rightV: Vector3
    lateinit var topV: Vector3
    lateinit var bTOP: Vector3
    var speed = 0f
    var crashed = false
    var keys: MutableList<String> = ArrayList()
    private var _des: Vector3? = null
    private var deltaTime = 0f

    init {
        Init()
    }

    fun SetKeys(key: MutableList<String>) {
        keys = key
    }

    private fun GetKey(key: String): Boolean {
        return keys.contains(key)
    }

    private fun InitialVectors() {
        val currentPosition =
            _agentInstance.getState<Any>(AgentStatics.GenericAgentStateName.CurrentPosition) as Vector3
        frontV = VectorFactory.subtract(
            _agentInstance.getState<Any>(AgentStatics.GenericAgentStateName.Front) as Vector3,
            currentPosition
        )
        rightV = VectorFactory.subtract(
            _agentInstance.getState<Any>(AgentStatics.GenericAgentStateName.Right) as Vector3,
            currentPosition
        )
        topV = VectorFactory.subtract(
            _agentInstance.getState<Any>(AgentStatics.GenericAgentStateName.Top) as Vector3,
            currentPosition
        )
        bTOP = VectorFactory.cross(frontV, Vector3(rightV.x, 0f, rightV.z))
        speed = _agentInstance.getState<Any>(AgentStatics.GenericAgentStateName.Speed) as Float
    }

    private fun MissileBodyControl() {
        if (VectorFactory.angle(bTOP, topV) <= 100.0f) {
            if (GetKey(MissileControllerStatics.PitchDownKey)) {
                if (VectorFactory.angle(bTOP, VectorFactory.worldTop()) <= MissileControllerStatics.MaxPitchAngle ||
                    VectorFactory.angle(rightV, VectorFactory.cross(bTOP, VectorFactory.worldTop())) < 90
                ) {
                    bTOP =
                        VectorFactory.lerp(bTOP, frontV, deltaTime * MissileControllerStatics.MaxPitchAngularVelocityPerSecond)
                    topV =
                        VectorFactory.lerp(topV, frontV, deltaTime * MissileControllerStatics.MaxPitchAngularVelocityPerSecond)
                    frontV = VectorFactory.cross(rightV, topV)
                }
            }
            if (GetKey(MissileControllerStatics.PitchUpKey)) {
                if (VectorFactory.angle(bTOP, VectorFactory.worldTop()) <= MissileControllerStatics.MaxPitchAngle ||
                    VectorFactory.angle(rightV, VectorFactory.cross(bTOP, VectorFactory.worldTop())) > 90
                ) {
                    bTOP = VectorFactory.lerp(
                        bTOP,
                        VectorFactory.worldZero().sub(frontV),
                        deltaTime * MissileControllerStatics.MaxPitchAngularVelocityPerSecond
                    )
                    topV = VectorFactory.lerp(
                        topV,
                        VectorFactory.worldZero().sub(frontV),
                        deltaTime * MissileControllerStatics.MaxPitchAngularVelocityPerSecond
                    )
                    frontV = VectorFactory.cross(rightV, topV)
                }
            }
        }
    }

    private fun MissileTurns() {
        if (GetKey(MissileControllerStatics.YawLeftKey)) {
            val X = deltaTime * MissileControllerStatics.MaxTurnAngularVelocityPerSecond * Mathf.Deg2Rad
            var tmp: Vector3
            var xx: Float
            var zz: Float
            xx = bTOP.x * Mathf.cos(X) - bTOP.z * Mathf.sin(X)
            zz = bTOP.x * Mathf.sin(X) + bTOP.z * Mathf.cos(X)
            tmp = Vector3(xx, bTOP.y, zz)
            bTOP = tmp
            xx = frontV.x * Mathf.cos(X) - frontV.z * Mathf.sin(X)
            zz = frontV.x * Mathf.sin(X) + frontV.z * Mathf.cos(X)
            tmp = Vector3(xx, frontV.y, zz)
            frontV = tmp
            xx = rightV.x * Mathf.cos(X) - rightV.z * Mathf.sin(X)
            zz = rightV.x * Mathf.sin(X) + rightV.z * Mathf.cos(X)
            tmp = Vector3(xx, rightV.y, zz)
            rightV = tmp
            xx = topV.x * Mathf.cos(X) - topV.z * Mathf.sin(X)
            zz = topV.x * Mathf.sin(X) + topV.z * Mathf.cos(X)
            tmp = Vector3(xx, topV.y, zz)
            topV = tmp
        }
        if (GetKey(MissileControllerStatics.YawRightKey)) {
            val X = -deltaTime * MissileControllerStatics.MaxTurnAngularVelocityPerSecond * Mathf.Deg2Rad
            var tmp: Vector3
            var xx: Float
            var zz: Float
            xx = bTOP.x * Mathf.cos(X) - bTOP.z * Mathf.sin(X)
            zz = bTOP.x * Mathf.sin(X) + bTOP.z * Mathf.cos(X)
            tmp = Vector3(xx, bTOP.y, zz)
            bTOP = tmp
            xx = frontV.x * Mathf.cos(X) - frontV.z * Mathf.sin(X)
            zz = frontV.x * Mathf.sin(X) + frontV.z * Mathf.cos(X)
            tmp = Vector3(xx, frontV.y, zz)
            frontV = tmp
            xx = rightV.x * Mathf.cos(X) - rightV.z * Mathf.sin(X)
            zz = rightV.x * Mathf.sin(X) + rightV.z * Mathf.cos(X)
            tmp = Vector3(xx, rightV.y, zz)
            rightV = tmp
            xx = topV.x * Mathf.cos(X) - topV.z * Mathf.sin(X)
            zz = topV.x * Mathf.sin(X) + topV.z * Mathf.cos(X)
            tmp = Vector3(xx, topV.y, zz)
            topV = tmp
        }
    }

    fun MissileVectorAdjust() {
        frontV = VectorFactory.cross(rightV, topV)
        rightV = VectorFactory.cross(topV, frontV)
        bTOP = VectorFactory.cross(frontV, Vector3(rightV.x, 0f, rightV.z))
        topV.nor()
        frontV.nor()
        rightV.nor()
        bTOP.nor()
    }

    private fun CalculateEulerAngles(): Vector3 {
        val Front = VectorFactory.worldFront()
        val Right = VectorFactory.worldRight()
        val Top = VectorFactory.worldTop()
        val originMatrix = Matrix3(
            floatArrayOf(
                Right.x, Right.y, Right.z,
                Top.x, Top.y, Top.z,
                Front.x, Front.y, Front.z
            )
        )
        val rotatedMatrix = Matrix3(
            floatArrayOf(
                rightV.x, rightV.y, rightV.z,
                topV.x, topV.y, topV.z,
                frontV.x, frontV.y, frontV.z
            )
        )
        val rotation = rotatedMatrix.mul(originMatrix.inv())
        val quaternion = Quaternion()
        quaternion.setFromMatrix(rotation)
        return Vector3(quaternion.yaw, quaternion.pitch, quaternion.roll)
    }

    private fun MissileFinalUpdate() {
        val curPosition = _agentInstance.getState<Any>(AgentStatics.GenericAgentStateName.CurrentPosition) as Vector3
        _des = VectorFactory.add(curPosition, VectorFactory.mul(frontV, speed * deltaTime))
        val newDirection = CalculateEulerAngles()
        _agentInstance.setState(AgentStatics.GenericAgentStateName.CurrentPosition, _des)
        _agentInstance.setState(AgentStatics.GenericAgentStateName.Front, VectorFactory.add(_des, frontV))
        _agentInstance.setState(AgentStatics.GenericAgentStateName.Right, VectorFactory.add(_des, rightV))
        _agentInstance.setState(AgentStatics.GenericAgentStateName.Top, VectorFactory.add(_des, topV))
        _agentInstance.setState(AgentStatics.GenericAgentStateName.CurrentDirection, newDirection)
    }

    private fun Init() {
        InitialVectors()
    }

    fun DoControl() {
        if (!crashed) {
            deltaTime = Global.getDeltaTime()
            MissileBodyControl()
            //MissileRotates();
            MissileTurns()
        }
        MissileVectorAdjust()
        MissileFinalUpdate()
        keys.clear()
    }
}