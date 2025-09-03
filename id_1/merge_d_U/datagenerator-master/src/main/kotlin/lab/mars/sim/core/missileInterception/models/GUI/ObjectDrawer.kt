package lab.mars.sim.core.missileInterception.models.GUI

import lab.mars.sim.core.util.Return
import lab.mars.windr.simArchGraphics.Drawer

/**
 * Created by imrwz on 6/20/2017.
 */
abstract class ObjectDrawer(id: String, assetPack: String, assetNode: String) : Drawer {
    var _id = ""
    protected var _assetPack = ""
    protected var _assetNode = ""

    init {
        _assetPack = assetPack
        _assetNode = assetNode
        _id = id
    }

    abstract fun Initial(state: MutableMap<String, Any>, stepDuration: Float, remaining: Float): Return
    abstract fun ReDraw(state: MutableMap<String, Any>, stepDuration: Float, remaining: Float): Return
    override fun initial(map: MutableMap<String, Any>, v: Float, v1: Float): Return {
        return Initial(map, v, v1)
    }

    override fun update(map: MutableMap<String, Any>, v: Float, v1: Float): Return {
        return ReDraw(map, v, v1)
    }
}