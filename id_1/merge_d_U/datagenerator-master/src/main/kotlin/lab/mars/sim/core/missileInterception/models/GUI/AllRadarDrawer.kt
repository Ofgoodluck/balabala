package lab.mars.sim.core.missileInterception.models.GUI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.missileInterception.models.Radar.RadarSimulationController
import lab.mars.sim.core.util.Return
import lab.mars.windr.simArchGraphics.Drawer
import lab.mars.windr.simUtility.drawer.radar.RadarDrawer
import lab.mars.windr.simUtility.model.radar.RadarEffectiveArea

class AllRadarDrawer : Drawer {
    override fun initial(p0: MutableMap<String, Any>?, p1: Float, p2: Float): Return {
        return Return.Finish
    }

    val radarDrawers = ArrayList<RadarDrawer>()

    override fun update(state: MutableMap<String, Any>, p1: Float, p2: Float): Return {
        val radarPositiveRadarEffective3DAreaMap = RadarSimulationController.GetInstance().GetAllPositiveRadars()
        if (radarDrawers.isEmpty()) {
            radarPositiveRadarEffective3DAreaMap.forEach { (key, area) ->
                val agentId = key.GetBindedAgent().id.toString()
                val drawer = RadarDrawer(agentId, "radar", {
                    val c = Color.YELLOW
                    c
                }, {
                    if (!key.IsOn() || key.GetBindedAgent().getState<Boolean>(AgentStatics.GenericAgentStateName.IsAlive) == false) {
                        return@RadarDrawer null
                    }
                    val a = RadarEffectiveArea()
                    val oldArea = RadarSimulationController.GetInstance().GetAllPositiveRadars()[key]!!
                    if (oldArea.GetAllRoundPoints().isEmpty()) {
                        return@RadarDrawer null
                    }
                    a.setRadarEffectiveArea(oldArea.GetAllRoundPoints())
                    val quater = Quaternion()
                    val currentDir = it.get<Vector3>(AgentStatics.GenericAgentStateName.CurrentDirection).value
                    quater.setEulerAngles(currentDir.x, currentDir.y, currentDir.z)
                    a.setTransform(it.get<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition).value, quater)
                    a
                })
                radarDrawers.add(drawer)
            }
        }
        radarDrawers.forEach {
            it.update(state, p1, p2)
        }
        return Return.Finish
    }
}