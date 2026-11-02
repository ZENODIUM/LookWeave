package com.vizeno.loomie.ui

import android.graphics.Bitmap
import android.graphics.PointF
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

enum class BodyRegion(val label: String) {
    NECK("Neck"),
    SHOULDERS("Shoulders"),
    CHEST("Chest"),
    ARMS("Arms"),
    WAIST("Waist"),
    LEGS("Legs"),
    FEET("Feet")
}

data class RegionHotspot(
    val region: BodyRegion,
    /** Normalized image coordinates 0..1 (content-fit space expects image-relative). */
    val x: Float,
    val y: Float,
    /** Soft ring size — keep small so overlays never cover the face. */
    val radiusFraction: Float = 0.028f
)

/**
 * Pose overlay result.
 * - [hotspots] empty → UI should show labeled region chips under the image (no garbage blobs).
 * - [fullBody] false → upper-body / failed pose; prefer chips over speculative overlays.
 */
data class PoseRegionResult(
    val hotspots: List<RegionHotspot>,
    val fullBody: Boolean,
    val useOverlay: Boolean
)

private const val MIN_LANDMARK_LIKELIHOOD = 0.55f
private const val MIN_BODY_SPAN = 0.42f

/**
 * ML Kit pose landmarks → body-region hotspots.
 * Bilateral regions (shoulders / arms / legs / feet) keep left and right rings so
 * markers sit off the torso midline instead of collapsing into a vertical stack.
 */
internal fun detectBodyRegionHotspots(bitmap: Bitmap): List<RegionHotspot> =
    detectPoseRegions(bitmap).hotspots

internal fun detectPoseRegions(bitmap: Bitmap): PoseRegionResult {
    val fromPose = runCatching { hotspotsFromPose(bitmap) }.getOrNull()
    if (fromPose != null) return fromPose
    return PoseRegionResult(hotspots = emptyList(), fullBody = false, useOverlay = false)
}

private fun hotspotsFromPose(bitmap: Bitmap): PoseRegionResult? {
    val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()
    val detector = PoseDetection.getClient(options)
    return try {
        val pose = Tasks.await(detector.process(InputImage.fromBitmap(bitmap, 0)))
        val w = bitmap.width.toFloat().coerceAtLeast(1f)
        val h = bitmap.height.toFloat().coerceAtLeast(1f)

        data class Lm(val point: PointF, val likelihood: Float)

        fun lm(type: Int): Lm? {
            val landmark = pose.getPoseLandmark(type) ?: return null
            if (landmark.inFrameLikelihood < MIN_LANDMARK_LIKELIHOOD) return null
            return Lm(PointF(landmark.position.x, landmark.position.y), landmark.inFrameLikelihood)
        }

        val nose = lm(PoseLandmark.NOSE)
        val leftShoulder = lm(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = lm(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = lm(PoseLandmark.LEFT_ELBOW)
        val rightElbow = lm(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = lm(PoseLandmark.LEFT_WRIST)
        val rightWrist = lm(PoseLandmark.RIGHT_WRIST)
        val leftHip = lm(PoseLandmark.LEFT_HIP)
        val rightHip = lm(PoseLandmark.RIGHT_HIP)
        val leftKnee = lm(PoseLandmark.LEFT_KNEE)
        val rightKnee = lm(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = lm(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = lm(PoseLandmark.RIGHT_ANKLE)

        val shoulderCount = listOfNotNull(leftShoulder, rightShoulder).size
        if (shoulderCount == 0 && leftHip == null && rightHip == null) {
            return PoseRegionResult(emptyList(), fullBody = false, useOverlay = false)
        }

        fun mid(a: Lm?, b: Lm?): PointF? = when {
            a != null && b != null -> PointF((a.point.x + b.point.x) / 2f, (a.point.y + b.point.y) / 2f)
            a != null -> a.point
            b != null -> b.point
            else -> null
        }

        fun avgPoint(vararg points: Lm?): PointF? {
            val ok = points.filterNotNull()
            if (ok.isEmpty()) return null
            return PointF(
                ok.map { it.point.x }.average().toFloat(),
                ok.map { it.point.y }.average().toFloat()
            )
        }

        val shoulderMid = mid(leftShoulder, rightShoulder)
        val hipMid = mid(leftHip, rightHip)
        val kneeMid = mid(leftKnee, rightKnee)
        val ankleMid = mid(leftAnkle, rightAnkle)

        // Vertical span of detected body — face/selfie crops collapse this and produce junk overlays.
        val ys = listOfNotNull(
            nose?.point?.y,
            leftShoulder?.point?.y,
            rightShoulder?.point?.y,
            hipMid?.y,
            kneeMid?.y,
            ankleMid?.y,
            leftElbow?.point?.y,
            rightElbow?.point?.y
        )
        if (ys.size < 2) {
            return PoseRegionResult(emptyList(), fullBody = false, useOverlay = false)
        }
        val bodySpan = (ys.maxOrNull()!! - ys.minOrNull()!!) / h
        val hasLowerBody = (leftHip != null || rightHip != null) &&
            (leftKnee != null || rightKnee != null || leftAnkle != null || rightAnkle != null)
        val fullBody = bodySpan >= MIN_BODY_SPAN && hasLowerBody

        // Upper-body / face-dominant frames: never paint speculative blobs on the face.
        if (!fullBody) {
            return PoseRegionResult(emptyList(), fullBody = false, useOverlay = false)
        }

        fun nx(x: Float) = (x / w).coerceIn(0.04f, 0.96f)
        fun ny(y: Float) = (y / h).coerceIn(0.04f, 0.96f)

        fun belowFace(y: Float): Boolean {
            val faceFloor = nose?.point?.y?.let { it + h * 0.04f }
                ?: shoulderMid?.y?.let { it - h * 0.02f }
                ?: (h * 0.18f)
            return y >= faceFloor
        }

        val spots = mutableListOf<RegionHotspot>()

        fun add(region: BodyRegion, point: PointF?, radius: Float = 0.028f) {
            if (point == null) return
            if (!belowFace(point.y) && region != BodyRegion.NECK) return
            spots += RegionHotspot(region, nx(point.x), ny(point.y), radius)
        }

        // Neck: between nose and shoulder midline — top-center on a standing full-body.
        run {
            val neck = when {
                nose != null && shoulderMid != null -> {
                    val y = nose.point.y * 0.35f + shoulderMid.y * 0.65f
                    PointF(shoulderMid.x, y)
                }
                shoulderMid != null -> PointF(shoulderMid.x, shoulderMid.y - hypot(w, h) * 0.025f)
                else -> null
            }
            if (neck != null && belowFace(neck.y) && abs(neck.y - (nose?.point?.y ?: -1f)) > h * 0.03f) {
                spots += RegionHotspot(BodyRegion.NECK, nx(neck.x), ny(neck.y), 0.024f)
            }
        }

        // Shoulders: left AND right of center (never collapse to midline).
        add(BodyRegion.SHOULDERS, leftShoulder?.point, 0.03f)
        add(BodyRegion.SHOULDERS, rightShoulder?.point, 0.03f)

        // Chest: mid-torso between shoulders and hips.
        run {
            val chest = when {
                shoulderMid != null && hipMid != null ->
                    PointF((shoulderMid.x + hipMid.x) / 2f, shoulderMid.y * 0.45f + hipMid.y * 0.55f)
                shoulderMid != null -> PointF(shoulderMid.x, shoulderMid.y + h * 0.07f)
                else -> null
            }
            add(BodyRegion.CHEST, chest, 0.03f)
        }

        // Arms: elbows (fallback wrist) — keep each side off the midline.
        add(BodyRegion.ARMS, avgPoint(leftElbow, leftWrist) ?: leftElbow?.point, 0.026f)
        add(BodyRegion.ARMS, avgPoint(rightElbow, rightWrist) ?: rightElbow?.point, 0.026f)

        // Waist: hip midline.
        add(BodyRegion.WAIST, hipMid, 0.03f)

        // Legs / feet: left and right landmarks so they sit under each limb.
        if (fullBody) {
            add(BodyRegion.LEGS, leftKnee?.point ?: avgPoint(leftHip, leftAnkle), 0.028f)
            add(BodyRegion.LEGS, rightKnee?.point ?: avgPoint(rightHip, rightAnkle), 0.028f)
            leftAnkle?.let { a ->
                spots += RegionHotspot(
                    BodyRegion.FEET,
                    nx(a.point.x),
                    ny(min(h * 0.96f, a.point.y)),
                    0.024f
                )
            }
            rightAnkle?.let { a ->
                spots += RegionHotspot(
                    BodyRegion.FEET,
                    nx(a.point.x),
                    ny(min(h * 0.96f, a.point.y)),
                    0.024f
                )
            }
            if (spots.none { it.region == BodyRegion.FEET }) {
                add(BodyRegion.FEET, ankleMid?.let { PointF(it.x, min(h * 0.96f, it.y)) }, 0.024f)
            }
        }

        val regionsPresent = spots.map { it.region }.toSet()
        val usable = regionsPresent.size >= 2 &&
            spots.none { it.region == BodyRegion.NECK && it.y < 0.10f }
        PoseRegionResult(
            hotspots = if (usable) spots else emptyList(),
            fullBody = fullBody,
            useOverlay = usable
        )
    } finally {
        detector.close()
    }
}

internal fun regionSuggestedGarmentCategory(region: BodyRegion): String = when (region) {
    BodyRegion.NECK, BodyRegion.SHOULDERS, BodyRegion.CHEST, BodyRegion.ARMS -> "TOP"
    BodyRegion.WAIST -> "BOTTOM"
    BodyRegion.LEGS -> "BOTTOM"
    BodyRegion.FEET -> "SHOES"
}
