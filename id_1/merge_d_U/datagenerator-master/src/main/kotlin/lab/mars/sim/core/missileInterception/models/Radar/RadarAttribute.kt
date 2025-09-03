package lab.mars.sim.core.missileInterception.models.Radar

/**
 * Created by imrwz on 5/5/2017.
 */
object RadarAttribute {
    fun RefreshRadarSpin(radar: Radar) {
        val spinSpeed = radar.GetScanAngularVelocity()
        val spinRange = radar.GetMaxScanAngle()
        val isCounterClockwise = radar.GetCurrentScanIsCounterClockwise()
        val timeStep = ElectricityEnvironment.GetTimeStep()
        val actualSpinSpeed = spinSpeed * timeStep
        if (isCounterClockwise) {
            radar.SetCurrentScanAngle(radar.GetCurrentScanAngle() + actualSpinSpeed)
            if (radar.GetCurrentScanAngle() > spinRange) {
                radar.SetCurrentScanIsCounterClockwise(false)
            }
        } else {
            radar.SetCurrentScanAngle(radar.GetCurrentScanAngle() - actualSpinSpeed)
            if (radar.GetCurrentScanAngle() < 0) {
                radar.SetCurrentScanIsCounterClockwise(true)
            }
        }
    }

    fun CalculatePositiveRadarGt(
        radar: Radar,
        theta: Float
    ): Float {
        val theta0_5 = radar.GetTheta0_5()
        val part =
            (-ElectricityEnvironment.K * Math.pow((2 * theta / theta0_5).toDouble(), 2.0)).toFloat()
        return Math.exp(part.toDouble()).toFloat()
    }
}