package lab.mars.sim.core.missileInterception.script
enum class EnvironmentEType {
    PositionDrift, //位置发生偏移
    PositionReturn, //位置回归D道
    ESMHasTarget, //被动雷达有有效探测信息
    ActiveHasTarget, //主动雷达有有效探测信息
    PlatformDisabled, //平台被打掉
    RandomDisabled, //目前没用
    HitTarget, //成功打击
}

class EnvironmentEvent(val time: TimeRange, val type : EnvironmentEType, val affectedPlatform : Int) {
    override fun toString(): String {
        return "EnvironmentEvent(time=$time, type=$type, affectedPlatform=$affectedPlatform)"
    }
}