import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

fun Color.withAlpha(a: Int) = Color(red, green, blue, a)
fun Color.shifted(id: Int): Color = Color.RGBtoHSB(red, green, blue, null)
    .let { (h, s, b) -> Color.getHSBColor(h + id * 0.1f + 0.5f, s, 1.0f - (1.0f - b) * 0.4f) }
    .withAlpha(alpha)

fun Color.withBrightness(b: Float): Color = Color.RGBtoHSB(red, green, blue, null)
    .let { (h, s, _) -> Color.getHSBColor(h, s, b) }
    .withAlpha(alpha)

fun Pair<Number, Number>.scaledInv(scale: Float) =
    Point2D.Float((second.toFloat() + 0.5f) * scale, (first.toFloat() + 0.5f) * scale)

fun Pair<Number, Number>.interpolated(dest: Pair<Number, Number>, progress: Float): Pair<Float, Float> {
    val (x1, y1) = this
    val (x2, y2) = dest
    return x1.toFloat() + (x2.toFloat() - x1.toFloat()) * progress to
            y1.toFloat() + (y2.toFloat() - y1.toFloat()) * progress
}


fun Pair<Number, Number>.scaled(scale: Float) =
    Point2D.Float((first.toFloat() + 0.5f) * scale, (second.toFloat() + 0.5f) * scale)

fun Graphics2D.drawStringCentered(
    str: String,
    at: Point2D,
    op: (String, Float, Float) -> Unit = { s, x, y -> drawString(s, x, y) }
) = fontMetrics.getStringBounds(str, this)
    .let { at.x - it.centerX to at.y - it.centerY }
    .let { (x, y) -> op(str, x.toFloat(), y.toFloat()) }

fun BufferedImage.useGraphics(op: (Graphics2D) -> Unit) = createGraphics().let {
    it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    op(it)
    it.dispose()
}

fun <S> display(
    state: AtomicReference<S>,
    title: String,
    dimension: Dimension = Dimension(600, 580),
    location: Point = Point(500, 300),
    op: (S, BufferedImage) -> Unit
) {
    val image = BufferedImage(
        dimension.width, dimension.height,
        BufferedImage.TYPE_INT_RGB
    ).also { op(state.get(), it) }
    var timer: Timer
    val panel = object : JPanel() {
        init {
            preferredSize = dimension
            size = preferredSize
            timer = Timer(10) {
                op(state.get(), image)
                repaint()
            }
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.drawImage(image, 0, 0, null)
        }
    }

    JFrame(title).apply {
        this.location = location
        add(panel)
        pack()
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        isVisible = true
    }

    timer.start()
}

fun linearInterpolation(x0: Double, x1: Double, t: Double) = x0 + (x1 - x0) * t
fun linearInterpolation(x0: Double, x1: Double, y0:Double, y1: Double, t: Double) = y0 + (y1 - y0) * (t - x0) / (x1 - x0)
