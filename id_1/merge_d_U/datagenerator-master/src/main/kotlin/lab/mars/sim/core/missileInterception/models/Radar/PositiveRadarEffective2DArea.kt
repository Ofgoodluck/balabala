package lab.mars.sim.core.missileInterception.models.Radar

import com.badlogic.gdx.math.Vector3
import lab.mars.windr.agentSimArch.utility.VectorFactory

/**
 * Created by imrwz on 2017/5/4.
 */
class PositiveRadarEffective2DArea {
    private val _leftPartPoints: MutableList<Vector3> = ArrayList()
    private val _rightPartPoints: MutableList<Vector3> = ArrayList()
    private var _farthestPointIndex = 0
    private val _largestPartPoints = arrayOfNulls<Vector3>(3) //right, left, middle
    fun GetRadarPoints(): List<Vector3> {
        val radarPoints: MutableList<Vector3> = ArrayList()
        radarPoints.addAll(_rightPartPoints)
        radarPoints.addAll(_leftPartPoints)
        return radarPoints
    }

    fun GetLeftPartPoints(): List<Vector3> {
        return _leftPartPoints
    }

    fun GetRightPartPoints(): List<Vector3> {
        return _rightPartPoints
    }

    fun GetLargestPartPoints(): Array<Vector3?> {
        return _largestPartPoints
    }

    fun SetRadarPoints(value: List<Vector3>) {
        val count = value.size / 2
        _leftPartPoints.clear()
        _rightPartPoints.clear()
        var largestDistance = 0f
        for (i in 0 until count) {
            val right = value[i]
            val left = value[i + count]
            val distance = VectorFactory.distance(right, left)
            if (distance > largestDistance) {
                largestDistance = distance
                _largestPartPoints[0] = right
                _largestPartPoints[1] = left
                _largestPartPoints[2] = VectorFactory.middle(left, right)
            }
            _rightPartPoints.add(value[i])
            _leftPartPoints.add(value[i + count])
        }
    }

    fun GetFarthestPointIndex(): Int {
        return _farthestPointIndex
    }

    fun SetFarthestPointIndex(value: Int) {
        _farthestPointIndex = value
    }
}