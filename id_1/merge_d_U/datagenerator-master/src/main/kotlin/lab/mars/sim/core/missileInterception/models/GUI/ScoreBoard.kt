package lab.mars.sim.core.missileInterception.models.GUI

import com.badlogic.gdx.graphics.Color
import lab.mars.windr.simUtility.model.ui.UIImage
import lab.mars.windr.simUtility.model.ui.UILabel

class ScoreBoard(val x: Float, val y: Float, val state: MutableMap<String, Any>) {
    val title: UILabel
    val scoreBoard: UILabel
    val backGround: UIImage
    var score = 0f
    val hitMissileProbs = hashMapOf<Int, Float>()

    init {
        title = UILabel(x, y + 300, 1, UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 50, Color.GREEN), "Hit Missiles: ")
        scoreBoard = UILabel(x, y + 200, 1, UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 30, Color.GREEN), "")
        backGround = UIImage("assets/ui/label.png", x, y, 0)
        scoreBoard.updateTransform(x, y + 200, 250f, 250f)
        backGround.updateTransform(x  - 15f, y - 180f, 350f, 500f)
        state["scoreBoardBackGround"] = backGround
        state["scoreBoardLabel"] = scoreBoard
        state["scoreBoardTitle"] = title
    }

    fun updateScore(hitIdx: Int, probability: Float) {
        hitMissileProbs[hitIdx] = probability
        score = 1f
        var mul = 1f
        val sb = StringBuilder()
        hitMissileProbs.forEach { (idx, prob) ->
            sb.append("Missile $idx Hit: ${(prob * 100).toInt()}%\n")
            mul *= 1 - prob
        }
        score = 1 - mul
        sb.append("Final Score: ${(score * 100).toInt()}%")
        scoreBoard.setText(sb.toString())
    }

}