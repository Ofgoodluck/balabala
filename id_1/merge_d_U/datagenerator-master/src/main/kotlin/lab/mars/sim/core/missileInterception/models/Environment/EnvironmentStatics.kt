package lab.mars.sim.core.missileInterception.models.Environment

import com.badlogic.gdx.math.Vector3

/**
 * Created by imrwz on 6/16/2017.
 */
object EnvironmentStatics {
    const val UAVStanbyRadius = 160f
    const val MissileObserveRadius = 3200f
    const val RadarSwitchOnRadius = 1176f
    const val BreakingRadius = 352f
    const val OccurringLandScapeAngle = 120f
    const val OccurringPortraitAngle = 30f
    const val GainInDistance = 3f
    val radarOriginalOrientation = Vector3(0f, 1f, 0f)
    const val MissileAgentIdPrefix = "missile agent "
    const val UAVAgentIdPrefix = "uav agent "
    const val ShipAgentIdPrefix = "ship agent "
    var MissileSelectableFrequencySet: MutableList<Int> = ArrayList()
    var MissileSelectableFrequencyCount = 3

    init {
        MissileSelectableFrequencySet.add(10)
        MissileSelectableFrequencySet.add(20)
        MissileSelectableFrequencySet.add(30)
        assert(MissileSelectableFrequencySet.size == MissileSelectableFrequencyCount)
    }
}