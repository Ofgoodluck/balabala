package lab.mars.sim.core.missileInterception.script

import lab.mars.windr.agentSimArch.game.Global


class TimeRange(val from: Int, val until: Int = from) //左闭右开
{
    fun withinRange(t: Int): Boolean {
        return (from == until && from == t) || (t in from until until)
    }

    fun length() : Int {
        return until - from
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeRange

        if (from != other.from) return false
        if (until != other.until) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from
        result = 31 * result + until
        return result
    }

    override fun toString(): String {
        return "TimeRange(from=$from, until=$until)"
    }


}

fun secondNow(): Int {
    return (Global.now() * Global.getDeltaTime()).toInt()
}

class SimTime(
    private var realValueMS: Int = 0,
    private val step: Int = 1000,
    private val endTimeMS: Int = 220000
) {

    fun getSecond(): Int {
        return (realValueMS / 1000)
    }

    fun getMillionSecond(): Int {
        return realValueMS
    }

    fun getStepMS(): Int {
        return step
    }

    fun getEndTimeSecond() : Int {
        return endTimeMS / 1000
    }
}