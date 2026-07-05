package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SampleImageGenerator {

    private const val TAG = "SampleImageGenerator"

    suspend fun preloadIfNeeded(context: Context, repository: ImageRepository) {
        withContext(Dispatchers.IO) {
            try {
                val count = repository.getImageCount()
                if (count > 0) {
                    Log.d(TAG, "Database already has $count images. Skipping preloading.")
                    return@withContext
                }

                Log.d(TAG, "Preloading sample artistic images...")
                val uploadsDir = File(context.filesDir, "uploads")
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs()
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val todayStr = dateFormat.format(Date())

                // 1. Cosmic Vaporwave (Square - 1:1)
                val cosmicFile = File(uploadsDir, "sample_cosmic.png")
                generateCosmicVaporwave(cosmicFile)
                repository.insertImage(
                    ImageItem(
                        title = "Cosmic Vaporwave Neon",
                        filePath = cosmicFile.absolutePath,
                        uploadDate = todayStr,
                        sizeInBytes = cosmicFile.length(),
                        width = 800,
                        height = 800
                    )
                )

                // 2. Sunset Minimalist Vista (Landscape - 16:9)
                val sunsetFile = File(uploadsDir, "sample_sunset.png")
                generateSunsetVista(sunsetFile)
                repository.insertImage(
                    ImageItem(
                        title = "Sunset Minimalist Vista",
                        filePath = sunsetFile.absolutePath,
                        uploadDate = todayStr,
                        sizeInBytes = sunsetFile.length(),
                        width = 1280,
                        height = 720
                    )
                )

                // 3. Emerald Pine Forest (Portrait - 3:4)
                val forestFile = File(uploadsDir, "sample_forest.png")
                generateEmeraldForest(forestFile)
                repository.insertImage(
                    ImageItem(
                        title = "Emerald Pine Forest",
                        filePath = forestFile.absolutePath,
                        uploadDate = todayStr,
                        sizeInBytes = forestFile.length(),
                        width = 600,
                        height = 800
                    )
                )

                // 4. Glassmorphism Abstract Geometry (Portrait - 9:16)
                val glassFile = File(uploadsDir, "sample_geometry.png")
                generateAbstractGeometry(glassFile)
                repository.insertImage(
                    ImageItem(
                        title = "Glassmorphism Geometry",
                        filePath = glassFile.absolutePath,
                        uploadDate = todayStr,
                        sizeInBytes = glassFile.length(),
                        width = 540,
                        height = 960
                    )
                )

                Log.d(TAG, "Sample images loaded successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating or saving sample images", e)
            }
        }
    }

    private fun generateCosmicVaporwave(file: File) {
        val width = 800
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw radial gradient background
        val paint = Paint().apply { isAntiAlias = true }
        val radialShader = RadialGradient(
            width / 2f, height / 2f, width * 0.7f,
            intArrayOf(Color.parseColor("#3b0066"), Color.parseColor("#0d001a")),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = radialShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw vaporwave grid lines (perspectives)
        paint.shader = null
        paint.color = Color.parseColor("#ff007f")
        paint.strokeWidth = 2f
        paint.alpha = 80
        val horizon = height * 0.55f

        // Horizontal grid lines
        for (y in horizon.toInt()..height step 35) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
        }

        // Perspective grid lines radiating from the horizon center
        val centerX = width / 2f
        for (x in -width..width * 2 step 60) {
            canvas.drawLine(centerX, horizon, x.toFloat(), height.toFloat(), paint)
        }

        // Draw neon synthwave sun on horizon
        val sunPaint = Paint().apply {
            isAntiAlias = true
            val sunShader = LinearGradient(
                centerX, horizon - 200f, centerX, horizon,
                Color.parseColor("#ff007f"), Color.parseColor("#ffea00"),
                Shader.TileMode.CLAMP
            )
            shader = sunShader
        }
        val sunRect = RectF(centerX - 180f, horizon - 180f, centerX + 180f, horizon + 180f)
        canvas.drawArc(sunRect, 180f, 180f, true, sunPaint)

        // Draw horizontal scanline cuts on the sun (classic synthwave retro sun look)
        val cutPaint = Paint().apply {
            color = Color.parseColor("#0d001a")
            style = Paint.Style.FILL
        }
        var currentCutY = horizon - 150f
        var cutHeight = 4f
        while (currentCutY < horizon) {
            canvas.drawRect(centerX - 180f, currentCutY, centerX + 180f, currentCutY + cutHeight, cutPaint)
            currentCutY += 25f
            cutHeight += 3f // increasing cut height downwards
        }

        // Draw twinkling stars
        paint.color = Color.WHITE
        paint.shader = null
        val random = java.util.Random(42)
        for (i in 0..40) {
            val starX = random.nextFloat() * width
            val starY = random.nextFloat() * (horizon - 20)
            paint.alpha = random.nextInt(155) + 100
            val size = random.nextFloat() * 3f + 1f
            canvas.drawCircle(starX, starY, size, paint)
        }

        // Save file
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        bitmap.recycle()
    }

    private fun generateSunsetVista(file: File) {
        val width = 1280
        val height = 720
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw beautiful sunset linear sky gradient
        val paint = Paint().apply { isAntiAlias = true }
        val skyShader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.parseColor("#2c3e50"), // deep dark blue-grey at the top
                Color.parseColor("#e74c3c"), // warm red-orange
                Color.parseColor("#f1c40f")  // glowing yellow at horizon
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = skyShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw glowing mountain silhouettes
        val mountainPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1a1a24")
            style = Paint.Style.FILL
            shader = null
        }

        // Background mountains
        val path1 = android.graphics.Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(0f, height * 0.7f)
            quadTo(width * 0.25f, height * 0.5f, width * 0.5f, height * 0.75f)
            quadTo(width * 0.75f, height * 0.9f, width.toFloat(), height * 0.65f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        mountainPaint.alpha = 180
        canvas.drawPath(path1, mountainPaint)

        // Foreground mountains (darker)
        val path2 = android.graphics.Path().apply {
            moveTo(0f, height.toFloat())
            lineTo(0f, height * 0.85f)
            lineTo(width * 0.2f, height * 0.75f)
            lineTo(width * 0.4f, height * 0.82f)
            lineTo(width * 0.7f, height * 0.68f)
            lineTo(width * 0.9f, height * 0.88f)
            lineTo(width.toFloat(), height * 0.78f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        mountainPaint.alpha = 255
        mountainPaint.color = Color.parseColor("#0e0e15")
        canvas.drawPath(path2, mountainPaint)

        // Draw a giant orange sun on background
        val sunPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#ff7f50")
            alpha = 140
        }
        canvas.drawCircle(width * 0.75f, height * 0.45f, 120f, sunPaint)

        // Save file
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        bitmap.recycle()
    }

    private fun generateEmeraldForest(file: File) {
        val width = 600
        val height = 800
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Deep emerald forest atmospheric background
        val paint = Paint().apply { isAntiAlias = true }
        val forestShader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.parseColor("#0a2f1d"), // extremely dark forest green
                Color.parseColor("#154734"), // rich emerald deep green
                Color.parseColor("#071e14")  // blackish bottom
            ),
            null, Shader.TileMode.CLAMP
        )
        paint.shader = forestShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw geometric fog strips in the middle
        paint.shader = null
        paint.color = Color.parseColor("#48c9b0")
        paint.alpha = 20
        canvas.drawRect(0f, height * 0.3f, width.toFloat(), height * 0.55f, paint)

        // Draw minimal pine trees
        val treePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Draw standard tree drawing logic
        fun drawPineTree(canvas: Canvas, x: Float, bottomY: Float, treeWidth: Float, treeHeight: Float, colorHex: String, alphaValue: Int) {
            treePaint.color = Color.parseColor(colorHex)
            treePaint.alpha = alphaValue

            val path = android.graphics.Path()
            // Draw trunk
            canvas.drawRect(x - treeWidth * 0.08f, bottomY - treeHeight * 0.15f, x + treeWidth * 0.08f, bottomY, treePaint)

            // Draw three layered triangles
            for (i in 0..2) {
                val layerBottom = bottomY - treeHeight * 0.15f - (treeHeight * 0.25f * i)
                val layerHeight = treeHeight * 0.4f
                path.reset()
                path.moveTo(x, layerBottom - layerHeight)
                path.lineTo(x - (treeWidth * (1f - i * 0.25f)) / 2f, layerBottom)
                path.lineTo(x + (treeWidth * (1f - i * 0.25f)) / 2f, layerBottom)
                path.close()
                canvas.drawPath(path, treePaint)
            }
        }

        // Background distant trees (faint/smaller)
        val random = java.util.Random(1337)
        for (i in 0..8) {
            val treeX = random.nextFloat() * width
            val treeY = height * 0.65f + random.nextFloat() * 50f
            val tWidth = 80f + random.nextFloat() * 40f
            val tHeight = 160f + random.nextFloat() * 80f
            drawPineTree(canvas, treeX, treeY, tWidth, tHeight, "#07331f", 120)
        }

        // Foreground trees (bigger/darker)
        for (i in 0..5) {
            val treeX = random.nextFloat() * width
            val treeY = height * 0.85f + random.nextFloat() * 60f
            val tWidth = 140f + random.nextFloat() * 60f
            val tHeight = 280f + random.nextFloat() * 100f
            drawPineTree(canvas, treeX, treeY, tWidth, tHeight, "#041910", 255)
        }

        // Save file
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        bitmap.recycle()
    }

    private fun generateAbstractGeometry(file: File) {
        val width = 540
        val height = 960
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Soft elegant background gradient: blue to magenta
        val paint = Paint().apply { isAntiAlias = true }
        val grad = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(
                Color.parseColor("#4facfe"), // neon light blue
                Color.parseColor("#00f2fe"), // cyan
                Color.parseColor("#f093fb"), // lavender pink
                Color.parseColor("#f5576c")  // hot pink coral
            ),
            null, Shader.TileMode.CLAMP
        )
        paint.shader = grad
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw layered futuristic glass spheres and cards
        paint.shader = null

        // 1. Warm glowing sphere
        paint.color = Color.parseColor("#ffea00")
        paint.alpha = 180
        canvas.drawCircle(width * 0.7f, height * 0.3f, 160f, paint)

        // 2. Translucent glowing pink sphere
        paint.color = Color.parseColor("#e0115f")
        paint.alpha = 150
        canvas.drawCircle(width * 0.3f, height * 0.65f, 220f, paint)

        // 3. Simulated glass card overlaid on top (frost white translucent roundrect)
        val glassPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 45 // frosted glass look
            style = Paint.Style.FILL
        }
        val glassBorder = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 110
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val cardLeft = width * 0.15f
        val cardTop = height * 0.25f
        val cardRight = width * 0.85f
        val cardBottom = height * 0.75f
        val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)
        canvas.drawRoundRect(cardRect, 40f, 40f, glassPaint)
        canvas.drawRoundRect(cardRect, 40f, 40f, glassBorder)

        // Save file
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        bitmap.recycle()
    }
}
