package lab.mars.sim.core.missileInterception.models.DataSender

import com.google.gson.GsonBuilder

val gson = GsonBuilder().create()

enum class InformationType(val value: Int) {
    NetGraph(1),
    MessageLog(2)
}

class MessageLog(val time: String, val from: Int, val to: Int, val type: String, val log: String) {
    override fun toString(): String {
        return "MessageLog(time='$time', from=$from, to=$to, type='$type', log='$log')"
    }
}

class NodeInfo(val x : Double = 0.0,
               val y : Double = 0.0,
               val z : Double = 0.0,
               val ip : String = "",
               val id : Int = 0,
               val distance : Double = 0.0,
               val neighbor : HashSet<Int> = hashSetOf())

class NetGraph {
    val graphMap = hashMapOf<Int, NodeInfo>()
}

class AdhocInfo {
    val Type: Int = 0
    val Content: String = ""

    inline fun <reified T> getValue(): T {
        return gson.fromJson(Content, T::class.java)
    }
}
