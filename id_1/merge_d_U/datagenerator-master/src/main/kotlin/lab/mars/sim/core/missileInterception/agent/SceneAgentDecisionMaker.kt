package lab.mars.sim.core.missileInterception.agent

import lab.mars.windr.agentSimArch.decidable.Decidable

abstract class SceneAgentDecisionMaker : Decidable(){

    abstract fun Observe()

    abstract fun Action()

    override fun act() {
        Observe()
        Action()
    }
}