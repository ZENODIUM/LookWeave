package com.vizeno.loomie.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vizeno.loomie.R
import com.vizeno.loomie.model.Garment
import com.vizeno.loomie.ui.theme.Olive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Try-on source: remote/local [Garment.imageUrl], else bundled sample drawable. */
internal fun garmentTryOnUri(packageName: String, garment: Garment): Uri? {
    val url = garment.imageUrl?.takeIf { it.isNotBlank() }
    if (url != null) return Uri.parse(url)
    val res = garmentDrawableRes(garment.id) ?: return null
    return Uri.parse("android.resource://$packageName/$res")
}

/** Sample capsule wardrobe drawables (g1–g12 + remapped variants g13–g22 + persona p*_**). */
internal fun garmentDrawableRes(garmentId: String): Int? = when (garmentId) {
    "g1", "g13", "g21" -> R.drawable.garment_g1
    "g2", "g15" -> R.drawable.garment_g2
    "g3", "g17", "g19" -> R.drawable.garment_g3
    "g4", "g20" -> R.drawable.garment_g4
    "g5", "g18" -> R.drawable.garment_g5
    "g6" -> R.drawable.garment_g6
    "g7", "g14" -> R.drawable.garment_g7
    "g8", "g16" -> R.drawable.garment_g8
    "g9", "g22" -> R.drawable.garment_g9
    "g10" -> R.drawable.garment_g10
    "g11" -> R.drawable.garment_g11
    "g12" -> R.drawable.garment_g12
    "p1_01" -> R.drawable.garment_p1_01
    "p1_02" -> R.drawable.garment_p1_02
    "p1_03" -> R.drawable.garment_p1_03
    "p1_04" -> R.drawable.garment_p1_04
    "p1_05" -> R.drawable.garment_p1_05
    "p1_06" -> R.drawable.garment_p1_06
    "p1_07" -> R.drawable.garment_p1_07
    "p1_08" -> R.drawable.garment_p1_08
    "p1_09" -> R.drawable.garment_p1_09
    "p1_10" -> R.drawable.garment_p1_10
    "p2_01" -> R.drawable.garment_p2_01
    "p2_02" -> R.drawable.garment_p2_02
    "p2_03" -> R.drawable.garment_p2_03
    "p2_04" -> R.drawable.garment_p2_04
    "p2_05" -> R.drawable.garment_p2_05
    "p2_06" -> R.drawable.garment_p2_06
    "p2_07" -> R.drawable.garment_p2_07
    "p2_08" -> R.drawable.garment_p2_08
    "p2_09" -> R.drawable.garment_p2_09
    "p2_10" -> R.drawable.garment_p2_10
    "p3_01" -> R.drawable.garment_p3_01
    "p3_02" -> R.drawable.garment_p3_02
    "p3_03" -> R.drawable.garment_p3_03
    "p3_04" -> R.drawable.garment_p3_04
    "p3_05" -> R.drawable.garment_p3_05
    "p3_06" -> R.drawable.garment_p3_06
    "p3_07" -> R.drawable.garment_p3_07
    "p3_08" -> R.drawable.garment_p3_08
    "p3_09" -> R.drawable.garment_p3_09
    "p3_10" -> R.drawable.garment_p3_10
    "p4_01" -> R.drawable.garment_p4_01
    "p4_02" -> R.drawable.garment_p4_02
    "p4_03" -> R.drawable.garment_p4_03
    "p4_04" -> R.drawable.garment_p4_04
    "p4_05" -> R.drawable.garment_p4_05
    "p4_06" -> R.drawable.garment_p4_06
    "p4_07" -> R.drawable.garment_p4_07
    "p4_08" -> R.drawable.garment_p4_08
    "p4_09" -> R.drawable.garment_p4_09
    "p4_10" -> R.drawable.garment_p4_10
    "p5_01" -> R.drawable.garment_p5_01
    "p5_02" -> R.drawable.garment_p5_02
    "p5_03" -> R.drawable.garment_p5_03
    "p5_04" -> R.drawable.garment_p5_04
    "p5_05" -> R.drawable.garment_p5_05
    "p5_06" -> R.drawable.garment_p5_06
    "p5_07" -> R.drawable.garment_p5_07
    "p5_08" -> R.drawable.garment_p5_08
    "p5_09" -> R.drawable.garment_p5_09
    "p5_10" -> R.drawable.garment_p5_10
    "p6_01" -> R.drawable.garment_p6_01
    "p6_02" -> R.drawable.garment_p6_02
    "p6_03" -> R.drawable.garment_p6_03
    "p6_04" -> R.drawable.garment_p6_04
    "p6_05" -> R.drawable.garment_p6_05
    "p6_06" -> R.drawable.garment_p6_06
    "p6_07" -> R.drawable.garment_p6_07
    "p6_08" -> R.drawable.garment_p6_08
    "p6_09" -> R.drawable.garment_p6_09
    "p6_10" -> R.drawable.garment_p6_10
    else -> null
}

/**
 * Prefer remote/local [Garment.imageUrl], then bundled sample drawable.
 * Avoids hanger placeholders when a product photo exists.
 */
@Composable
internal fun GarmentProductImage(garment: Garment, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageUrl = garment.imageUrl
    val drawable = garmentDrawableRes(garment.id)
    var remoteBitmap by remember(imageUrl) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var remoteFailed by remember(imageUrl) { mutableStateOf(imageUrl.isNullOrBlank()) }

    LaunchedEffect(imageUrl) {
        if (imageUrl.isNullOrBlank()) {
            remoteBitmap = null
            remoteFailed = true
            return@LaunchedEffect
        }
        remoteFailed = false
        remoteBitmap = withContext(Dispatchers.IO) {
            runCatching {
                when {
                    imageUrl.startsWith("file:") || imageUrl.startsWith("/") -> {
                        val path = if (imageUrl.startsWith("file:")) Uri.parse(imageUrl).path else imageUrl
                        path?.let { p -> File(p).takeIf { it.exists() }?.absolutePath?.let { BitmapFactory.decodeFile(it) } }
                    }
                    imageUrl.startsWith("content:") || imageUrl.startsWith("android.resource:") -> {
                        context.contentResolver.openInputStream(Uri.parse(imageUrl))?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                    imageUrl.startsWith("http") -> {
                        val connection = (java.net.URL(imageUrl).openConnection() as java.net.HttpURLConnection).apply {
                            connectTimeout = 10_000
                            readTimeout = 12_000
                            instanceFollowRedirects = true
                            setRequestProperty("User-Agent", "LookWeave/1.0 (Android)")
                            setRequestProperty("Accept", "image/avif,image/webp,image/*,*/*")
                        }
                        try {
                            if (connection.responseCode !in 200..299) null
                            else connection.inputStream.use { BitmapFactory.decodeStream(it) }
                        } finally {
                            connection.disconnect()
                        }
                    }
                    else -> null
                }
            }.getOrNull()
        }
        remoteFailed = remoteBitmap == null
    }

    when {
        remoteBitmap != null -> Image(
            remoteBitmap!!.asImageBitmap(),
            contentDescription = "Product photo: ${garment.name}",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        !remoteFailed && !imageUrl.isNullOrBlank() -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Olive, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        }
        drawable != null -> Image(
            painter = painterResource(drawable),
            contentDescription = "Product photo: ${garment.name}, ${garment.colorName}",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        else -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Checkroom, contentDescription = null, tint = Olive, modifier = Modifier.size(28.dp))
        }
    }
}
