package lab.mars.sim.core.missileInterception.script
enum class CoordinationType {
    FormationKeep, //队形保持协同编队
    ESMPositioning, //无源辐射源融合编队
    ActiveRadarPositioning, //目标探测融合编队
    Guidance, //导引编队
    Dismiss, //解除某些协同关系
}

class CoordinationEvent(
    val idx : Int,
    val time: TimeRange,
    val type: CoordinationType,
    val lead: Int,
    val followers: Array<Int>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoordinationEvent

        if (idx != other.idx) return false
        if (time != other.time) return false
        if (type != other.type) return false
        if (lead != other.lead) return false
        if (!followers.contentEquals(other.followers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idx
        result = 31 * result + time.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + lead
        result = 31 * result + followers.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "CoordinationEvent(idx=$idx, time=$time, type=$type, formationLead=$lead, followers=${followers.contentToString()})"
    }
}
