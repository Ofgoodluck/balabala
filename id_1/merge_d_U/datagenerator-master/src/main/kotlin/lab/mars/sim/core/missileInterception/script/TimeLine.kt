package lab.mars.sim.core.missileInterception.script

class TimeLine(
    xxIds: HashSet<Int>,
    xxRoleScripts: HashMap<Int, ArrayList<RoleScript>>,
    environmentEvents: ArrayList<EnvironmentEvent>,
    coordinationEvent: ArrayList<CoordinationEvent>,
    val cyberSpaceEvent: ArrayList<CyberspaceEvent>
) {

    val xxEventList = ArrayList<HashMap<Int, ArrayList<Any>>>()
    var endTimeSecond: Int = 0
    var xxActiveRadarEvents = ArrayList<ActiveRadarEvent>()

    init {
        coordinationEvent.forEach {
            if (it.time.until > endTimeSecond) {
                endTimeSecond = it.time.until
            }
        }
        environmentEvents.forEach {
            if (it.time.until > endTimeSecond) {
                endTimeSecond = it.time.until
            }
        }

        (0 until endTimeSecond).forEach { currentSecond ->
            val allXXCurrentEvents = hashMapOf<Int, ArrayList<Any>>()
            xxIds.forEach { id ->
                val xxEvents = arrayListOf<Any>()
                xxRoleScripts[id]!!.forEach {
                    if (it.time.withinRange(currentSecond)) {
                        xxEvents.add(it)
                    }
                }
                environmentEvents.forEach {
                    if (it.time.withinRange(currentSecond) && it.affectedPlatform == id) {
                        xxEvents.add(it)
                    }
                }
                coordinationEvent.forEach {
                    if (it.time.withinRange(currentSecond) && ((it.lead == id) || (it.followers.contains(id)))) {
                        xxEvents.add(it)
                    }
                }
                allXXCurrentEvents[id] = xxEvents
            }
            xxEventList.add(allXXCurrentEvents)
        }
    }

    fun getCurrentXXRoles(id: Int): HashSet<XXRole> {
        val currentSecond = secondNow()
        val ret = hashSetOf<XXRole>()
        xxEventList[currentSecond][id]!!.forEach {
            if (it is RoleScript) {
                ret.add(it.role)
            }
        }
        return ret
    }

    fun getCurrentEnvironmentEvents(id: Int): EnvironmentEvent? {
        val currentSecond = secondNow()
        xxEventList[currentSecond][id]!!.forEach {
            if (it is EnvironmentEvent) {
                return it
            }
        }
        return null
    }

    fun getCurrentEnvironmentEvents(): ArrayList<EnvironmentEvent> {
        val currentSecond = secondNow()
        val ret = ArrayList<EnvironmentEvent>()
        if (xxEventList.size <= currentSecond) {
            return ret
        }
        xxEventList[currentSecond].forEach {entry ->
            entry.value.forEach {
                if (it is EnvironmentEvent) {
                    ret.add(it)
                }
            }
        }
        return ret
    }

    fun getCurrentCoordinationEvents(id: Int): ArrayList<CoordinationEvent> {
        val currentSecond = secondNow()
        val ret = ArrayList<CoordinationEvent>()
        xxEventList[currentSecond][id]!!.forEach {
            if (it is CoordinationEvent) {
                ret.add(it)
            }
        }
        return ret
    }

    fun getCurrentCyberSpaceEvent(): CyberspaceEvent? {
        val currentSecond = secondNow()
        cyberSpaceEvent.forEach {
            if (it.time.withinRange(currentSecond)) {
                return it
            }
        }
        return null
    }

    fun getCurrentCoordinationEvents(): HashSet<CoordinationEvent> {
        val ret = HashSet<CoordinationEvent>()
        val currentSecond = secondNow()
        if (xxEventList.size <= (currentSecond)) {
            return ret
        }
        xxEventList[currentSecond].forEach { entry ->
            entry.value.forEach {
                if (it is CoordinationEvent) {
                    ret.add(it)
                }
            }
        }
        return ret
    }

    fun getCurrentActiveRadarEvents() : HashMap<Int, ActiveRadarEvent> {
        val ret = hashMapOf<Int, ActiveRadarEvent>()
        val now = secondNow()
        xxActiveRadarEvents.filter { it.time.withinRange(now) }.map { ret[it.affectedPlatform] = it }
        return ret
    }

    fun ended(): Boolean {
        return secondNow() == this.endTimeSecond
    }
}

fun initializeTimeLine(isLoadScript: Boolean = false) : TimeLine {
    return if (isLoadScript) {
        initializeTimeLineFromScript()
    } else {
        initializeTimeLineFromDefault()
    }
}

fun initializeTimeLineFromScript() : TimeLine {
    val xxRoleScripts = hashMapOf(
        Pair(
            1, arrayListOf(
                RoleScript(TimeRange(0, 120), XXRole.ClusterLead),
                RoleScript(TimeRange(0, 100), XXRole.FormationLead),
                RoleScript(TimeRange(0, 100), XXRole.ESMLead),
                RoleScript(TimeRange(100, 180), XXRole.ESMFollower),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            2, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 100), XXRole.ESMFollower),
                RoleScript(TimeRange(100, 141), XXRole.ActiveLead),
                RoleScript(TimeRange(141), XXRole.Dead)
            )
        ),
        Pair(
            3, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationLead),
                RoleScript(TimeRange(0, 100), XXRole.ESMFollower),
                RoleScript(TimeRange(100, 180), XXRole.ActiveLead),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            4, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationLead),
                RoleScript(TimeRange(0, 100), XXRole.ESMFollower),
                RoleScript(TimeRange(100, 156), XXRole.ActiveLead),
                RoleScript(TimeRange(156, 200), XXRole.GuidanceLead),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            5, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 141), XXRole.ESMFollower),
                RoleScript(TimeRange(141, 180), XXRole.ActiveLead),
                RoleScript(TimeRange(156, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            6, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 156), XXRole.ESMFollower),
                RoleScript(TimeRange(156, 180), XXRole.GuidanceLead),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            7, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 156), XXRole.ESMFollower),
                RoleScript(TimeRange(156, 180), XXRole.GuidanceLead),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            8, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 180), XXRole.ESMFollower),
                RoleScript(TimeRange(120, 200), XXRole.ClusterLead),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            9, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 180), XXRole.ESMFollower),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            10, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 180), XXRole.ESMFollower),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        )
    )
    //环境事件
    val xxEnvironmentEvents = ScriptTool.handleXXEnvironment(ScriptTool.filePathMap["xxEnvironmentEvent"]!!)
    //网络空间事件
    val xxCyberspaceEvents = ScriptTool.handleCyberspace(ScriptTool.filePathMap["xxCyberspaceEvents"]!!)
    //协同事件
    val xxCoordinationEvents = ScriptTool.handleXXCoordination(ScriptTool.filePathMap["xxCoordinationEvent"]!!)
    //xx平台雷达主动变更时间线
    val xxActiveRadarEvent = ScriptTool.handleXXActiveRadarFrequency(ScriptTool.filePathMap["xxActiveRadarFrequency"]!!)

    val timeLine = TimeLine(
        hashSetOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        xxRoleScripts,
        xxEnvironmentEvents,
        xxCoordinationEvents,
        xxCyberspaceEvents
    )
    timeLine.xxActiveRadarEvents = xxActiveRadarEvent
    return timeLine
}

fun initializeTimeLineFromDefault(): TimeLine {
    val xxRoleScripts = hashMapOf(
        Pair(
            1, arrayListOf(
                RoleScript(TimeRange(0, 120), XXRole.ClusterLead),
                RoleScript(TimeRange(0, 100), XXRole.FormationLead),
                RoleScript(TimeRange(0, 100), XXRole.ESMLead),
                RoleScript(TimeRange(100, 180), XXRole.ESMFollower),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            2, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 100), XXRole.ESMFollower),
                RoleScript(TimeRange(100, 141), XXRole.ActiveLead),
                RoleScript(TimeRange(141), XXRole.Dead)
            )
        ),
        Pair(
            3, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationLead),
                RoleScript(TimeRange(0, 100), XXRole.ESMFollower),
                RoleScript(TimeRange(100, 180), XXRole.ActiveLead),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            4, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationLead),
                RoleScript(TimeRange(0, 100), XXRole.ESMFollower),
                RoleScript(TimeRange(100, 156), XXRole.ActiveLead),
                RoleScript(TimeRange(156, 200), XXRole.GuidanceLead),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            5, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 141), XXRole.ESMFollower),
                RoleScript(TimeRange(141, 180), XXRole.ActiveLead),
                RoleScript(TimeRange(156, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            6, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 156), XXRole.ESMFollower),
                RoleScript(TimeRange(156, 180), XXRole.GuidanceLead),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            7, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 156), XXRole.ESMFollower),
                RoleScript(TimeRange(156, 180), XXRole.GuidanceLead),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            8, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 180), XXRole.ESMFollower),
                RoleScript(TimeRange(120, 200), XXRole.ClusterLead),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            9, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 180), XXRole.ESMFollower),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        ),
        Pair(
            10, arrayListOf(
                RoleScript(TimeRange(0, 100), XXRole.FormationFollower),
                RoleScript(TimeRange(0, 180), XXRole.ESMFollower),
                RoleScript(TimeRange(180, 200), XXRole.GuidanceFollower),
                RoleScript(TimeRange(200), XXRole.Dead)
            )
        )
    )
    //环境事件
    val xxEnvironmentEvents = arrayListOf(
        EnvironmentEvent(TimeRange(10, 20), EnvironmentEType.PositionDrift, 1),
        EnvironmentEvent(TimeRange(20), EnvironmentEType.PositionReturn, 1),
        EnvironmentEvent(TimeRange(30, 40), EnvironmentEType.PositionDrift, 2),
        EnvironmentEvent(TimeRange(40), EnvironmentEType.PositionReturn, 2),
        EnvironmentEvent(TimeRange(40, 50), EnvironmentEType.PositionDrift, 6),
        EnvironmentEvent(TimeRange(50), EnvironmentEType.PositionReturn, 6),
        EnvironmentEvent(TimeRange(45, 55), EnvironmentEType.PositionDrift, 4),
        EnvironmentEvent(TimeRange(55), EnvironmentEType.PositionReturn, 4),
        EnvironmentEvent(TimeRange(50, 60), EnvironmentEType.PositionDrift, 5),
        EnvironmentEvent(TimeRange(60), EnvironmentEType.PositionReturn, 5),
        EnvironmentEvent(TimeRange(57, 67), EnvironmentEType.PositionDrift, 7),
        EnvironmentEvent(TimeRange(67), EnvironmentEType.PositionReturn, 7),
        EnvironmentEvent(TimeRange(57, 67), EnvironmentEType.PositionDrift, 10),
        EnvironmentEvent(TimeRange(67), EnvironmentEType.PositionReturn, 10),
        EnvironmentEvent(TimeRange(150, 155), EnvironmentEType.ESMHasTarget, 6),
        EnvironmentEvent(TimeRange(145, 165), EnvironmentEType.ESMHasTarget, 7),
        EnvironmentEvent(TimeRange(140, 150), EnvironmentEType.ESMHasTarget, 10),
        EnvironmentEvent(TimeRange(150, 155), EnvironmentEType.ActiveHasTarget, 4),
        EnvironmentEvent(TimeRange(141), EnvironmentEType.PlatformDisabled, 2),
        EnvironmentEvent(TimeRange(195), EnvironmentEType.PlatformDisabled, 6),
        EnvironmentEvent(TimeRange(212), EnvironmentEType.PlatformDisabled, 4),
        EnvironmentEvent(TimeRange(223), EnvironmentEType.PlatformDisabled, 9),
        EnvironmentEvent(TimeRange(233), EnvironmentEType.PlatformDisabled, 10),
        EnvironmentEvent(TimeRange(235), EnvironmentEType.PlatformDisabled, 1),
        EnvironmentEvent(TimeRange(238), EnvironmentEType.PlatformDisabled, 5),
        EnvironmentEvent(TimeRange(202), EnvironmentEType.HitTarget, 10),
        EnvironmentEvent(TimeRange(207), EnvironmentEType.HitTarget, 7),
        EnvironmentEvent(TimeRange(215), EnvironmentEType.HitTarget, 6),
        EnvironmentEvent(TimeRange(220), EnvironmentEType.HitTarget, 4),
        EnvironmentEvent(TimeRange(228), EnvironmentEType.HitTarget, 3),
        EnvironmentEvent(TimeRange(232), EnvironmentEType.HitTarget, 9),
        EnvironmentEvent(TimeRange(235), EnvironmentEType.HitTarget, 1),
        EnvironmentEvent(TimeRange(241), EnvironmentEType.HitTarget, 5),
        EnvironmentEvent(TimeRange(244), EnvironmentEType.HitTarget, 8)
    )
    //网络空间事件
    val xxCyberspaceEvents = arrayListOf(
        CyberspaceEvent(
            TimeRange(0), hashMapOf(
                Pair(1, hashSetOf(2, 3, 4, 5, 6, 7, 8, 9, 10)),
                Pair(2, hashSetOf(1)),
                Pair(3, hashSetOf(1)),
                Pair(4, hashSetOf(1)),
                Pair(5, hashSetOf(1)),
                Pair(6, hashSetOf(1)),
                Pair(7, hashSetOf(1)),
                Pair(8, hashSetOf(1)),
                Pair(9, hashSetOf(1)),
                Pair(10, hashSetOf(1)),
            )
        ),
        CyberspaceEvent(
            TimeRange(120), hashMapOf(
                Pair(1, hashSetOf(8)),
                Pair(2, hashSetOf(8)),
                Pair(3, hashSetOf(8)),
                Pair(4, hashSetOf(8)),
                Pair(5, hashSetOf(8)),
                Pair(6, hashSetOf(8)),
                Pair(7, hashSetOf(8)),
                Pair(8, hashSetOf(1, 2, 3, 4, 5, 6, 7, 9, 10)),
                Pair(9, hashSetOf(8)),
                Pair(10, hashSetOf(8)),
            )
        ),
        CyberspaceEvent(
            TimeRange(141), hashMapOf(
                Pair(1, hashSetOf(8)),
                Pair(3, hashSetOf(8)),
                Pair(4, hashSetOf(8)),
                Pair(5, hashSetOf(8)),
                Pair(6, hashSetOf(8)),
                Pair(7, hashSetOf(8)),
                Pair(8, hashSetOf(1, 3, 4, 5, 6, 7, 9, 10)),
                Pair(9, hashSetOf(8)),
                Pair(10, hashSetOf(8)),
            )
        )
    )
    //协同事件
    val xxCoordinationEvents = arrayListOf(
//        CoordinationEvent(5, TimeRange(0, 141), CoordinationType.ActiveRadarPositioning, 2, arrayOf(1, 8, 9)),
//        CoordinationEvent(6, TimeRange(0, 141), CoordinationType.ActiveRadarPositioning, 3, arrayOf(5, 10)),
//        CoordinationEvent(7, TimeRange(0, 156), CoordinationType.ActiveRadarPositioning, 4, arrayOf(6, 7)),
        CoordinationEvent(1, TimeRange(0, 100), CoordinationType.FormationKeep, 1, arrayOf(2, 5, 8)),
        CoordinationEvent(2, TimeRange(0, 100), CoordinationType.FormationKeep, 3, arrayOf(6, 9)),
        CoordinationEvent(3, TimeRange(0, 100), CoordinationType.FormationKeep, 4, arrayOf(7, 10)),
        CoordinationEvent(4, TimeRange(0, 100), CoordinationType.ESMPositioning, 1, arrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10)),
        CoordinationEvent(1, TimeRange(100), CoordinationType.Dismiss, 1, arrayOf(2, 5, 8)),
        CoordinationEvent(2, TimeRange(100), CoordinationType.Dismiss, 3, arrayOf(6, 9)),
        CoordinationEvent(3, TimeRange(100), CoordinationType.Dismiss, 4, arrayOf(7, 10)),
        CoordinationEvent(4, TimeRange(100), CoordinationType.Dismiss, 1, arrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10)),
        CoordinationEvent(5, TimeRange(100, 141), CoordinationType.ActiveRadarPositioning, 2, arrayOf(1, 8, 9)),
        CoordinationEvent(6, TimeRange(100, 141), CoordinationType.ActiveRadarPositioning, 3, arrayOf(5, 10)),
        CoordinationEvent(7, TimeRange(100, 156), CoordinationType.ActiveRadarPositioning, 4, arrayOf(6, 7)),
        //{1, 2, 3, 4, ARP}
        //replace 2 by 5,
        // {3, 5, 10} -> {3, 1, 10}
        //replace 5 by 1
        //2号挂了以后，编队分别变成了{5, 8, 9}和{3, 1, 10}
        //先解散2号XX原来所处的5号协同编队：
        CoordinationEvent(5, TimeRange(141), CoordinationType.Dismiss, 2, arrayOf(1, 8, 9)),
        //现在1，8，9是空闲的了，那么要把5号调出来带领他们，5号原来在6号{3, 5, 10}编队，所以就需要先解除3和5的协同关系
        CoordinationEvent(6, TimeRange(141), CoordinationType.Dismiss, 3, arrayOf(5)),
        //这时，编队情况变成了 5,1,8,9空闲，{3, 10}继续协同，接下来让{5, 8, 9}形成新的编队
        CoordinationEvent(5, TimeRange(141, 156), CoordinationType.ActiveRadarPositioning, 5, arrayOf(8, 9)),
        //，然后{3，1， 10}继续形成6号编队
        CoordinationEvent(6, TimeRange(141, 156), CoordinationType.ActiveRadarPositioning, 3, arrayOf(1, 10)),
        CoordinationEvent(7, TimeRange(156), CoordinationType.Dismiss, 4, arrayOf(6, 7)),
        CoordinationEvent(8, TimeRange(156, 250), CoordinationType.Guidance, 4, arrayOf()),
        CoordinationEvent(9, TimeRange(156, 250), CoordinationType.Guidance, 6, arrayOf()),
        CoordinationEvent(10, TimeRange(156, 250), CoordinationType.Guidance, 7, arrayOf()),
        CoordinationEvent(5, TimeRange(180), CoordinationType.Dismiss, 5, arrayOf(8, 9)),
        CoordinationEvent(6, TimeRange(180), CoordinationType.Dismiss, 3, arrayOf(1, 10)),
        CoordinationEvent(11, TimeRange(180, 250), CoordinationType.Guidance, 3, arrayOf(10, 8)),
        CoordinationEvent(12, TimeRange(180, 250), CoordinationType.Guidance, 5, arrayOf(1, 9))
    )

    val timeLine = TimeLine(
        hashSetOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        xxRoleScripts,
        xxEnvironmentEvents,
        xxCoordinationEvents,
        xxCyberspaceEvents
    )
    return timeLine
}