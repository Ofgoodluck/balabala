package lab.mars.sim.core.missileInterception.controller

object MissileControllerStatics {
    const val PitchUpKey = "PitchUp"
    const val PitchDownKey = "PitchDown"
    const val YawLeftKey = "YawLeft"
    const val YawRightKey = "YawRight"
    const val MaxApproximatelyEqualAngleDiff = 0.005f
    var MaxPitchAngle = 90f
    var MaxPitchAngularVelocityPerSecond = 0.1f
    var MaxTurnAngularVelocityPerSecond = 0.1f
}