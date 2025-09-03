package lab.mars.sim.core.missileInterception.models.GUI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import lab.mars.sim.core.missileInterception.script.CoordinationEvent
import lab.mars.sim.core.missileInterception.script.CoordinationType
import lab.mars.sim.core.missileInterception.script.TimeRange
import lab.mars.sim.core.missileInterception.script.secondNow
import lab.mars.windr.simUtility.model.ui.UIImage
import lab.mars.windr.simUtility.model.ui.UILabel

class CoordinationEventLabel(
    val x: Float,
    val y: Float,
    val eventId: Int,
    val eventType: CoordinationType,
    var time: TimeRange,
    var leadIdx: Int,
    val followers: HashSet<Int>,
    state: MutableMap<String, Any>
) {
    private lateinit var timeLabel: UILabel
    private lateinit var leadLabel: UILabel
    private val followerLabels = ArrayList<UILabel>()
    private lateinit var coordinationLabel: UILabel
    private lateinit var image: UIImage
    private lateinit var pixMap: Pixmap
    private val dismissedFollowers = HashSet<Int>()

    private companion object {
        val CoordinationEventNameString = hashMapOf(
            Pair(CoordinationType.FormationKeep, "Formation"),
            Pair(CoordinationType.ESMPositioning, "ESM"),
            Pair(CoordinationType.ActiveRadarPositioning, "ActiveRadar"),
            Pair(CoordinationType.Guidance, "Guidance"),
            Pair(CoordinationType.Dismiss, "Dismiss")
        )

        val pixMapWidth = 200
        val pixMapHeight = 200
        val pixMapCircleRadius = 10

        val leadCircleX = pixMapWidth / 2
        val leadCircleY = pixMapHeight / 4
        val followerCircleY = pixMapHeight / 5 * 3
        val progressBarHeight = 20
        val progressBarY = pixMapHeight - progressBarHeight
        val font = UILabel.FontParameter("assets/ui/font/ARLRDBD.TTF", 20, Color.YELLOW)
    }

    private fun initLabels(state: MutableMap<String, Any>) {
        this.followerLabels.forEachIndexed { idx, _ ->
            state.remove("coordlabel-${eventId}-$idx")
        }
        this.followerLabels.clear()
        this.followers.addAll(dismissedFollowers)
        leadLabel = UILabel(
            x + leadCircleX - 6,
            y + (pixMapHeight - leadCircleY) + 6,
            1,
            font,
            "${leadIdx}"
        )
        coordinationLabel = UILabel(
            x,
            y + pixMapHeight,
            1,
            font,
            "Grp.${eventId}:${CoordinationEventNameString[eventType]!!}"
        )
        pixMap = Pixmap(pixMapWidth, pixMapHeight, Pixmap.Format.RGBA8888)
        val step = pixMapWidth / (followers.size + 1)
        pixMap.setColor(Color.CYAN)
        pixMap.drawRectangle(0, 0, pixMapWidth, pixMapHeight)
        var followerXStart = step
        followers.forEach {
            if (!dismissedFollowers.contains(it)) {
                pixMap.drawLine(followerXStart, followerCircleY, leadCircleX, leadCircleY)
            }
            val followerLabel =
                UILabel(x + followerXStart - 6, y + (pixMapHeight - followerCircleY) + 6, 1, font, "${it}")
            followerLabels.add(followerLabel)
            followerXStart += step

        }
        pixMap.setColor(Color.FOREST)
        pixMap.fillCircle(leadCircleX, leadCircleY, pixMapCircleRadius)
        followerXStart = step
        followers.forEach {
            if (dismissedFollowers.contains(it)) {
                pixMap.setColor(Color.RED)
            } else {
                pixMap.setColor(Color.LIME)
            }
            pixMap.fillCircle(followerXStart, followerCircleY, pixMapCircleRadius)
            followerXStart += step
        }
        pixMap.setColor(Color.FOREST)
        pixMap.drawRectangle(0, progressBarY, pixMapWidth, progressBarHeight)
        val text = "${time.from}-${time.until}"
        timeLabel = UILabel(
            x + 1 + (text.length * 4),
            y + 17f,
            1,
            font,
            text
        )
        image = UIImage(Texture(pixMap), x, y, 0)
        state["image-${eventId}"] = image
        state["label-${eventId}"] = timeLabel
        state["leadlabel-${eventId}"] = leadLabel
        state["coordlabel-${eventId}"] = coordinationLabel
        followerLabels.forEachIndexed { index, i ->
            state["coordlabel-${eventId}-$index"] = i
        }
    }

    init {
        initLabels(state)
    }

    fun update(state: MutableMap<String, Any>) {
        if (time.withinRange(secondNow())) {
            pixMap.setColor(Color.FOREST)
            val now = secondNow() - time.from
            val length = time.length().toFloat()
            pixMap.fillRectangle(0, progressBarY, (pixMapWidth / length * now).toInt(), progressBarHeight)
        } else {
            pixMap.setColor(Color.GRAY)
            pixMap.fillRectangle(0, progressBarY, pixMapWidth, progressBarHeight)
            coordinationLabel.setText("${CoordinationEventNameString[eventType]!!}\n${eventId}-Dismissed")
        }
        image = UIImage(Texture(pixMap), x, y, 0)
        state["image-${eventId}"] = image
    }

    fun dispose(state: MutableMap<String, Any>) {
        state.remove("label-${eventId}")
        state.remove("image-${eventId}")
        state.remove("leadlabel-${eventId}")
        state.remove("coordlabel-${eventId}")
        followerLabels.forEachIndexed { index, i ->
            state.remove("coordlabel-${eventId}-$index")
        }
    }

    fun updateEvent(event: CoordinationEvent, state: MutableMap<String, Any>) {
        if (event.type == this.eventType && event.lead == this.leadIdx &&
            this.followers.containsAll(event.followers.toList())
        ) {
            return
        }
        if (event.type == CoordinationType.Dismiss) {
            if (event.followers.isEmpty()) {
                this.dismissedFollowers.add(event.lead)
            } else {
                this.dismissedFollowers.addAll(event.followers)
            }
        } else if (event.type == this.eventType) {
            if (event.idx == 5) {
                println()
            }
            this.time = event.time
            if (this.leadIdx != event.lead) {
                this.dismissedFollowers.add(this.leadIdx)
            }
            this.leadIdx = event.lead
            this.dismissedFollowers.addAll(this.followers.filter { !event.followers.contains(it) })
            this.followers.addAll(event.followers)
        }
        initLabels(state)
    }
}