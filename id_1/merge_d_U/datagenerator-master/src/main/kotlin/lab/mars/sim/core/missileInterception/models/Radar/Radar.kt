package lab.mars.sim.core.missileInterception.models.Radar

import lab.mars.windr.agentSimArch.agent.Agent

/**
 * Created by imrwz on 2017/5/4.
 */
class Radar(
    _pt: Int,
    _on: Boolean,
    _lambda: Int,
    _theta0_5: Float,
    _minimumReceivableSignalPower: Float,
    _maxScanAngle: Float,
    _scanAngularVelocity: Float
) {
    private lateinit var _bindedAgent: Agent
    private var _pt = 200
    private var _on = false
    private var _lambda = 100
    private var _theta0_5 = 10f
    private var _minimumReceivableSignalPower = 10f
    private var _maxScanAngle = 45f
    private var _scanAngularVelocity = 40f
    private var _currentScanAngle = 0f
    private var _currentScanIsCounterClockwise = false
    var updated = true

    init {
        this._pt = _pt
        this._on = _on
        this._lambda = _lambda
        this._theta0_5 = _theta0_5
        this._minimumReceivableSignalPower = _minimumReceivableSignalPower
        this._maxScanAngle = _maxScanAngle
        this._scanAngularVelocity = _scanAngularVelocity
    }

    fun SetBindedAgent(agent: Agent) {
        _bindedAgent = agent
    }

    fun GetCurrentScanIsCounterClockwise(): Boolean {
        return _currentScanIsCounterClockwise
    }

    fun SetCurrentScanIsCounterClockwise(value: Boolean) {
        updated = true
        _currentScanIsCounterClockwise = value
    }

    fun GetPt(): Int {
        return _pt
    }

    fun GetBindedAgent(): Agent {
        return _bindedAgent
    }

    fun SetPt(_pt: Int) {
        updated = true
        this._pt = _pt
    }

    fun IsOn(): Boolean {
        return _on
    }

    fun SetOn(_on: Boolean) {
        this._on = _on
    }

    fun GetLambda(): Int {
        return _lambda
    }

    fun SetLambda(_lambda: Int) {
        updated = true
        this._lambda = _lambda
    }

    fun GetTheta0_5(): Float {
        return _theta0_5
    }

    fun SetTheta0_5(_theta0_5: Float) {
        updated = true
        this._theta0_5 = _theta0_5
    }

    fun GetMinimumReceivableSignalPower(): Float {
        return _minimumReceivableSignalPower
    }

    fun SetMinimumReceivableSignalPower(_minimumReceivableSignalPower: Float) {
        updated = true
        this._minimumReceivableSignalPower = _minimumReceivableSignalPower
    }

    fun GetMaxScanAngle(): Float {
        return _maxScanAngle
    }

    fun SetMaxScanAngle(_maxScanAngle: Float) {
        updated = true
        this._maxScanAngle = _maxScanAngle
    }

    fun GetScanAngularVelocity(): Float {
        return _scanAngularVelocity
    }

    fun SetScanAngularVelocity(_scanAngularVelocity: Float) {
        updated = true
        this._scanAngularVelocity = _scanAngularVelocity
    }

    fun GetCurrentScanAngle(): Float {
        return _currentScanAngle
    }

    fun SetCurrentScanAngle(_currentScanAngle: Float) {
        updated = true
        this._currentScanAngle = _currentScanAngle
    }

    fun Gt(theta: Float): Float {
        val part =
            (-ElectricityEnvironment.K * Math.pow((2 * theta / _theta0_5).toDouble(), 2.0)).toFloat()
        return Math.exp(part.toDouble()).toFloat()
    }
}