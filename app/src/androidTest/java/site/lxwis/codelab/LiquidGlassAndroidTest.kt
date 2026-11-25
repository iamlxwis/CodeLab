package site.lxwis.codelab

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.PixelCopy
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.geometry.Size
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class LiquidGlassAndroidTest {
    @Test fun shaderDisplacesBackdropAtCapsuleEdge() {
        val painted = CountDownLatch(1)
        val copied = CountDownLatch(1)
        val tileWidth = 80
        val tileHeight = 48
        val colors = Bitmap.createBitmap(tileWidth, tileHeight, Bitmap.Config.ARGB_8888)
        colors.eraseColor(Color.GREEN)
        for (y in 0 until 5) for (x in 0 until tileWidth) colors.setPixel(x, y, Color.RED)
        val pixels = Bitmap.createBitmap(tileWidth * 2, tileHeight, Bitmap.Config.ARGB_8888)
        try {
            var copyStatus = PixelCopy.ERROR_UNKNOWN
            lateinit var comparison: View
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    val source = BitmapShader(colors, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                    val original = Paint().apply { shader = source }
                    val refracted = Paint().apply {
                        shader = refractionShader(Size(tileWidth.toFloat(), tileHeight.toFloat()), progressive = false)
                            .apply { setInputShader("backdrop", source) }
                    }
                    comparison = object : View(activity as Context) {
                        override fun onDraw(canvas: Canvas) {
                            canvas.drawRect(0f, 0f, tileWidth.toFloat(), tileHeight.toFloat(), original)
                            canvas.save()
                            canvas.translate(tileWidth.toFloat(), 0f)
                            canvas.drawRect(0f, 0f, tileWidth.toFloat(), tileHeight.toFloat(), refracted)
                            canvas.restore()
                            if (canvas.isHardwareAccelerated) painted.countDown()
                        }
                    }
                    activity.setContentView(FrameLayout(activity).apply {
                        addView(comparison, FrameLayout.LayoutParams(tileWidth * 2, tileHeight, Gravity.CENTER))
                    })
                }
                assertTrue("Hardware-accelerated backdrop did not draw", painted.await(10, TimeUnit.SECONDS))
                scenario.onActivity { activity ->
                    val bounds = IntArray(2).also(comparison::getLocationInWindow)
                    val rect = Rect(bounds[0], bounds[1], bounds[0] + tileWidth * 2, bounds[1] + tileHeight)
                    PixelCopy.request(activity.window, rect, pixels, { result ->
                        copyStatus = result
                        copied.countDown()
                    }, Handler(Looper.getMainLooper()))
                }
                assertTrue("PixelCopy did not finish", copied.await(10, TimeUnit.SECONDS))
                assertEquals(PixelCopy.SUCCESS, copyStatus)
                assertEquals(Color.RED, pixels.getPixel(tileWidth / 2, 2))
                assertEquals(Color.GREEN, pixels.getPixel(tileWidth / 2 + tileWidth, 2))
                assertEquals(Color.GREEN, pixels.getPixel(tileWidth / 2 + tileWidth, tileHeight / 2))
            }
        } finally {
            pixels.recycle()
            colors.recycle()
        }
    }
}
