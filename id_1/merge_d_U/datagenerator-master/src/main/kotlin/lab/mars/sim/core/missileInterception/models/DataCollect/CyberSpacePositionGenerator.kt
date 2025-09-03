package lab.mars.sim.core.missileInterception.models.DataCollect

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.script.Config
import lab.mars.windr.agentSimArch.utility.VectorFactory
import java.util.HashSet
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random


private val randomDevice = Random(0)

private fun generateRandomPos2D(centerPos: Vector2, theta : Float, radius: Float): Vector2 {
    val x = radius * cos(theta)
    val y = radius * sin(theta)
    return Vector2(centerPos.x + x.toFloat(), centerPos.y + y.toFloat())
}

private fun isLineCrossed(
    Ax1: Float,
    Ay1: Float,
    Ax2: Float,
    Ay2: Float,
    Bx1: Float,
    By1: Float,
    Bx2: Float,
    By2: Float
): Boolean {
    return if (
        (max(Ax1, Ax2) >= min(Bx1, Bx2) && min(Ax1, Ax2) <= max(Bx1, Bx2)) && //判断x轴的投影是否相交
        (max(Ay1, Ay2) >= min(By1, By2) && min(Ay1, Ay2) <= max(By1, By2)) //判断y轴投影是否相交
    ) {
        ((Bx1 - Ax1) * (Ay2 - Ay1) - (By1 - Ay1) * (Ax2 - Ax1)) *
                ((Bx2 - Ax1) * (Ay2 - Ay1) * (Ax2 - Ax1)) <= 0 &&    //判断B是否跨过A
                ((Ax1 - Bx1) * (By2 - By1) - (Ay1 - By1) * (Bx2 - Bx1)) *
                ((Ax2 - Bx1) * (By2 - By1) - (Ay2 - By1) * (Bx2 - Bx1)) <= 0 //判断A是否跨过B
    } else {
        false
    }
}


private fun hasCrossedLines(fromId: Int, toId: Int, Apos2: Vector2,
                            configuredGraph : HashMap<Int, HashSet<Int>>, generatedPositions : HashMap<Int, Vector3>): Boolean {
    val Apos1 = generatedPositions[fromId]!!
    outter@ for ((nodeId, neighbors) in configuredGraph) {
        if (nodeId == fromId) {
            continue@outter
        }
        if (!generatedPositions.containsKey(nodeId)) {
            continue@outter
        }
        val Bpos1 = generatedPositions[nodeId]!!
        inner@ for (it in neighbors) {
            if (it == fromId) {
                continue@inner
            }
            if (!generatedPositions.containsKey(it)) {
                continue@inner
            }
            val Bpos2 = generatedPositions[it]!!
            if (isLineCrossed(
                    Apos1.x, Apos1.y,
                    Apos2.x, Apos2.y,
                    Bpos1.x, Bpos1.y,
                    Bpos2.x, Bpos2.y
                )
            ) {
                return true
            }
        }
    }
    return false
}

private fun resolvePositionConflicts(limitSize: Float, generatedPositions : HashMap<Int, Vector3>) {
    generatedPositions.forEach outter@{ (nodeId, position) ->
        while (true) {
            var needToResolve = false
            generatedPositions.forEach inner@{ (iid, ipos) ->
                if (nodeId == iid) {
                    return@inner
                }
                if (VectorFactory.distance(position, ipos) <= limitSize * 5) {
                    needToResolve = true
                    return@inner
                }
            }
            if (needToResolve) {
                val offset = randomDevice.nextFloat() * limitSize * 10 - limitSize * 5
                val plusOrSubtract = if (randomDevice.nextInt(1) == 0) -1 else 1
                when (randomDevice.nextInt(2)) {
                    0 -> position.x += offset * plusOrSubtract
                    1 -> position.y += offset * plusOrSubtract
                }
            } else {
                break
            }
        }
    }
}
val generatedPositions = hashMapOf(
    Pair(1, Vector3(375f, 250f, 0f))
)

fun recursivelyGeneratePositions(nodeId: Int, configuredGraph : HashMap<Int, HashSet<Int>>) : HashMap<Int, Vector3> {
    if (! configuredGraph.containsKey(nodeId)) {
        return hashMapOf()
    }
    val neighbors = configuredGraph[nodeId]!!
    val selfPos = generatedPositions[nodeId]!!
    val neighborCount = neighbors.size
    val neighborRotateAngleDif = 360f / neighborCount
    var neighborRotateAngle = 0f
    for (it in neighbors) {
        if (generatedPositions.containsKey(it)) {
            continue
        }
        while (true) {
            val newPos2DAndAngle =
                generateRandomPos2D(Vector2(selfPos.x, selfPos.y), neighborRotateAngle, 100f)
            val neighborPosition = Vector3(newPos2DAndAngle.x, newPos2DAndAngle.y, 0f)
            if (hasCrossedLines(nodeId, it, Vector2(neighborPosition.x, neighborPosition.y), configuredGraph, generatedPositions)) {
                continue
            }
            generatedPositions[it] = neighborPosition
            resolvePositionConflicts(10f, generatedPositions)
            recursivelyGeneratePositions(it, configuredGraph)
            break
        }
        neighborRotateAngle += neighborRotateAngleDif
    }
    return generatedPositions
}