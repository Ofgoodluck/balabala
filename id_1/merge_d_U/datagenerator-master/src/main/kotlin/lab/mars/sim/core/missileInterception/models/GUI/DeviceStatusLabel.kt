package lab.mars.sim.core.missileInterception.models.GUI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.missileInterception.agent.MissileAgent
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics
import lab.mars.sim.core.missileInterception.script.DeviceType
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.simUtility.model.ui.UIImage
import lab.mars.windr.simUtility.model.ui.UILabel

class DeviceStatusLabel(val x: Float, val y: Float, state: MutableMap<String, Any>) {
    private val deviceText: UILabel
    private val agentIdText: UILabel
    private val warHeadText: UILabel
    private var image: UIImage
    private val pixMap: Pixmap
    private val agents: List<Agent>
    private val allDevices = DeviceType.values()

    init {
        deviceText =
            UILabel(x - 170, y + 210, 1, UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 25, Color.YELLOW), "")
        agentIdText =
            UILabel(x + 40, y + 240, 1, UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 25, Color.YELLOW), "")
        warHeadText =
            UILabel(x + 40, y + 100, 1, UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 17, Color.YELLOW), "")
        agents = Global.findAgents(MissileAgent::class.java)
        agents.sortBy { it.getState<Int>(MissileAgentStatics.MissileAgentStateName.Id) }
        val sb = StringBuilder()
        allDevices.forEach {
            sb.append("${it.name}\n")
        }
        deviceText.setText(sb.toString())
        sb.clear()
        agents.forEach {
            sb.append("${it.getState<Int>(MissileAgentStatics.MissileAgentStateName.Id)}      ")
        }
        agentIdText.setText(sb.toString())
        sb.clear()
        agents.forEach {
            sb.append("${(it.getState<Float>(MissileAgentStatics.MissileAgentStateName.WarheadProbability) * 100).toInt()}%    ")
        }
        warHeadText.setText(sb.toString())
        pixMap = Pixmap(600, 210, Pixmap.Format.RGBA8888)
        updatePixmap()
        image = UIImage(Texture(pixMap), x, y, 0)
        state["deviceLabel"] = deviceText
        state["agentLabel"] = agentIdText
        state["deviceStatusImage"] = image
        state["warHeadLabel"] = warHeadText
    }

    companion object {
        val xStep = 50
        val yStep = 30
        val radius = 10
    }

    private fun fillOnCircle(cx: Int, cy: Int) {
        pixMap.setColor(Color.GREEN)
        pixMap.fillCircle(cx, cy, radius)
    }

    private fun fillOffCircle(cx: Int, cy: Int) {
        pixMap.setColor(Color.RED)
        pixMap.fillCircle(cx, cy, radius)
    }

    private fun fillNoCircle(cx: Int, cy: Int) {
        pixMap.setColor(Color.LIGHT_GRAY)
        pixMap.fillCircle(cx, cy, radius)
    }

    private fun fillCross(cx: Int, cy: Int) {
        pixMap.setColor(Color.PURPLE)
        pixMap.drawLine(cx + radius, cy + radius, cx - radius, cy - radius)
        pixMap.drawLine(cx + radius, cy - radius, cx - radius, cy + radius)
    }

    private fun updatePixmap() {
        var agentStartX = 50
        for (agent in agents) {
            val agentDeviceList =
                agent.getState<HashSet<DeviceType>>(MissileAgentStatics.MissileAgentStateName.DeviceList)
            var deviceStartY = 10
            if (agent.getState<Boolean>(AgentStatics.GenericAgentStateName.IsAlive) == false) {
                allDevices.forEach {
                    fillCross(agentStartX, deviceStartY)
                    deviceStartY += yStep
                }
                agentStartX += xStep
                continue
            }
            allDevices.forEach {
                if (!agentDeviceList.contains(it)) {
                    fillNoCircle(agentStartX, deviceStartY)
                } else {
                    when (it) {
                        DeviceType.FlightController -> {
                            fillOnCircle(agentStartX, deviceStartY)
                        }

                        DeviceType.ActiveRadar -> {
                            val activeRadarInfo =
                                agent.getState<MissileAgentStatics.ActiveRadarData>(MissileAgentStatics.MissileAgentStateName.ActiveRadarData)
                            if (activeRadarInfo.status == MissileAgentStatics.DeviceStatus.On) {
                                fillOnCircle(agentStartX, deviceStartY)
                            } else {
                                fillOffCircle(agentStartX, deviceStartY)
                            }
                        }

                        DeviceType.ESMRadar -> {
                            val esmRadarData =
                                agent.getState<MissileAgentStatics.ESMRadarData>(MissileAgentStatics.MissileAgentStateName.ESMRadarData)
                            if (esmRadarData.status == MissileAgentStatics.DeviceStatus.On) {
                                fillOnCircle(agentStartX, deviceStartY)
                            } else {
                                fillOffCircle(agentStartX, deviceStartY)
                            }
                        }

                        DeviceType.GuidanceRadar -> {
                            val guidanceRadarData =
                                agent.getState<MissileAgentStatics.GuidanceRadarOutputData>(MissileAgentStatics.MissileAgentStateName.GuidanceRadarOutputData)
                            if (guidanceRadarData.status == MissileAgentStatics.DeviceStatus.On) {
                                fillOnCircle(agentStartX, deviceStartY)
                            } else {
                                fillOffCircle(agentStartX, deviceStartY)
                            }
                        }

                        DeviceType.WarHead -> {

                        }

                        DeviceType.Time -> {

                        }
                    }
                }
                deviceStartY += yStep
            }
            agentStartX += xStep
        }
    }

    fun update(nx: Float, ny: Float, state: MutableMap<String, Any>) {
        updatePixmap()
        image = UIImage(Texture(pixMap), nx, ny, 0)
        deviceText.updateTransform(nx - 170, ny + 210, 0f, 0f)
        agentIdText.updateTransform(nx + 40, ny + 240, 0f, 0f)
        warHeadText.updateTransform(nx + 40, ny + 85, 0f, 0f)
        state["deviceStatusImage"] = image

    }
}