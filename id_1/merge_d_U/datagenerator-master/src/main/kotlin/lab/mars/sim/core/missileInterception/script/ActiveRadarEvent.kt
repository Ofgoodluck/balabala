package lab.mars.sim.core.missileInterception.script

class ActiveRadarEvent(val time: TimeRange, val frequency: Frequency, val affectedPlatform: Int) {
    override fun toString(): String {
        return "ActiveRadarEvent(time=$time, frequency=$frequency, affectedPlatform=$affectedPlatform)"
    }
}
