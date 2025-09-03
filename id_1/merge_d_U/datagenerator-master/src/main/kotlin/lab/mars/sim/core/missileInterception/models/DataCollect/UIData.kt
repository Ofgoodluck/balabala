package lab.mars.sim.core.missileInterception.models.DataCollect

import lab.mars.sim.core.missileInterception.script.CoordinationType

abstract class SNGenerator {
    open fun getLeadNotifierList(leadIdx: Int, followerIdx: Array<Int>): HashMap<Int, List<String>> {
        return hashMapOf()
    }

    open fun getLeadSubscriberList(leadIdx: Int, followerIdx: Array<Int>): HashMap<Int, List<String>> {
        return hashMapOf()
    }

    open fun getFollowerNotifierList(leadIdx: Int, followerIdx: Int): HashMap<Int, List<String>> {
        return hashMapOf()
    }

    open fun getFollowerSubscriberList(leadIdx: Int, followerIdx: Int): HashMap<Int, List<String>> {
        return hashMapOf()
    }

}

object FormationKeepSNGenerator : SNGenerator() {
    override fun getLeadNotifierList(leadIdx: Int, followerIdx: Array<Int>): HashMap<Int, List<String>> {
        val ret = hashMapOf<Int, List<String>>()
        val selfNotifier = arrayListOf<String>()
        selfNotifier.add("/root/平台飞控/位置")
        ret[leadIdx] = selfNotifier
        return ret
    }
}

object ActiveRadarPositioningSNGenerator : SNGenerator() {
    override fun getLeadNotifierList(leadIdx: Int, followerIdx: Array<Int>): HashMap<Int, List<String>> {
        val ret = hashMapOf<Int, List<String>>()
        val selfNotifier = arrayListOf<String>()
        selfNotifier.add("/root/时钟设备/当前时间")
        selfNotifier.add("/root/主动雷达/探测信息")
        ret[leadIdx] = selfNotifier
        followerIdx.forEach {
            val followerNotifier = arrayListOf<String>()
            followerNotifier.add("/root/global/$it/被动雷达/探测信息")
            ret[it] = followerNotifier
        }
        return ret
    }

    override fun getFollowerSubscriberList(leadIdx: Int, followerIdx: Int): HashMap<Int, List<String>> {
        val ret = hashMapOf<Int, List<String>>()
        val leadSubscriptions = arrayListOf<String>()
        leadSubscriptions.add("/root/被动雷达/探测信息")
        ret[leadIdx] = leadSubscriptions
        return ret
    }
}

object ESMPositioningSNGenerator : SNGenerator() {
    override fun getLeadNotifierList(leadIdx: Int, followerIdx: Array<Int>): HashMap<Int, List<String>> {
        val ret = hashMapOf<Int, List<String>>()
        val selfNotifier = arrayListOf<String>()
        selfNotifier.add("/root//被动雷达/探测信息")
        ret[leadIdx] = selfNotifier
        followerIdx.forEach {
            val followerNotifier = arrayListOf<String>()
            followerNotifier.add("/root/global/$it/被动雷达/探测信息")
            ret[it] = followerNotifier
        }
        return ret
    }

    override fun getFollowerSubscriberList(leadIdx: Int, followerIdx: Int): HashMap<Int, List<String>> {
        val ret = hashMapOf<Int, List<String>>()
        val leadSubscriptions = arrayListOf<String>()
        leadSubscriptions.add("/root/被动雷达/探测信息")
        ret[leadIdx] = leadSubscriptions
        return ret
    }
}

object GuidanceSNGenerator : SNGenerator() {
    override fun getLeadNotifierList(leadIdx: Int, followerIdx: Array<Int>): HashMap<Int, List<String>> {
        val ret = hashMapOf<Int, List<String>>()
        val selfNotifier = arrayListOf<String>()
        selfNotifier.add("/root/引导雷达/目标位置")
        return ret
    }

    override fun getFollowerNotifierList(leadIdx: Int, followerIdx: Int): HashMap<Int, List<String>> {
        val ret = hashMapOf<Int, List<String>>()
        ret[leadIdx] = arrayListOf("/root/引导雷达/目标位置")
        return ret
    }
}

val SNGenerators = hashMapOf(
    Pair(CoordinationType.FormationKeep, FormationKeepSNGenerator),
    Pair(CoordinationType.ESMPositioning, ESMPositioningSNGenerator),
    Pair(CoordinationType.ActiveRadarPositioning, ActiveRadarPositioningSNGenerator),
    Pair(CoordinationType.Guidance, GuidanceSNGenerator),
)


data class NodeUIData(
    val nodeId: Int,
    val nodeState: String,
    val notifierList: HashMap<Int, List<String>>,
    val subscriberList: HashMap<Int, List<String>>,
    val appList: List<CoordinationType>
)

data class CoordGroupData(
    val taskName: String,
    val taskId: Int,
    val leadIdx: Int,
    val members: ArrayList<NodeUIData>
)

data class UIData(
    val curTime: Int,
    val environmentEvent: List<String>,
    val currentOnlineNodes: HashSet<Int>,
    val groupListInfo: List<CoordGroupData>
)