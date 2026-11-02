package com.vizeno.loomie.ui

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class LoomieMood { Idle, Listening, Thinking, Happy, Speaking }

private enum class LoomieTouch { None, Wink, Hearts }

// Single green yarn family — tubular shade / body / highlight.
private val Sage = Color(0xFF8FA994)
private val SageDeep = Color(0xFF6F8A76)
private val SageMid = Color(0xFF7F9A85)
private val SageLite = Color(0xFFB5C7B8)
private val SageCore = Color(0xFFA8BBAE)
private val Ink = Color(0xFF1E2420)
private val Blush = Color(0xFFE8B4BC)
private val HeartPink = Color(0xFFE27A8A)

@Composable
fun LoomieMascot(
    mood: LoomieMood = LoomieMood.Idle,
    size: Dp = 112.dp,
    talking: Boolean = false,
    talkLevel: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reduceMotion = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f
    val isTalking = talking || mood == LoomieMood.Speaking
    var touch by remember { mutableIntStateOf(0) } // 0 none, 1 wink, 2 hearts
    val touchKind = when (touch) {
        1 -> LoomieTouch.Wink
        2 -> LoomieTouch.Hearts
        else -> LoomieTouch.None
    }
    LaunchedEffect(touch) {
        if (touch != 0) {
            delay(if (touch == 2) 1600 else 1200)
            touch = 0
        }
    }
    val transition = rememberInfiniteTransition(label = "loomie")
    val floatRaw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (reduceMotion) 1 else 3600, easing = LinearEasing), RepeatMode.Restart),
        label = "float"
    )
    val blinkRaw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (reduceMotion) 1 else 4800, easing = LinearEasing), RepeatMode.Restart),
        label = "blink"
    )
    val thinkRaw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(if (reduceMotion) 1 else 2200, easing = LinearEasing), RepeatMode.Restart),
        label = "think"
    )
    val talkRaw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (reduceMotion) 1 else 420, easing = LinearEasing), RepeatMode.Restart),
        label = "talk"
    )
    val speakShapeRaw by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (reduceMotion) 1 else 1680, easing = LinearEasing), RepeatMode.Restart),
        label = "speakShape"
    )
    val floatPhase = if (reduceMotion) 0f else floatRaw
    val blinkPhase = if (reduceMotion) 1f else blinkRaw
    val thinkSpin = if (reduceMotion) 0f else thinkRaw
    val talkPhase = if (reduceMotion || !isTalking) 0f else talkRaw
    val speakShape = if (reduceMotion || !isTalking) 0f else speakShapeRaw
    val liveTalk by animateFloatAsState(
        targetValue = if (isTalking && !reduceMotion) talkLevel.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(90),
        label = "talkLevel"
    )
    val bounce by animateFloatAsState(
        targetValue = when {
            reduceMotion -> 0f
            touchKind != LoomieTouch.None -> .85f
            mood == LoomieMood.Happy -> 1f
            isTalking -> .28f + liveTalk * .35f
            else -> 0f
        },
        animationSpec = tween(if (isTalking) 110 else 320),
        label = "happy"
    )
    val squish by animateFloatAsState(
        targetValue = if (touchKind == LoomieTouch.Hearts && !reduceMotion) 1f else 0f,
        animationSpec = tween(220),
        label = "squish"
    )
    val eyeScale = when {
        reduceMotion -> 1f
        blinkPhase in 0.86f..0.91f && !isTalking && touchKind == LoomieTouch.None -> 0.12f
        else -> 1f
    }
    val mouthOpen = if (!isTalking) 0f else {
        val chatter = sin(talkPhase * 2f * PI.toFloat()).let { (it + 1f) * .5f }
        val breath = sin(speakShape * 2f * PI.toFloat()).let { (it + 1f) * .5f }
        (.12f + chatter * .45f + breath * .2f + liveTalk * .45f).coerceIn(0f, 1f)
    }
    // Four mouth shapes only — no smile-with-center-dot.
    val mouthStyle = if (!isTalking) 0 else ((speakShape * 4f).toInt() % 4)
    val description = when {
        touchKind == LoomieTouch.Wink -> "Loomie winks"
        touchKind == LoomieTouch.Hearts -> "Loomie loves that"
        isTalking -> "Loomie speaking"
        mood == LoomieMood.Listening -> "Loomie listening"
        mood == LoomieMood.Thinking -> "Loomie thinking"
        mood == LoomieMood.Happy -> "Loomie happy"
        else -> "Loomie, a soft wool-ball companion, resting"
    }
    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { touch = 1 },
                    onDoubleTap = { touch = 2 }
                )
            }
    ) {
        val w = this.size.width
        val h = this.size.height
        val bob = sin(floatPhase * 2f * PI.toFloat()) * h * .022f - bounce * h * .03f
        val talkBob = if (isTalking) sin(talkPhase * 2f * PI.toFloat()) * h * .008f else 0f
        val center = Offset(w * .48f, h * .46f + bob + talkBob)
        val radius = w * .38f * (1f - squish * .06f)
        val sway = sin(floatPhase * 2f * PI.toFloat()) * radius * .06f

        drawContactShadow(center, radius)
        drawYarnCore(center, radius)
        drawBackWraps(center, radius, floatPhase)
        drawFaceWool(center, radius, floatPhase)
        drawFrontWraps(center, radius, floatPhase)
        drawCrownWool(center, radius, floatPhase)
        drawTrailingStrand(center, radius, sway)
        drawFeatures(
            center = center,
            radius = radius,
            eyeScale = eyeScale,
            mood = mood,
            talking = isTalking,
            mouthOpen = mouthOpen,
            mouthStyle = mouthStyle,
            touch = touchKind,
            canvasW = w
        )

        if (mood == LoomieMood.Thinking && !isTalking) {
            rotate(thinkSpin, center) {
                drawCircle(Sage.copy(alpha = .32f), radius * 1.28f, center, style = Stroke(width = w * .012f))
                drawCircle(SageDeep, w * .018f, Offset(center.x, center.y - radius * 1.28f))
                drawCircle(SageLite.copy(alpha = .85f), w * .008f, Offset(center.x - w * .004f, center.y - radius * 1.28f - w * .004f))
            }
        }
        if (mood == LoomieMood.Listening || isTalking) {
            val pulse = if (reduceMotion) .08f else sin(floatPhase * 2f * PI.toFloat()) * .05f + .1f + liveTalk * .08f
            drawCircle(Sage.copy(alpha = .16f + pulse), radius * 1.32f, center, style = Stroke(width = w * .013f))
        }
    }
}

private fun DrawScope.drawContactShadow(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0x33202924), Color.Transparent),
            center = Offset(center.x + radius * .08f, center.y + radius * 1.05f),
            radius = radius * .95f
        ),
        radius = radius * .95f,
        center = Offset(center.x + radius * .08f, center.y + radius * 1.05f)
    )
}

private fun DrawScope.drawYarnCore(center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(SageLite, SageCore, SageDeep),
            center = Offset(center.x - radius * .25f, center.y - radius * .3f),
            radius = radius * 1.25f
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.yarnTube(path: Path, thickness: Float, body: Color = Sage, shade: Color = SageDeep, highlight: Color = SageLite) {
    drawPath(path, shade, style = Stroke(width = thickness, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, body, style = Stroke(width = thickness * .72f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, highlight.copy(alpha = .55f), style = Stroke(width = thickness * .22f, cap = StrokeCap.Round))
}

private fun ellipseArc(
    center: Offset,
    rx: Float,
    ry: Float,
    startDeg: Float,
    sweepDeg: Float,
    tiltDeg: Float = 0f
): Path {
    val path = Path()
    val steps = 28
    val start = Math.toRadians(startDeg.toDouble())
    val sweep = Math.toRadians(sweepDeg.toDouble())
    val tilt = Math.toRadians(tiltDeg.toDouble())
    val cosT = cos(tilt)
    val sinT = sin(tilt)
    for (i in 0..steps) {
        val a = start + sweep * (i / steps.toDouble())
        val lx = cos(a) * rx
        val ly = sin(a) * ry
        val x = (center.x + lx * cosT - ly * sinT).toFloat()
        val y = (center.y + lx * sinT + ly * cosT).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    return path
}

private fun DrawScope.drawBackWraps(center: Offset, radius: Float, phase: Float) {
    val thickness = radius * .22f
    val wraps = listOf(
        0f to 1.02f,
        18f to .96f,
        -22f to .90f,
        40f to .84f,
        -48f to .78f,
        62f to .72f,
        -70f to .66f
    )
    wraps.forEachIndexed { index, (tilt, scale) ->
        val wobble = sin((phase + index * .11f) * 2f * PI.toFloat()) * 3f
        val path = ellipseArc(center, radius * scale, radius * scale * .92f, -20f + wobble, 220f, tilt)
        val body = if (index % 2 == 0) Sage else SageMid
        yarnTube(path, thickness * (1f - index * .03f), body, SageDeep, SageLite)
    }
    yarnTube(ellipseArc(center, radius * .98f, radius * .55f, 200f, 160f, 12f), thickness * .9f)
    yarnTube(ellipseArc(center, radius * .55f, radius * .98f, -40f, 160f, -8f), thickness * .85f, SageMid, SageDeep, SageLite)
}

private fun DrawScope.drawFrontWraps(center: Offset, radius: Float, phase: Float) {
    val thickness = radius * .2f
    val wobble = sin(phase * 2f * PI.toFloat()) * 2.5f
    yarnTube(ellipseArc(Offset(center.x, center.y - radius * .02f), radius * 1.02f, radius * .78f, 200f + wobble, 140f, 8f), thickness)
    yarnTube(ellipseArc(center, radius * .88f, radius * 1.0f, -30f, 95f, -18f), thickness * .95f, SageMid, SageDeep, SageLite)
    yarnTube(ellipseArc(center, radius * .88f, radius * 1.0f, 85f, 95f, 16f), thickness * .95f)
    yarnTube(ellipseArc(Offset(center.x, center.y + radius * .05f), radius * 1.05f, radius * .7f, 20f, 140f, -6f), thickness * .88f, SageMid, SageDeep, SageLite)
    yarnTube(ellipseArc(Offset(center.x - radius * .02f, center.y - radius * .08f), radius * .62f, radius * .48f, 150f, 80f, 25f), thickness * .7f)
    yarnTube(ellipseArc(Offset(center.x + radius * .02f, center.y - radius * .05f), radius * .6f, radius * .46f, -20f, 75f, -22f), thickness * .7f, SageMid, SageDeep, SageLite)
}

/** Thick stacked loops that seal the bald crown — drawn last before features. */
private fun DrawScope.drawCrownWool(center: Offset, radius: Float, phase: Float) {
    val thickness = radius * .24f
    val wobble = sin(phase * 2f * PI.toFloat()) * 2f
    val crownY = center.y - radius * .55f
    val rows = listOf(
        0.00f to .42f,
        0.10f to .55f,
        0.20f to .68f,
        0.30f to .78f,
        0.40f to .88f,
        0.50f to .96f,
        0.58f to 1.02f
    )
    rows.forEachIndexed { index, (drop, scale) ->
        val origin = Offset(center.x + if (index % 2 == 0) 0f else radius * .02f, crownY + radius * drop)
        val body = if (index % 2 == 0) Sage else SageMid
        yarnTube(
            ellipseArc(origin, radius * scale, radius * (.28f + drop * .12f), 185f + wobble, 170f, if (index % 2 == 0) 4f else -5f),
            thickness * (1.08f - index * .04f),
            body,
            SageDeep,
            SageLite
        )
    }
    yarnTube(
        ellipseArc(Offset(center.x - radius * .08f, center.y - radius * .62f), radius * .48f, radius * .36f, -50f + wobble, 200f, 32f),
        thickness * .95f,
        SageMid,
        SageDeep,
        SageLite
    )
    yarnTube(
        ellipseArc(Offset(center.x + radius * .1f, center.y - radius * .6f), radius * .46f, radius * .34f, 20f, 195f, -28f),
        thickness * .92f
    )
    yarnTube(
        ellipseArc(Offset(center.x, center.y - radius * .72f), radius * .38f, radius * .28f, 200f + wobble, 155f, 6f),
        thickness * .88f,
        SageMid,
        SageDeep,
        SageLite
    )
    yarnTube(
        ellipseArc(Offset(center.x, center.y - radius * .48f), radius * .7f, radius * .42f, 205f, 140f, -8f),
        thickness
    )
    yarnTube(
        ellipseArc(Offset(center.x - radius * .18f, center.y - radius * .5f), radius * .36f, radius * .4f, -100f, 160f, 18f),
        thickness * .8f,
        SageMid,
        SageDeep,
        SageLite
    )
    yarnTube(
        ellipseArc(Offset(center.x + radius * .2f, center.y - radius * .52f), radius * .34f, radius * .38f, -10f, 160f, -16f),
        thickness * .8f
    )
}

/** Dense green yarn covering the whole face/cheek area — no bare nest. */
private fun DrawScope.drawFaceWool(center: Offset, radius: Float, phase: Float) {
    val face = Offset(center.x - radius * .02f, center.y - radius * .02f)
    val thickness = radius * .17f
    val wobble = sin(phase * 2f * PI.toFloat()) * 2f

    drawOval(
        brush = Brush.radialGradient(
            listOf(SageCore, Sage, SageDeep),
            center = Offset(face.x - radius * .12f, face.y - radius * .18f),
            radius = radius * .95f
        ),
        topLeft = Offset(face.x - radius * .72f, face.y - radius * .74f),
        size = Size(radius * 1.44f, radius * 1.48f)
    )

    val packs = listOf(
        Triple(Offset(face.x, face.y - radius * .45f), Pair(radius * .55f, radius * .38f), Triple(200f + wobble, 150f, 0f)),
        Triple(Offset(face.x - radius * .12f, face.y - radius * .4f), Pair(radius * .4f, radius * .32f), Triple(-30f, 170f, 28f)),
        Triple(Offset(face.x + radius * .14f, face.y - radius * .42f), Pair(radius * .38f, radius * .3f), Triple(10f, 165f, -24f)),
        Triple(Offset(face.x, face.y), Pair(radius * .7f, radius * .62f), Triple(-25f, 230f, 6f)),
        Triple(Offset(face.x, face.y + radius * .04f), Pair(radius * .62f, radius * .55f), Triple(15f, 210f, -12f)),
        Triple(Offset(face.x, face.y - radius * .06f), Pair(radius * .58f, radius * .48f), Triple(-40f, 200f, 18f)),
        Triple(Offset(face.x - radius * .08f, face.y + radius * .1f), Pair(radius * .42f, radius * .36f), Triple(40f, 180f, -8f)),
        Triple(Offset(face.x + radius * .08f, face.y + radius * .1f), Pair(radius * .42f, radius * .36f), Triple(-50f, 180f, 10f)),
        Triple(Offset(face.x, face.y - radius * .02f), Pair(radius * .48f, radius * .52f), Triple(80f, 160f, 4f)),
        Triple(Offset(face.x, face.y + radius * .16f), Pair(radius * .55f, radius * .32f), Triple(10f + wobble, 160f, -4f)),
        Triple(Offset(face.x - radius * .18f, face.y), Pair(radius * .28f, radius * .42f), Triple(-90f, 150f, 22f)),
        Triple(Offset(face.x + radius * .18f, face.y), Pair(radius * .28f, radius * .42f), Triple(-20f, 150f, -22f)),
        Triple(Offset(face.x, face.y), Pair(radius * .36f, radius * .3f), Triple(-15f, 220f, 0f))
    )
    packs.forEachIndexed { index, (origin, radii, arc) ->
        val (rx, ry) = radii
        val (start, sweep, tilt) = arc
        val body = if (index % 2 == 0) Sage else SageMid
        yarnTube(
            ellipseArc(origin, rx, ry, start, sweep, tilt),
            thickness * (1f - index * .04f).coerceAtLeast(.55f),
            body,
            SageDeep,
            SageLite
        )
    }
}

/** Eyes + mouth drawn last so they sit clearly on the wool. */
private fun DrawScope.drawFeatures(
    center: Offset,
    radius: Float,
    eyeScale: Float,
    mood: LoomieMood,
    talking: Boolean,
    mouthOpen: Float,
    mouthStyle: Int,
    touch: LoomieTouch,
    canvasW: Float
) {
    val face = Offset(center.x - radius * .02f, center.y - radius * .02f)
    val faceRx = radius * .68f
    val faceRy = radius * .70f

    val eyeY = face.y - faceRy * .12f
    val eyeOffset = faceRx * .38f
    val eyeW = faceRx * .42f
    val eyeH = faceRy * .52f * eyeScale
    val look = when {
        talking -> faceRx * .02f
        mood == LoomieMood.Listening -> faceRx * .05f
        mood == LoomieMood.Thinking -> -faceRx * .04f
        else -> 0f
    }
    listOf(-1f, 1f).forEach { side ->
        val eyeCenter = Offset(face.x + side * eyeOffset + look, eyeY)
        val winkThis = touch == LoomieTouch.Wink && side < 0f
        drawCircle(SageDeep.copy(alpha = .28f), eyeW * .72f, eyeCenter)
        when {
            touch == LoomieTouch.Hearts -> {
                drawHeart(eyeCenter, eyeW * .55f, HeartPink)
            }
            winkThis -> {
                val wink = Path().apply {
                    moveTo(eyeCenter.x - eyeW * .45f, eyeCenter.y)
                    quadraticTo(eyeCenter.x, eyeCenter.y + eyeH * .35f, eyeCenter.x + eyeW * .45f, eyeCenter.y)
                }
                drawPath(wink, Ink, style = Stroke(width = canvasW * .02f, cap = StrokeCap.Round))
            }
            else -> {
                drawRoundRect(
                    color = Ink,
                    topLeft = Offset(eyeCenter.x - eyeW / 2f, eyeCenter.y - eyeH / 2f),
                    size = Size(eyeW, eyeH.coerceAtLeast(1f)),
                    cornerRadius = CornerRadius(eyeW, eyeW)
                )
                if (eyeScale > .35f) {
                    drawCircle(Color.White.copy(alpha = .95f), eyeW * .2f, Offset(eyeCenter.x - eyeW * .16f, eyeCenter.y - eyeH * .22f))
                    drawCircle(Color.White.copy(alpha = .4f), eyeW * .09f, Offset(eyeCenter.x + eyeW * .14f, eyeCenter.y + eyeH * .1f))
                }
            }
        }
    }

    val blushAlpha = if (touch == LoomieTouch.Hearts) .78f else .55f
    val blushSize = if (touch == LoomieTouch.Hearts) faceRx * .34f else faceRx * .28f
    drawCircle(Blush.copy(alpha = blushAlpha), blushSize, Offset(face.x - faceRx * .78f, face.y + faceRy * .28f))
    drawCircle(Blush.copy(alpha = blushAlpha), blushSize, Offset(face.x + faceRx * .78f, face.y + faceRy * .28f))

    val mouthY = face.y + faceRy * .48f
    when {
        talking || mood == LoomieMood.Speaking -> drawSpeakingMouth(face, faceRx, faceRy, mouthY, mouthOpen, mouthStyle, canvasW)
        touch == LoomieTouch.Hearts || touch == LoomieTouch.Wink || mood == LoomieMood.Happy -> {
            val smile = Path().apply {
                moveTo(face.x - faceRx * .42f, mouthY)
                quadraticTo(face.x, mouthY + faceRy * .42f, face.x + faceRx * .42f, mouthY)
            }
            drawPath(smile, Ink, style = Stroke(width = canvasW * .026f, cap = StrokeCap.Round))
        }
        mood == LoomieMood.Thinking -> drawCircle(Ink, canvasW * .016f, Offset(face.x, mouthY))
        mood == LoomieMood.Listening -> drawRoundRect(
            Ink,
            Offset(face.x - faceRx * .24f, mouthY - faceRy * .06f),
            Size(faceRx * .48f, faceRy * .14f),
            CornerRadius(faceRx * .08f)
        )
        else -> {
            val smile = Path().apply {
                moveTo(face.x - faceRx * .34f, mouthY)
                quadraticTo(face.x, mouthY + faceRy * .28f, face.x + faceRx * .34f, mouthY)
            }
            drawPath(smile, Ink, style = Stroke(width = canvasW * .022f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawSpeakingMouth(
    face: Offset,
    faceRx: Float,
    faceRy: Float,
    mouthY: Float,
    mouthOpen: Float,
    mouthStyle: Int,
    canvasW: Float
) {
    when (mouthStyle) {
        0 -> {
            // Soft open oval
            val openH = faceRy * (.08f + mouthOpen * .32f)
            val openW = faceRx * (.28f + mouthOpen * .16f)
            drawOval(Ink, Offset(face.x - openW / 2f, mouthY - openH / 2f), Size(openW, openH))
        }
        1 -> {
            // Small open "o" ring (no filled center dot)
            val r = faceRx * (.1f + mouthOpen * .14f)
            drawCircle(Ink, r, Offset(face.x, mouthY), style = Stroke(width = canvasW * .018f, cap = StrokeCap.Round))
        }
        2 -> {
            // Flat "ahh" oval
            val openH = faceRy * (.06f + mouthOpen * .22f)
            val openW = faceRx * (.4f + mouthOpen * .18f)
            drawOval(Ink, Offset(face.x - openW / 2f, mouthY - openH / 2f), Size(openW, openH))
        }
        else -> {
            // Soft closed-ish chat curve (no center fill)
            val smile = Path().apply {
                moveTo(face.x - faceRx * (.22f + mouthOpen * .06f), mouthY)
                quadraticTo(
                    face.x,
                    mouthY + faceRy * (.1f + mouthOpen * .2f),
                    face.x + faceRx * (.22f + mouthOpen * .06f),
                    mouthY
                )
            }
            drawPath(smile, Ink, style = Stroke(width = canvasW * .02f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y + size * .42f)
        cubicTo(
            center.x - size * .9f, center.y + size * .05f,
            center.x - size * .7f, center.y - size * .55f,
            center.x, center.y - size * .18f
        )
        cubicTo(
            center.x + size * .7f, center.y - size * .55f,
            center.x + size * .9f, center.y + size * .05f,
            center.x, center.y + size * .42f
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawTrailingStrand(center: Offset, radius: Float, sway: Float) {
    val tip = Offset(center.x + radius * 1.15f + sway, center.y + radius * 1.35f)
    val path = Path().apply {
        moveTo(center.x + radius * .55f, center.y + radius * .55f)
        cubicTo(
            center.x + radius * 1.05f, center.y + radius * .75f,
            center.x + radius * 1.35f + sway * .5f, center.y + radius * 1.05f,
            tip.x, tip.y
        )
    }
    val thickness = radius * .2f
    yarnTube(path, thickness)
    drawCircle(SageDeep, thickness * .42f, tip)
    drawCircle(SageLite.copy(alpha = .7f), thickness * .22f, Offset(tip.x - thickness * .08f, tip.y - thickness * .1f))
}
