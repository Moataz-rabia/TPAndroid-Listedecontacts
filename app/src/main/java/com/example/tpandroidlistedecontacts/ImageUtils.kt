package com.example.tpandroidlistedecontacts

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Chargement de photos depuis le stockage interne, avec sous-échantillonnage
 * pour éviter de saturer la mémoire avec les photos pleine résolution.
 */
object ImageUtils {

    fun loadBitmap(path: String?, targetSizePx: Int): Bitmap? {
        if (path.isNullOrBlank()) return null
        if (!File(path).exists()) return null

        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sampleSize = 1
            val largestSide = maxOf(bounds.outWidth, bounds.outHeight)
            while (largestSide / (sampleSize * 2) >= targetSizePx) {
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            null
        }
    }
}
