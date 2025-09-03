package lab.mars.sim.core.missileInterception.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics.FormationKeepFollowerPositionOffsets
import lab.mars.sim.core.missileInterception.agent.MissileAgentStatics.MissileColorsInCoordination
import lab.mars.sim.core.missileInterception.controller.MissileAutoPilotController
import lab.mars.sim.core.missileInterception.models.Radar.Radar
import lab.mars.sim.core.missileInterception.script.*
import lab.mars.windr.agentSimArch.agent.Agent
import lab.mars.windr.agentSimArch.decidable.Decidable
import lab.mars.windr.agentSimArch.game.Global
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.util.*

class MissileScriptExecutor : Decidable() {

    private var random: Random? = null

    var targetInc = Vector3.Zero
    fun doFormationKeepAsLead(envEvent: EnvironmentEvent?) {
        val front = getState<Vector3>(AgentStatics.GenericAgentStateName.Front)
        val currentPosition = getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
        if (targetInc == Vector3.Zero) {
            val speed = getState<Float>(AgentStatics.GenericAgentStateName.Speed)
            targetInc =
                VectorFactory.subtract(front, currentPosition).scl(speed)
            setState(
                AgentStatics.GenericAgentStateName.NextTargetPosition,
                VectorFactory.add(currentPosition, targetInc)
            )
            val targetList = ArrayList<Vector3>()
            val t = currentPosition.cpy()
            (0 until 100).forEach {
                targetList.add(t.cpy().add(targetInc.cpy().scl(Global.getDeltaTime())))
                t.add(targetInc)
            }
            setState(MissileAgentStatics.MissileAgentStateName.MissilePredefinedTargets, targetList)
        } else {
            val currentTarget = getState<Vector3>(AgentStatics.GenericAgentStateName.NextTargetPosition)
            val thisPosition = getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            val target = VectorFactory.add(currentTarget, targetInc)
            setState(AgentStatics.GenericAgentStateName.NextTargetPosition, target)
        }
        setState(AgentStatics.GenericAgentStateName.Color, MissileColorsInCoordination[selfIdx])
        this.environmentEvent = envEvent
        if (this.environmentEvent == null) {
            return
        }
        this.processPositionDriftAndReturnEvent()
    }

    private fun processPositionDriftAndReturnEvent() {
        when (this.environmentEvent!!.type) {
            EnvironmentEType.PositionDrift -> setState(
                MissileAgentStatics.MissileAgentStateName.MissilePilotStatus,
                MissileAutoPilotController.MissilePilotStatus.BeginDrift
            )

            EnvironmentEType.PositionReturn -> setState(
                MissileAgentStatics.MissileAgentStateName.MissilePilotStatus,
                MissileAutoPilotController.MissilePilotStatus.EndDrift
            )

            else -> return
        }
    }

    private fun doFormationKeepAsFollower(coordinationEvent: CoordinationEvent, envEvent: EnvironmentEvent?) {
        val formationLeadIdx = coordinationEvent.lead
        val leadColor = MissileColorsInCoordination[formationLeadIdx]!!
        setState(AgentStatics.GenericAgentStateName.Color, Color(leadColor.r / 2, leadColor.g / 2, leadColor.b / 2, 1f))
        val lead = Global.findAgent("Missile$formationLeadIdx")
        val leadTarget = lead.getState<Vector3?>(AgentStatics.GenericAgentStateName.NextTargetPosition)
        if (leadTarget == null) {
            val speed = getState<Float>(AgentStatics.GenericAgentStateName.Speed)
            val leadPosition = lead.getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            val leadFront = lead.getState<Vector3>(AgentStatics.GenericAgentStateName.Front)
            val thisFront = getState<Vector3>(AgentStatics.GenericAgentStateName.Front)
            val thisTop = getState<Vector3>(AgentStatics.GenericAgentStateName.Top)
            val thisRight = getState<Vector3>(AgentStatics.GenericAgentStateName.Right)
            val thisPosition = getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            val newPosition = leadPosition.cpy().add(FormationKeepFollowerPositionOffsets[selfIdx])
                .add(VectorFactory.subtract(leadFront, leadPosition).scl(speed * Global.getDeltaTime()))
            val newFront = VectorFactory.add(newPosition, VectorFactory.subtract(thisFront, thisPosition))
            val newTop = VectorFactory.add(newPosition, VectorFactory.subtract(thisTop, thisPosition))
            val newRight = VectorFactory.add(newPosition, VectorFactory.subtract(thisRight, thisPosition))
            setState(AgentStatics.GenericAgentStateName.CurrentPosition, newPosition)
            setState(AgentStatics.GenericAgentStateName.Front, newFront)
            setState(AgentStatics.GenericAgentStateName.Top, newTop)
            setState(AgentStatics.GenericAgentStateName.Right, newRight)
            (bindAgent as MissileAgent).positionReset = true
            targetInc = VectorFactory.subtract(newFront, newPosition).scl(speed)
            setState(
                AgentStatics.GenericAgentStateName.NextTargetPosition,
                VectorFactory.add(newPosition, targetInc.cpy())
            )
            val targetList = ArrayList<Vector3>()
            val t = newPosition.cpy()
            (0 until 100).forEach {
                targetList.add(t.cpy().add(targetInc.cpy().scl(Global.getDeltaTime())))
                t.add(targetInc)
            }
            setState(MissileAgentStatics.MissileAgentStateName.MissilePredefinedTargets, targetList)
        } else {
            val currentTarget = getState<Vector3>(AgentStatics.GenericAgentStateName.NextTargetPosition)
            val thisPosition = getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
            val thisTarget = currentTarget.add(targetInc)
            setState(AgentStatics.GenericAgentStateName.NextTargetPosition, thisTarget)
        }
        if (envEvent == this.environmentEvent) {
            return
        }
        this.environmentEvent = envEvent
        if (this.environmentEvent == null) {
            return
        }
        processPositionDriftAndReturnEvent()
    }

    private fun getSelfFrontRightTop(): Triple<Vector3, Vector3, Vector3> {
        val front = getState<Vector3>(AgentStatics.GenericAgentStateName.Front)
        val right = getState<Vector3>(AgentStatics.GenericAgentStateName.Right)
        val top = getState<Vector3>(AgentStatics.GenericAgentStateName.Top)
        val position = getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
        return Triple(
            VectorFactory.subtract(front, position),
            VectorFactory.subtract(right, position),
            VectorFactory.subtract(top, position)
        )
    }

    private fun getYawPitchOffset(targetPosition: Vector3): Vector2 {
        val position = getState<Vector3>(AgentStatics.GenericAgentStateName.CurrentPosition)
        val relative = VectorFactory.subtract(targetPosition, position)
        val selfFRT = getSelfFrontRightTop()
        val front = selfFRT.first
        val right = selfFRT.second
        val top = selfFRT.third
        val yawOffset = VectorFactory.angle(front, VectorFactory.projectOnPlane(relative, top))
        val pitchOffset = VectorFactory.angle(front, VectorFactory.projectOnPlane(relative, right))
        return Vector2(yawOffset, pitchOffset)
    }

    private fun getPosition(agent: Agent): Vector3 {
        return agent.getState(AgentStatics.GenericAgentStateName.CurrentPosition)
    }

    fun Float.clip(min: Float, max: Float): Float {
        if (this < min) {
            return min
        } else if (this > max) {
            return max
        }
        return this
    }

    var esmSignalShip: Agent? = null
    fun doESM(coordinationEvent: CoordinationEvent, event: EnvironmentEvent?) {
        if (esmSignalShip == null) {
            val shipAgents = Global.findAgents(ShipAgent::class.java)
            val shipIdx = random!!.nextInt(shipAgents.size)
            esmSignalShip = shipAgents[shipIdx]!!
        }
        val position = getPosition(esmSignalShip!!)
        val offset = getYawPitchOffset(position)
        if (event == null) {
            val credibility = random!!.nextFloat() * 0.2f
            val esmData = MissileAgentStatics.ESMRadarData(
                MissileAgentStatics.DeviceStatus.On,
                (offset.x + random!!.nextFloat() * 30 - 15).clip(0f, 360f),
                (offset.y + random!!.nextFloat() * 30 - 15).clip(0f, 180f),
                "Fussy",
                credibility
            )
            setState(MissileAgentStatics.MissileAgentStateName.ESMRadarData, esmData)
            return
        }
        if (event.type == EnvironmentEType.ESMHasTarget) {
            val credibility = random!!.nextFloat() * 0.2f + 0.3f
            val esmData = MissileAgentStatics.ESMRadarData(
                MissileAgentStatics.DeviceStatus.On,
                offset.x,
                offset.y,
                esmSignalShip!!.getID(),
                credibility
            )
            setState(MissileAgentStatics.MissileAgentStateName.ESMRadarData, esmData)
        }
    }

    fun doActiveRadar(coordinationEvent: CoordinationEvent, event: EnvironmentEvent?) {
        if (coordinationEvent.lead == selfIdx) {
            setState(
                MissileAgentStatics.MissileAgentStateName.ESMRadarData,
                MissileAgentStatics.ESMRadarData(MissileAgentStatics.DeviceStatus.Off)
            )
            val radar = getState<Radar>(MissileAgentStatics.MissileAgentStateName.Radar)
            radar.SetOn(true)
        }

        if (event == null) {
            val carrier = Global.findAgent("Carrier1")
            val position = getPosition(carrier)
            val offset = getYawPitchOffset(position)
            val credibility = random!!.nextFloat() * 0.2f
            if (coordinationEvent.lead == selfIdx) {
                val activeRadarData = MissileAgentStatics.ActiveRadarData(
                    MissileAgentStatics.DeviceStatus.On,
                    (offset.x + random!!.nextFloat() * 30 - 15).clip(0f, 360f),
                    (offset.y + random!!.nextFloat() * 30 - 15).clip(0f, 180f),
                    "Fussy",
                    credibility
                )
                setState(MissileAgentStatics.MissileAgentStateName.ActiveRadarData, activeRadarData)
            } else {
                val esmRadarData = MissileAgentStatics.ESMRadarData(
                    MissileAgentStatics.DeviceStatus.On,
                    (offset.x + random!!.nextFloat() * 30 - 15).clip(0f, 360f),
                    (offset.y + random!!.nextFloat() * 30 - 15).clip(0f, 180f),
                    "Fussy",
                    credibility
                )
                setState(MissileAgentStatics.MissileAgentStateName.ESMRadarData, esmRadarData)
            }
            return
        }
        val carrier = Global.findAgent("Carrier1")
        val position = getPosition(carrier)
        val offset = getYawPitchOffset(position)
        val credibility = random!!.nextFloat() * 0.2f + 0.4f
        if (event.type == EnvironmentEType.ActiveHasTarget && coordinationEvent.lead == selfIdx) {
            val activeRadarData = MissileAgentStatics.ActiveRadarData(
                MissileAgentStatics.DeviceStatus.On,
                offset.x,
                offset.y,
                carrier.getID(),
                credibility
            )
            setState(MissileAgentStatics.MissileAgentStateName.ActiveRadarData, activeRadarData)
        } else if (event.type == EnvironmentEType.ESMHasTarget) {
            val esmRadarData = MissileAgentStatics.ESMRadarData(
                MissileAgentStatics.DeviceStatus.On,
                offset.x,
                offset.y,
                carrier.getID(),
                credibility
            )
            setState(MissileAgentStatics.MissileAgentStateName.ESMRadarData, esmRadarData)
        }
    }

    fun doGuidanceRadar(coordinationEvent: CoordinationEvent, event: EnvironmentEvent?) {
        val carrier = Global.findAgent("Carrier1")
        val carrierPosition = getPosition(carrier)
        if (coordinationEvent.lead == selfIdx) {
            setState(
                MissileAgentStatics.MissileAgentStateName.ESMRadarData,
                MissileAgentStatics.ESMRadarData(MissileAgentStatics.DeviceStatus.Off)
            )
            val radar = getState<Radar>(MissileAgentStatics.MissileAgentStateName.Radar)
            radar.SetOn(true)
            radar.SetTheta0_5(radar.GetTheta0_5() / 10)
            radar.SetPt(radar.GetPt() / 5)
            val thisPosition = getPosition(bindAgent)
            val offset = getYawPitchOffset(carrierPosition)
            val carrierDistance = VectorFactory.distance(carrierPosition, thisPosition)
            val guidanceOutput = MissileAgentStatics.GuidanceRadarOutputData(
                MissileAgentStatics.DeviceStatus.On,
                offset.x,
                offset.y,
                carrierDistance,
                carrier.getID()
            )
            setState(MissileAgentStatics.MissileAgentStateName.GuidanceRadarOutputData, guidanceOutput)
        }
        if (allMissiles.count {
                val alive = it.getState<Boolean>(AgentStatics.GenericAgentStateName.IsAlive)
                val deviceList = it.getState<HashSet<DeviceType>>(MissileAgentStatics.MissileAgentStateName.DeviceList)
                return@count alive && deviceList.contains(DeviceType.GuidanceRadar)
            } == 0) {
            setState(
                MissileAgentStatics.MissileAgentStateName.WarheadProbability,
                Config.WarHeadProbability[selfIdx]!! / 3
            )
        }
        setState(AgentStatics.GenericAgentStateName.NextTargetPosition, carrierPosition)
    }

    fun doDismiss(event: CoordinationEvent) {
        this.coordinationEvent.forEach {
            if (it.idx != event.idx) {
                return@forEach
            }
            when (it.type) {
                CoordinationType.FormationKeep -> {
                    val carrier = Global.findAgent("Carrier1")
                    val position = getPosition(carrier)
                    val randomOffset = Vector3(random!!.nextFloat() * 20 - 10, 0f, random!!.nextFloat() * 20 - 10)
                    val nextTarget = VectorFactory.add(position, randomOffset)
                    setState(AgentStatics.GenericAgentStateName.NextTargetPosition, nextTarget)
                }

                CoordinationType.ActiveRadarPositioning -> {
                    if (event.lead == selfIdx) {
                        setState(
                            MissileAgentStatics.MissileAgentStateName.ActiveRadarData,
                            MissileAgentStatics.ActiveRadarData()
                        )
                    } else {
                        setState(
                            MissileAgentStatics.MissileAgentStateName.ESMRadarData,
                            MissileAgentStatics.ESMRadarData()
                        )
                    }
                    val radar = getState<Radar>(MissileAgentStatics.MissileAgentStateName.Radar)
                    radar.SetOn(false)
                }

                CoordinationType.Guidance -> {
                    val guidanceOutput =
                        MissileAgentStatics.GuidanceRadarOutputData(MissileAgentStatics.DeviceStatus.Off)
                    setState(MissileAgentStatics.MissileAgentStateName.GuidanceRadarOutputData, guidanceOutput)
                    val radar = getState<Radar>(MissileAgentStatics.MissileAgentStateName.Radar)
                    radar.SetOn(false)
                }

                else -> {

                }
            }
        }
    }

    val coordinationEvent = HashSet<CoordinationEvent>()
    var environmentEvent: EnvironmentEvent? = null
    var currentRole = HashSet<XXRole>()
    var selfIdx = 0
    var lastTimeSecond = -1
    lateinit var allMissiles: List<Agent>
    override fun act() {
        if (this.getState<Boolean>(AgentStatics.GenericAgentStateName.IsAlive) == false) {
            return
        }
        if (secondNow() == lastTimeSecond || secondNow() == Config.timeLine.endTimeSecond) {
            return
        }
        lastTimeSecond = secondNow()
        val tl = Config.timeLine
        val idx = getState<Int>(MissileAgentStatics.MissileAgentStateName.Id)
        if (random == null) {
            allMissiles = Global.findAgents(MissileAgent::class.java)
            random = Random(idx.toLong())
        }
        val coordinateEvent = tl.getCurrentCoordinationEvents(idx)
        val envEvent = tl.getCurrentEnvironmentEvents(idx)
        val role = tl.getCurrentXXRoles(idx)
        selfIdx = idx
        if (envEvent?.type == EnvironmentEType.PlatformDisabled || envEvent?.type == EnvironmentEType.HitTarget) {
            this.setState(AgentStatics.GenericAgentStateName.IsAlive, false)
            return
        }
        if (envEvent?.type == EnvironmentEType.RandomDisabled) {
            val p = this.random!!.nextFloat()
            val deviceList = getState<HashSet<DeviceType>>(MissileAgentStatics.MissileAgentStateName.DeviceList)
            if (deviceList.contains(DeviceType.GuidanceRadar) && p <= Config.probabilityOfGuidanceXXDisabled) {
                this.setState(AgentStatics.GenericAgentStateName.IsAlive, false)
                return
            }
            if (deviceList.contains(DeviceType.ESMRadar) && p <= Config.probabilityOfESMXXDisabled) {
                this.setState(AgentStatics.GenericAgentStateName.IsAlive, false)
                return
            }
        }
        coordinateEvent.forEach { event ->
            if (event.type == CoordinationType.Dismiss) {
                doDismiss(event)
                this.coordinationEvent.removeIf {
                    event.lead == it.lead &&
                            event.followers.contentEquals(it.followers)
                }
            } else {
                this.coordinationEvent.add(event)
            }
        }
        this.currentRole = role
        this.coordinationEvent.forEach { event ->
            when (event.type) {
                CoordinationType.FormationKeep -> {
                    if (event.lead == idx) {
                        doFormationKeepAsLead(envEvent)
                    } else {
                        doFormationKeepAsFollower(event, envEvent)
                    }
                }

                CoordinationType.ESMPositioning -> {
                    doESM(event, envEvent)
                }

                CoordinationType.ActiveRadarPositioning -> {
                    doActiveRadar(event, envEvent)
                }

                CoordinationType.Guidance -> {
                    doGuidanceRadar(event, envEvent)
                }

                else -> {

                }
            }
        }

    }
}