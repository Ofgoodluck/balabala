package lab.mars.sim.core.missileInterception.models.Radar

import com.badlogic.gdx.math.Vector3
import lab.mars.windr.agentSimArch.utility.VectorFactory

/**
 * Created by imrwz on 7/5/2017.
 */
class PositiveRadarEffective3DArea {
    private var _allAroundPoints: List<List<Vector3>> = ArrayList()
    private var _largest3DAreaPlanePoints = arrayOfNulls<Vector3>(3)
    private var _largest3DAreaPlaneVector = Vector3.Zero
    private fun SetLargest3DAreaPlanePoints(vararg vector: Vector3?) {
        assert(vector.size == 3)
        vector.forEachIndexed { idx, it ->
            _largest3DAreaPlanePoints[idx] = it
        }
        val a = VectorFactory.subtract(vector[0], vector[1])
        val b = VectorFactory.subtract(vector[1], vector[2])
        _largest3DAreaPlaneVector = VectorFactory.cross(a, b)
    }

    fun GetLargest3DAreaPlanePoints(): Array<Vector3?> {
        return _largest3DAreaPlanePoints
    }

    fun GetAllRoundPoints(): List<List<Vector3>> {
        return _allAroundPoints
    }

    fun GetLargest3DAreaPlaneVector(): Vector3 {
        return _largest3DAreaPlaneVector
    }

    fun SetAllRoundPoints(points: List<List<Vector3>>) {
        _allAroundPoints = points
        val singleBorder = points[0]
        val length = singleBorder.size
        var max = 0
        var maxDistance = 0f
        for (i in 0 until length / 2) {
            val thisDistance = VectorFactory.distance(singleBorder[i], singleBorder[length - i - 1])
            if (thisDistance >= maxDistance) {
                max = i
                maxDistance = thisDistance
            }
        }
        val first = singleBorder[max]
        val center = VectorFactory.middle(singleBorder[max], singleBorder[length - max])
        val second = points[1][max]
        SetLargest3DAreaPlanePoints(first, center, second)
    }
}