package lab.mars.sim.core.missileInterception.models.Radar

import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.agentSimArch.game.IVCSCommittee
import lab.mars.windr.agentSimArch.utility.Mathf
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.util.*

/**
 * Created by imrwz on 5/5/2017.
 */
class RadarSimulationController private constructor() : IVCSCommittee {

    private val _detectiveRadarEffectiveAreas: MutableMap<Radar, PositiveRadarEffective3DArea> = HashMap()
    private val _jamRadarEffectiveAreas: MutableMap<Radar, PositiveRadarEffective3DArea> = HashMap()
    fun GetRadar3DArea(radar: Radar): PositiveRadarEffective3DArea? {
        return GetAllPositiveRadars()[radar]
    }

    fun GetAllPositiveRadars(): Map<Radar, PositiveRadarEffective3DArea> {
        val allRadars: MutableMap<Radar, PositiveRadarEffective3DArea> = HashMap()
        for ((key, value) in _detectiveRadarEffectiveAreas) {
            allRadars[key] = value
        }
        for ((key, value) in _jamRadarEffectiveAreas) {
            allRadars[key] = value
        }
        return allRadars
    }

    private fun ConvertTo3DArea(_2dBorders: List<Vector3?>): List<List<Vector3>> {
        val borderPoints3D: MutableList<List<Vector3>> = ArrayList()
        var angle = 0f
        while (angle <= 180) {
            val singleBorderPoints: MutableList<Vector3> = ArrayList()
            for (i in _2dBorders.indices) {
                val originPre = _2dBorders[i]
                var prePoint = Vector3(originPre!!.x, originPre.y, originPre.z)
                prePoint = VectorFactory.rotateAroundTop(prePoint, angle)
                prePoint = VectorFactory.rotateAroundFront(prePoint, 90f)
                prePoint = VectorFactory.rotateAroundTop(prePoint, 90f)
                //                Quaternion quaternion = new Quaternion();
//                quaternion.setEulerAngles(currentDirection.x, currentDirection.y, currentDirection.z);
//                prePoint = prePoint.mul(quaternion);
//                prePoint = VectorFactory.add(prePoint, currentPosition);
                singleBorderPoints.add(prePoint)
            }
            borderPoints3D.add(singleBorderPoints)
            angle += 10f
        }
        return borderPoints3D
    }

    fun AddNewRadar(radar: Radar, isJamRadar: Boolean) {
        if (isJamRadar) {
            _jamRadarEffectiveAreas[radar] = PositiveRadarEffective3DArea()
        } else {
            _detectiveRadarEffectiveAreas[radar] = PositiveRadarEffective3DArea()
        }
    }

    private fun FindNearestPoints(
        radar: Radar,
        targetPosition: Vector3
    ): Array<Vector3> //(x0,y0) is the radar position, (x1,y1) is the farthest point of the radar effective area, (a, b) is the target position
    {
        var minimumDistance = Float.MAX_VALUE
        var minimumBorderIndex = 0
        var minimumPointIndex = 0
        val _3DArea = GetAllPositiveRadars()[radar]
        val allBorders = _3DArea!!.GetAllRoundPoints()
        for (borderIndex in allBorders.indices) {
            val borderPoints = allBorders[borderIndex]
            for (pointIndex in borderPoints.indices) {
                val point = borderPoints[pointIndex]
                val distance = VectorFactory.distance(targetPosition, point)
                if (distance < minimumDistance) {
                    minimumDistance = distance
                    minimumBorderIndex = borderIndex
                    minimumPointIndex = pointIndex
                }
            }
        }
        val minimumBorder = allBorders[minimumBorderIndex]
        val size = minimumBorder.size
        var another = 0
        another = if (minimumPointIndex < size / 2) {
            minimumPointIndex + (size / 2 - minimumPointIndex)
        } else {
            size - minimumPointIndex
        }
        val a = minimumBorder[minimumPointIndex]
        val b = minimumBorder[another]
        return arrayOf(a, b)
    }

    private fun CalculateSpin(original: Vector3, spinAngle: Float, related: Vector3): Vector3 {
        val q = Quaternion()
        q.x = 0f
        q.y = 0f
        q.z = Mathf.sin(spinAngle / 2.0f * (Mathf.PI / 180f))
        q.w = Mathf.cos(spinAngle / 2.0f * (Mathf.PI / 180f))
        return q.transform(original).add(related)
    }

    private fun JudgeVectorSyntropy(a: Vector3, b: Vector3): Boolean {
        val angle = VectorFactory.angle(a, b)

        //Debug.Log("angle: " + angle);
        return angle < 90
    }

    fun InRadarEffectiveArea(radar: Radar, target: Agent): Boolean {
        val targetPosition = target.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
        val nearestBorderPoints = FindNearestPoints(radar, targetPosition)
        val _3DArea = GetAllPositiveRadars()[radar]
        val allPoints = _3DArea!!.GetAllRoundPoints()
        val oneBorder = allPoints[0]
        val oneBorderSize = oneBorder.size
        val farthestPoint = oneBorder[oneBorderSize / 2]
        val nearestPoint = oneBorder[0]
        val isBorderVectorSyntropy = JudgeVectorSyntropy(
            VectorFactory.subtract(
                nearestBorderPoints[0], targetPosition
            ), VectorFactory.subtract(
                nearestBorderPoints[1], targetPosition
            )
        )
        val isFarthestVectorSyntropy = JudgeVectorSyntropy(
            VectorFactory.subtract(farthestPoint, targetPosition),
            VectorFactory.subtract(nearestPoint, targetPosition)
        )
        return !isBorderVectorSyntropy && !isFarthestVectorSyntropy
    }

    fun CalculateProjectedPlaneAreaCenter(planeToProject: Vector3, targets: Array<Vector3?>): Vector3 {
        var summary = Vector3()
        for (target in targets) {
            val projectedPosition = VectorFactory.projectOnPlane(target, planeToProject.nor())
            summary = VectorFactory.add(summary, projectedPosition)
        }
        return summary.scl(targets.size.toFloat())
    }

    private fun CalculateDetectiveRadarEffectiveAreaOnJamed(
        detectiveRadar: Radar,
        jamRadar: Array<Radar>,
        targetRCS: Float
    ): List<List<Vector3>>? {
        val agent = detectiveRadar.GetBindedAgent()
        for (jam in jamRadar) {
            if (jam.IsOn()) {
                CalculatePositiveRadarEffectiveAreaWithoutJam(jam, targetRCS)
                if (InRadarEffectiveArea(jam, agent)) {
                    val detectiveFront =
                        detectiveRadar.GetBindedAgent().getState<Vector3>(AgentStatics.GenericAgentStateName.Front)
                    val detectivePosition = detectiveRadar.GetBindedAgent()
                        .getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
                    val jamPosition =
                        jam.GetBindedAgent().getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
                    val angle =
                        VectorFactory.angle(detectiveFront, VectorFactory.subtract(jamPosition, detectivePosition))
                    println(jam.GetBindedAgent().id.toString() + angle.toString())
                }
            }
        }
        return null
    }

    private fun CalculatePositiveRadarEffectiveAreaWithoutJam(radar: Radar, targetRCS: Float): List<List<Vector3>> {
        val Pt = radar.GetPt().toFloat()
        val lambda = radar.GetLambda().toFloat()
        val _radarBorderArray: MutableList<Vector3?> = ArrayList()
        val rightPart: MutableList<Vector3?> = ArrayList()
        val leftPart: MutableList<Vector3?> = ArrayList()
        var theta = 0.0f
        while (theta < 90) {
            val part = Pt * RadarAttribute.CalculatePositiveRadarGt(radar, theta) *
                    Mathf.pow(lambda, 2f) * targetRCS / (Mathf.pow(4 * Mathf.PI, 3f) *
                    ElectricityEnvironment.K * ElectricityEnvironment.L)
            val Rmax = Mathf.pow(part, 0.25f)
            val currentScanAngle = radar.GetCurrentScanAngle()
            val right = MathUtility.CalculateCoordinates(Rmax, theta)
            val left = Vector3(-right.x, right.y, right.z)
            rightPart.add(right)
            leftPart.add(left)
            theta += ElectricityEnvironment.deltaTheta
        }
        Collections.reverse(leftPart)
        _radarBorderArray.addAll(rightPart)
        _radarBorderArray.addAll(leftPart)
        RadarAttribute.RefreshRadarSpin(radar)
        return ConvertTo3DArea(_radarBorderArray)
    }

    private fun RefreshRadarSpin(radar: Radar) {
        val spinSpeed = radar.GetScanAngularVelocity()
        val spinRange = radar.GetMaxScanAngle()
        val isCounterClockwise = radar.GetCurrentScanIsCounterClockwise()
        val timeStep = Global.getDeltaTime()
        val actualSpinSpeed = spinSpeed * timeStep
        if (isCounterClockwise) {
            val currentAngle = radar.GetCurrentScanAngle()
            radar.SetCurrentScanAngle(currentAngle + actualSpinSpeed)
            if (radar.GetCurrentScanAngle() > spinRange) {
                radar.SetCurrentScanIsCounterClockwise(false)
            }
        } else {
            val currentAngle = radar.GetCurrentScanAngle()
            radar.SetCurrentScanAngle(currentAngle - actualSpinSpeed)
            if (radar.GetCurrentScanAngle() < 0) {
                radar.SetCurrentScanIsCounterClockwise(true)
            }
        }
    }

    private fun UpdateAllRadars() {
        val allActiveRadars = GetAllPositiveRadars()
        runBlocking {
            for (radar in allActiveRadars.keys) {
                if (!radar.IsOn()) {
                    continue
                }
                if (!_detectiveRadarEffectiveAreas.containsKey(radar)) {
                    _detectiveRadarEffectiveAreas[radar] = PositiveRadarEffective3DArea()
                }
                val area = _detectiveRadarEffectiveAreas[radar]!!
                if (radar.updated) {
                    launch {
                        area.SetAllRoundPoints(CalculatePositiveRadarEffectiveAreaWithoutJam(radar, 2f))
                        radar.updated = false
                    }
                }
            }
        }
    }

    companion object {
        private val _instance = RadarSimulationController()
        fun GetInstance(): RadarSimulationController {
            return _instance
        }
    }

    override fun init() {
    }

    override fun record(allStateMapContainer: MutableMap<String, Any>?) {
        UpdateAllRadars()
    }

    override fun commit() {
    }
}