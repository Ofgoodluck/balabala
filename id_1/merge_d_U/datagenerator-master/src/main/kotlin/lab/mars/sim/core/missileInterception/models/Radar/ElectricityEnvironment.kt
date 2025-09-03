package lab.mars.sim.core.missileInterception.models.Radar

import com.badlogic.gdx.math.Vector3

/**
 * Created by imrwz on 2017/5/4.
 */
object ElectricityEnvironment {
    const val K = 1f
    const val L = 3f
    const val deltaTheta = 1f
    const val Kj = 2f
    val radarOriginalOrientation = Vector3(0f, 0f, 1f)
    var largestJamAngle = 20f
    var Kr = 0.07f
    fun GetTimeStep(): Float {
        return 0.0167f
    }
}