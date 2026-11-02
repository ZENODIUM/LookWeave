package com.vizeno.loomie.ui

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import android.media.FaceDetector
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Local mannequin:
 * - ML Kit landmarks locate eyes, brows, nose and mouth.
 * - Those feature pixels are erased.
 * - Holes are filled from neighbouring cheek/forehead skin pixels (inpainting),
 *   so the face becomes continuous skin without a drawn oval/circle.
 * - Hair and ear bands are left alone. Processing stays on-device.
 */
internal fun createLocalMannequin(source: Bitmap): Bitmap? {
    val working = source.copy(Bitmap.Config.ARGB_8888, true)
    val face = detectWithMlKit(working) ?: detectWithLegacy(working) ?: return null
    return inpaintFaceFeatures(working, face)
}

private data class FaceGeometry(
    val bounds: Rect,
    val eyeDistance: Float,
    val leftEye: PointF?,
    val rightEye: PointF?,
    val nose: PointF?,
    val mouth: PointF?,
    val faceOval: List<PointF>
)

private fun detectWithMlKit(bitmap: Bitmap): FaceGeometry? = runCatching {
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setMinFaceSize(0.12f)
        .build()
    val detector = FaceDetection.getClient(options)
    try {
        val faces = Tasks.await(detector.process(InputImage.fromBitmap(bitmap, 0)))
        val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() } ?: return@runCatching null
        geometryFromMlKit(face)
    } finally {
        detector.close()
    }
}.getOrNull()

private fun geometryFromMlKit(face: Face): FaceGeometry {
    val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
    val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
    val nose = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
    val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position
    val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position
    val mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position
    val mouth = when {
        mouthLeft != null && mouthRight != null && mouthBottom != null ->
            PointF((mouthLeft.x + mouthRight.x) * .5f, (mouthLeft.y + mouthRight.y + mouthBottom.y) / 3f)
        mouthBottom != null -> mouthBottom
        mouthLeft != null && mouthRight != null -> PointF((mouthLeft.x + mouthRight.x) * .5f, (mouthLeft.y + mouthRight.y) * .5f)
        else -> null
    }
    val eyeDistance = if (leftEye != null && rightEye != null) {
        hypot(rightEye.x - leftEye.x, rightEye.y - leftEye.y)
    } else face.boundingBox.width() * .28f
    return FaceGeometry(
        bounds = face.boundingBox,
        eyeDistance = eyeDistance.coerceAtLeast(18f),
        leftEye = leftEye,
        rightEye = rightEye,
        nose = nose,
        mouth = mouth,
        faceOval = face.getContour(FaceContour.FACE)?.points.orEmpty()
    )
}

private fun detectWithLegacy(source: Bitmap): FaceGeometry? {
    val scales = linkedSetOf(1f)
    val longest = max(source.width, source.height).toFloat()
    for (target in listOf(720f, 512f, 384f, 256f)) if (longest > target) scales += target / longest
    var best: FaceGeometry? = null
    var bestEyes = 0f
    for (scale in scales) {
        val probe = if (scale == 1f) source.copy(Bitmap.Config.RGB_565, false)
        else {
            val w = (source.width * scale).toInt().coerceAtLeast(64)
            val h = (source.height * scale).toInt().coerceAtLeast(64)
            Bitmap.createScaledBitmap(source, w, h, true).copy(Bitmap.Config.RGB_565, false)
        }
        val faces = arrayOfNulls<FaceDetector.Face>(1)
        if (FaceDetector(probe.width, probe.height, 1).findFaces(probe, faces) == 0 || faces[0] == null) continue
        val mid = PointF()
        faces[0]!!.getMidPoint(mid)
        val eyes = (faces[0]!!.eyesDistance() / scale).coerceAtLeast(18f)
        val mx = mid.x / scale
        val my = mid.y / scale
        if (eyes <= bestEyes) continue
        bestEyes = eyes
        best = FaceGeometry(
            bounds = Rect(
                (mx - eyes * 1.35f).roundToInt(), (my - eyes * 1.35f).roundToInt(),
                (mx + eyes * 1.35f).roundToInt(), (my + eyes * 1.85f).roundToInt()
            ),
            eyeDistance = eyes,
            leftEye = PointF(mx - eyes * .5f, my),
            rightEye = PointF(mx + eyes * .5f, my),
            nose = PointF(mx, my + eyes * .45f),
            mouth = PointF(mx, my + eyes * .95f),
            faceOval = emptyList()
        )
    }
    return best
}

private fun inpaintFaceFeatures(bitmap: Bitmap, face: FaceGeometry): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val pad = (face.eyeDistance * .55f).toInt().coerceAtLeast(12)
    val x0 = (face.bounds.left - pad).coerceAtLeast(0)
    val y0 = (face.bounds.top - pad).coerceAtLeast(0)
    val x1 = (face.bounds.right + pad).coerceAtMost(width - 1)
    val y1 = (face.bounds.bottom + pad).coerceAtMost(height - 1)
    val roiW = x1 - x0 + 1
    val roiH = y1 - y0 + 1
    if (roiW < 8 || roiH < 8) return bitmap

    val pixels = IntArray(roiW * roiH)
    bitmap.getPixels(pixels, 0, roiW, x0, y0, roiW, roiH)

    val eyes = face.eyeDistance
    val midX = ((face.leftEye?.x ?: face.bounds.exactCenterX()) + (face.rightEye?.x ?: face.bounds.exactCenterX())) * .5f
    val midY = ((face.leftEye?.y ?: face.bounds.exactCenterY()) + (face.rightEye?.y ?: face.bounds.exactCenterY())) * .5f
    val browLineY = midY - eyes * .42f - y0

    val cheekSamples = mutableListOf<Int>()
    collectPatch(pixels, roiW, roiH, midX - eyes * .62f - x0, midY + eyes * .38f - y0, eyes * .18f, cheekSamples)
    collectPatch(pixels, roiW, roiH, midX + eyes * .62f - x0, midY + eyes * .38f - y0, eyes * .18f, cheekSamples)
    collectPatch(pixels, roiW, roiH, midX - x0, midY - eyes * .45f - y0, eyes * .12f, cheekSamples)
    val skin = medianColor(cheekSamples.filter { luminance(it) in 45f..235f }.ifEmpty { cheekSamples })
    val skinYuv = yuv(skin)

    val interiorPoints = if (face.faceOval.size >= 8) {
        face.faceOval.map { PointF(it.x - x0, it.y - y0) }
    } else landmarkHull(face, x0, y0)
    val interior = polygonMask(interiorPoints, roiW, roiH, shrink = 1)
    val minX = interiorPoints.minOf { it.x }
    val maxX = interiorPoints.maxOf { it.x }
    val faceWidth = (maxX - minX).coerceAtLeast(1f)

    // Precise feature holes from landmarks only (eyes, brows, nose, mouth).
    val hole = BooleanArray(roiW * roiH)
    stampFeature(hole, roiW, roiH, x0, y0, face.leftEye, eyes * .38f, eyes * .3f)
    stampFeature(hole, roiW, roiH, x0, y0, face.rightEye, eyes * .38f, eyes * .3f)
    face.leftEye?.let { stampFeature(hole, roiW, roiH, x0, y0, PointF(it.x, it.y - eyes * .3f), eyes * .42f, eyes * .18f) }
    face.rightEye?.let { stampFeature(hole, roiW, roiH, x0, y0, PointF(it.x, it.y - eyes * .3f), eyes * .42f, eyes * .18f) }
    stampFeature(hole, roiW, roiH, x0, y0, face.nose ?: PointF(midX, midY + eyes * .4f), eyes * .28f, eyes * .52f)
    stampFeature(hole, roiW, roiH, x0, y0, face.mouth ?: PointF(midX, midY + eyes * .95f), eyes * .5f, eyes * .32f)
    // Clip holes to the face interior and out of hair/ears.
    for (i in hole.indices) {
        val x = i % roiW
        val y = i / roiW
        if (!interior[i]) { hole[i] = false; continue }
        val lateral = abs(x - (minX + maxX) * .5f) / faceWidth
        if (lateral > .44f) hole[i] = false
        if (y < browLineY && isHairLike(pixels[i], skinYuv)) hole[i] = false
    }

    val protect = BooleanArray(roiW * roiH) // hair + ears — never rewrite
    for (i in protect.indices) {
        val x = i % roiW
        val y = i / roiW
        val lateral = abs(x - (minX + maxX) * .5f) / faceWidth
        if (lateral > .44f) protect[i] = true
        if (y < browLineY && isHairLike(pixels[i], skinYuv)) protect[i] = true
        if (!interior[i]) protect[i] = true
    }

    val known = BooleanArray(roiW * roiH)
    val out = pixels.copyOf()
    for (i in out.indices) {
        if (protect[i] || hole[i]) continue
        if (interior[i]) known[i] = true
    }

    // Inpaint landmark holes from adjacent skin pixels.
    val radius = (eyes * .28f).toInt().coerceIn(6, 22)
    var remaining = hole.count { it }
    var guard = 0
    while (remaining > 0 && guard < 16) {
        guard++
        val newly = BooleanArray(roiW * roiH)
        var filledThisPass = 0
        for (y in 0 until roiH) for (x in 0 until roiW) {
            val i = y * roiW + x
            if (!hole[i]) continue
            var rSum = 0f
            var gSum = 0f
            var bSum = 0f
            var wSum = 0f
            for (dy in -radius..radius) for (dx in -radius..radius) {
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0 until roiW || ny !in 0 until roiH) continue
                val ni = ny * roiW + nx
                if (!known[ni]) continue
                val dist = sqrt((dx * dx + dy * dy).toFloat()).coerceAtLeast(1f)
                val w = 1f / (dist * dist)
                val c = out[ni]
                rSum += android.graphics.Color.red(c) * w
                gSum += android.graphics.Color.green(c) * w
                bSum += android.graphics.Color.blue(c) * w
                wSum += w
            }
            out[i] = if (wSum < 0.5f) withAlpha(skin, pixels[i]) else android.graphics.Color.argb(
                android.graphics.Color.alpha(pixels[i]),
                (rSum / wSum).roundToInt().coerceIn(0, 255),
                (gSum / wSum).roundToInt().coerceIn(0, 255),
                (bSum / wSum).roundToInt().coerceIn(0, 255)
            )
            newly[i] = true
            filledThisPass++
        }
        if (filledThisPass == 0) break
        for (i in newly.indices) if (newly[i]) {
            hole[i] = false
            known[i] = true
        }
        remaining -= filledThisPass
    }
    for (i in hole.indices) if (hole[i]) {
        out[i] = withAlpha(skin, pixels[i])
        known[i] = true
    }

    // Smooth only inside the face so inpainted features melt into continuous skin
    // (cheeks keep lighting; no flat oval stamp).
    val blurR = (eyes * .18f).toInt().coerceIn(4, 14)
    val smoothed = maskedBoxBlur(out, known, roiW, roiH, blurR)
    for (i in out.indices) if (known[i]) out[i] = mixColor(out[i], smoothed[i], .85f)

    bitmap.setPixels(out, 0, roiW, x0, y0, roiW, roiH)
    return bitmap
}

/** Box blur that only samples/writes pixels inside [mask]. */
private fun maskedBoxBlur(source: IntArray, mask: BooleanArray, width: Int, height: Int, radius: Int): IntArray {
    if (radius <= 0) return source.copyOf()
    val out = source.copyOf()
    for (y in 0 until height) for (x in 0 until width) {
        val i = y * width + x
        if (!mask[i]) continue
        var rSum = 0; var gSum = 0; var bSum = 0; var count = 0
        for (dy in -radius..radius) for (dx in -radius..radius) {
            val nx = x + dx
            val ny = y + dy
            if (nx !in 0 until width || ny !in 0 until height) continue
            val ni = ny * width + nx
            if (!mask[ni]) continue
            val c = source[ni]
            rSum += android.graphics.Color.red(c)
            gSum += android.graphics.Color.green(c)
            bSum += android.graphics.Color.blue(c)
            count++
        }
        if (count > 0) {
            out[i] = android.graphics.Color.argb(
                android.graphics.Color.alpha(source[i]),
                rSum / count, gSum / count, bSum / count
            )
        }
    }
    return out
}

private fun landmarkHull(face: FaceGeometry, x0: Int, y0: Int): List<PointF> {
    val eyes = face.eyeDistance
    val midX = ((face.leftEye?.x ?: face.bounds.exactCenterX()) + (face.rightEye?.x ?: face.bounds.exactCenterX())) * .5f
    val midY = ((face.leftEye?.y ?: face.bounds.exactCenterY()) + (face.rightEye?.y ?: face.bounds.exactCenterY())) * .5f
    val points = mutableListOf<PointF>()
    fun add(p: PointF?, dx: Float = 0f, dy: Float = 0f) {
        if (p != null) points += PointF(p.x - x0 + dx, p.y - y0 + dy)
    }
    add(face.leftEye, -eyes * .55f, -eyes * .55f)
    add(face.leftEye, -eyes * .75f, eyes * .15f)
    add(face.leftEye, -eyes * .35f, eyes * .9f)
    add(face.mouth, -eyes * .55f, eyes * .35f)
    add(face.mouth, 0f, eyes * .55f)
    add(face.mouth, eyes * .55f, eyes * .35f)
    add(face.rightEye, eyes * .35f, eyes * .9f)
    add(face.rightEye, eyes * .75f, eyes * .15f)
    add(face.rightEye, eyes * .55f, -eyes * .55f)
    add(PointF(midX, midY), 0f, -eyes * 1.05f)
    if (points.size < 6) {
        points += PointF(midX - x0, midY - y0 - eyes * 1.1f)
        points += PointF(midX - x0 - eyes * 1.1f, midY - y0)
        points += PointF(midX - x0, midY - y0 + eyes * 1.4f)
        points += PointF(midX - x0 + eyes * 1.1f, midY - y0)
    }
    return convexHull(points)
}

private fun convexHull(points: List<PointF>): List<PointF> {
    val sorted = points.distinctBy { "${it.x.toInt()}:${it.y.toInt()}" }.sortedWith(compareBy({ it.x }, { it.y }))
    if (sorted.size <= 3) return sorted
    fun cross(o: PointF, a: PointF, b: PointF) = (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
    val lower = mutableListOf<PointF>()
    for (p in sorted) {
        while (lower.size >= 2 && cross(lower[lower.size - 2], lower.last(), p) <= 0) lower.removeAt(lower.lastIndex)
        lower += p
    }
    val upper = mutableListOf<PointF>()
    for (p in sorted.asReversed()) {
        while (upper.size >= 2 && cross(upper[upper.size - 2], upper.last(), p) <= 0) upper.removeAt(upper.lastIndex)
        upper += p
    }
    return lower.dropLast(1) + upper.dropLast(1)
}

private fun collectPatch(
    pixels: IntArray, width: Int, height: Int, fx: Float, fy: Float, radius: Float, out: MutableList<Int>
) {
    val r = radius.roundToInt().coerceAtLeast(2)
    val cx = fx.roundToInt()
    val cy = fy.roundToInt()
    for (dy in -r..r) for (dx in -r..r) {
        if (dx * dx + dy * dy > r * r) continue
        val x = (cx + dx).coerceIn(0, width - 1)
        val y = (cy + dy).coerceIn(0, height - 1)
        out += pixels[y * width + x]
    }
}

private fun stampFeature(
    mask: BooleanArray, width: Int, height: Int, x0: Int, y0: Int,
    center: PointF?, halfW: Float, halfH: Float
) {
    if (center == null) return
    val left = (center.x - halfW - x0).roundToInt()
    val right = (center.x + halfW - x0).roundToInt()
    val top = (center.y - halfH - y0).roundToInt()
    val bottom = (center.y + halfH - y0).roundToInt()
    for (y in top..bottom) {
        if (y !in 0 until height) continue
        for (x in left..right) {
            if (x !in 0 until width) continue
            val nx = (x - (center.x - x0)) / halfW
            val ny = (y - (center.y - y0)) / halfH
            if (nx * nx + ny * ny <= 1.02f) mask[y * width + x] = true
        }
    }
}

private fun polygonMask(points: List<PointF>, width: Int, height: Int, shrink: Int): BooleanArray {
    val mask = BooleanArray(width * height)
    if (points.size < 3) return mask
    val xs = FloatArray(points.size) { points[it].x }
    val ys = FloatArray(points.size) { points[it].y }
    val minX = xs.min().toInt().coerceAtLeast(0)
    val maxX = xs.max().toInt().coerceAtMost(width - 1)
    val minY = ys.min().toInt().coerceAtLeast(0)
    val maxY = ys.max().toInt().coerceAtMost(height - 1)
    for (y in minY..maxY) for (x in minX..maxX) {
        if (pointInPolygon(x + .5f, y + .5f, xs, ys)) mask[y * width + x] = true
    }
    var current = mask
    repeat(shrink.coerceAtLeast(0)) {
        val next = BooleanArray(current.size)
        for (y in 1 until height - 1) for (x in 1 until width - 1) {
            val i = y * width + x
            if (current[i] && current[i - 1] && current[i + 1] && current[i - width] && current[i + width]) next[i] = true
        }
        current = next
    }
    return current
}

private fun pointInPolygon(x: Float, y: Float, xs: FloatArray, ys: FloatArray): Boolean {
    var inside = false
    var j = xs.size - 1
    for (i in xs.indices) {
        val yi = ys[i]; val yj = ys[j]; val xi = xs[i]; val xj = xs[j]
        val denom = (yj - yi).takeIf { it != 0f } ?: 1f
        val intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / denom + xi)
        if (intersect) inside = !inside
        j = i
    }
    return inside
}

private fun isSkinLike(color: Int, skin: FloatArray): Boolean = colorDistance(yuv(color), skin) < 38f

private fun isHairLike(color: Int, skin: FloatArray): Boolean {
    val y = luminance(color)
    return y < skin[0] - 32f || y < 38f
}

private fun luminance(color: Int): Float {
    val r = android.graphics.Color.red(color).toFloat()
    val g = android.graphics.Color.green(color).toFloat()
    val b = android.graphics.Color.blue(color).toFloat()
    return 0.299f * r + 0.587f * g + 0.114f * b
}

private fun yuv(color: Int): FloatArray {
    val r = android.graphics.Color.red(color).toFloat()
    val g = android.graphics.Color.green(color).toFloat()
    val b = android.graphics.Color.blue(color).toFloat()
    return floatArrayOf(
        0.299f * r + 0.587f * g + 0.114f * b,
        -0.14713f * r - 0.28886f * g + 0.436f * b,
        0.615f * r - 0.51499f * g - 0.10001f * b
    )
}

private fun colorDistance(a: FloatArray, b: FloatArray): Float {
    val dy = a[0] - b[0]
    val du = (a[1] - b[1]) * 1.55f
    val dv = (a[2] - b[2]) * 1.55f
    return sqrt(dy * dy + du * du + dv * dv)
}

private fun medianColor(samples: List<Int>): Int {
    if (samples.isEmpty()) return android.graphics.Color.rgb(180, 140, 120)
    val rs = samples.map { android.graphics.Color.red(it) }.sorted()
    val gs = samples.map { android.graphics.Color.green(it) }.sorted()
    val bs = samples.map { android.graphics.Color.blue(it) }.sorted()
    val mid = samples.size / 2
    return android.graphics.Color.rgb(rs[mid], gs[mid], bs[mid])
}

private fun withAlpha(color: Int, alphaSource: Int): Int = android.graphics.Color.argb(
    android.graphics.Color.alpha(alphaSource),
    android.graphics.Color.red(color),
    android.graphics.Color.green(color),
    android.graphics.Color.blue(color)
)

private fun mixColor(from: Int, to: Int, t: Float): Int {
    val u = t.coerceIn(0f, 1f)
    return android.graphics.Color.argb(
        android.graphics.Color.alpha(from),
        (android.graphics.Color.red(from) + (android.graphics.Color.red(to) - android.graphics.Color.red(from)) * u).roundToInt(),
        (android.graphics.Color.green(from) + (android.graphics.Color.green(to) - android.graphics.Color.green(from)) * u).roundToInt(),
        (android.graphics.Color.blue(from) + (android.graphics.Color.blue(to) - android.graphics.Color.blue(from)) * u).roundToInt()
    )
}
