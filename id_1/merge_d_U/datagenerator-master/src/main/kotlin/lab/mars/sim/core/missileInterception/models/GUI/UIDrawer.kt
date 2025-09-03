package lab.mars.sim.core.missileInterception.models.GUI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.AgentStatics
import lab.mars.sim.core.missileInterception.agent.MissileAgent
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics
import lab.mars.sim.core.missileInterception.models.DataCollect.format
import lab.mars.sim.core.missileInterception.script.Config
import lab.mars.sim.core.missileInterception.script.CoordinationType
import lab.mars.sim.core.missileInterception.script.EnvironmentEType
import lab.mars.sim.core.missileInterception.script.secondNow
import lab.mars.sim.core.util.Return
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.simArchGraphics.Drawer
import lab.mars.windr.simUtility.model.ui.UILabel

class UIDrawer : Drawer {
    lateinit var agents: MutableList<Agent>

    private fun updateXXStatus(state: MutableMap<String, Any>) {
        val statusLabel = state["statusLabel"] as UILabel
        val sb = StringBuilder("\n")
        agents.forEach {
            val pos = it.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            val dir = it.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentDirection)
            sb.append("${it.getID()}:\n")
            sb.append("POS:${pos.format(2)}\nDIR:${dir.format(2)}\n")
        }
        statusLabel.setText(sb.toString())
        statusLabel.updateTransform(
            Gdx.graphics.width.toFloat() - 300f,
            Gdx.graphics.height.toFloat() - 30f, 0f, 0f
        )
    }


    class CoordinationEventLabelSlot(val x: Float, val y: Float, var label: CoordinationEventLabel? = null)

    private val coordinationEventLabels = arrayListOf(
        CoordinationEventLabelSlot(10f, 100f, null),
        CoordinationEventLabelSlot(220f, 100f, null),
        CoordinationEventLabelSlot(430f, 100f, null),
        CoordinationEventLabelSlot(640f, 100f, null),
        CoordinationEventLabelSlot(850f, 100f, null),
        CoordinationEventLabelSlot(1060f, 100f, null),
        CoordinationEventLabelSlot(1270f, 100f, null),
        CoordinationEventLabelSlot(1480f, 100f, null),
        CoordinationEventLabelSlot(1690f, 100f, null)
    )

    private lateinit var deviceStatusLabel: DeviceStatusLabel

    private var coordIdx = 0
    private fun findNextCoordinationEventLabelPosition() {
        coordIdx++
        if (coordIdx >= coordinationEventLabels.size) {
            coordIdx = 0
        }
    }

    fun updateCoordinationEvent(state: MutableMap<String, Any>) {
        if (lastTime == Config.timeLine.endTimeSecond) {
            return
        }
        val coordinationEvents = Config.timeLine.getCurrentCoordinationEvents()
        coordinationEvents.forEach { event ->
            coordinationEventLabels.find { it.label != null && it.label!!.eventId == event.idx }?.apply {
                this.label!!.updateEvent(event, state)
                return@forEach
            }
            if (event.type == CoordinationType.Dismiss) {
                val l = coordinationEventLabels.find { it.label?.eventId == event.idx }!!
                l.label!!.updateEvent(event, state)
                return@forEach
            }
            val l = coordinationEventLabels[coordIdx]
            l.label?.dispose(state)
            l.label = CoordinationEventLabel(
                l.x,
                l.y,
                event.idx,
                event.type,
                event.time,
                event.lead,
                event.followers.toHashSet(),
                state
            )
            findNextCoordinationEventLabelPosition()
        }
        coordinationEventLabels.forEach {
            it.label?.update(state)
        }
    }

    private val environmentEventStringSize = 10
    private val environmentEventStrings = java.util.LinkedList<String>()
    private var scoreBoard: ScoreBoard? = null
    private val disabledPlatforms = hashSetOf<Int>()
    private fun updateEnvironmentEvent(state: MutableMap<String, Any>) {
        val envEventLabel = state["envEventLabel"]!! as UILabel
        val environmentEvents = Config.timeLine.getCurrentEnvironmentEvents()
        while (environmentEvents.size > (environmentEventStringSize - environmentEventStrings.size)) {
            environmentEventStrings.removeFirst()
        }
        environmentEvents.forEach {
            if (disabledPlatforms.contains(it.affectedPlatform)) {
                return@forEach
            }
            if (it.type == EnvironmentEType.PlatformDisabled) {
                disabledPlatforms.add(it.affectedPlatform)
            }
            if (it.type == EnvironmentEType.HitTarget) {
                disabledPlatforms.add(it.affectedPlatform)
                if (scoreBoard == null) {
                    scoreBoard = ScoreBoard(Gdx.graphics.width * 2 / 3f, Gdx.graphics.height * 2 / 3f, state)
                }
                scoreBoard!!.updateScore(
                    it.affectedPlatform,
                    agents[it.affectedPlatform - 1].getState(MissileAgentStatics.MissileAgentStateName.WarheadProbability)
                )
            }
            environmentEventStrings.add("${secondNow()}: Missile${it.affectedPlatform} - ${it.type.name}\n")
        }
        val sb = StringBuilder("Time: $lastTime seconds\nENV Event:\n")
        environmentEventStrings.forEach {
            sb.append(it)
        }
        envEventLabel.setText(sb.toString())
        envEventLabel.updateTransform(30f, Gdx.graphics.height.toFloat() - 30f, 0f, 0f)
        val timeLabel = state["timeLabel"]!! as UILabel
        timeLabel.setText("Time: $lastTime seconds")
        timeLabel.updateTransform(30f, 330f, 0f, 0f)
    }

    override fun initial(state: MutableMap<String, Any>, stepDuration: Float, remainingDuration: Float): Return {
        agents = Global.findAgents(MissileAgent::class.java)
        agents.sortBy { it.getState<Int>(MissileAgentStatics.MissileAgentStateName.Id) }
        val envEventLabel = UILabel(
            30f,
            Gdx.graphics.height.toFloat() - 30f,
            1,
            UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 20, Color.YELLOW),
            "CurrentTime:"
        )
        val xxStatusLabel = UILabel(
            Gdx.graphics.width.toFloat() - 200f,
            Gdx.graphics.height.toFloat() - 30f,
            1,
            UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 20, Color.YELLOW), ""
        )
        state["statusLabel"] = xxStatusLabel
        state["envEventLabel"] = envEventLabel
        state["timeLabel"] = UILabel(30f, 330f, 2, UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 30, Color.YELLOW), "")
        deviceStatusLabel = DeviceStatusLabel(Gdx.graphics.width / 2f, Gdx.graphics.height - 400f, state)
        return Return.Finish
    }

    private var lastTime = -1

    override fun update(state: MutableMap<String, Any>, stepDuration: Float, remainingDuration: Float): Return {
        if (secondNow() == Config.timeLine.endTimeSecond) {
            return Return.Finish
        }
        if (secondNow() != lastTime) {
            lastTime = secondNow()
            updateCoordinationEvent(state)
            updateEnvironmentEvent(state)
        }
        updateXXStatus(state)
        deviceStatusLabel.update(Gdx.graphics.width / 2f - 325, Gdx.graphics.height - 250f, state)
        return Return.Finish
    }
}