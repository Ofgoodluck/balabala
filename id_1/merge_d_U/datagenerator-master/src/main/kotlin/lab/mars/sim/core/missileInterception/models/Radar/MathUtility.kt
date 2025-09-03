package lab.mars.sim.core.missileInterception.models.Radar

import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3

/**
 * Created by imrwz on 5/5/2017.
 */
object MathUtility {
    fun CalculateCoordinates(longEdge: Float, theta: Float): Vector3 {
        val x = (longEdge * Math.sin(theta * Math.PI / 180)).toFloat()
        val y = (longEdge * Math.cos(theta * Math.PI / 180)).toFloat()
        return Vector3(x, y, 0f)
    }

    @JvmOverloads
    fun CalculateSpin(original: Vector3?, spinAngle: Float, related: Vector3? = Vector3.Zero): Vector3 {
        val q = Quaternion()
        q.x = 0f
        q.y = 0f
        q.z = Math.sin(spinAngle / 2.0f * (Math.PI / 180f)).toFloat()
        q.w = Math.cos(spinAngle / 2.0f * (Math.PI / 180f)).toFloat()
        return q.transform(original).add(related)
    }
}