package site.lxwis.codelab

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

/** Tests actual Android Canvas rendering rather than only a JVM lexical tokenizer. */
@RunWith(AndroidJUnit4::class)
class ScreenshotRendererAndroidTest {
    @Test fun createsSyntaxColoredTransparentPngAtRequestedDimensions() {
        val image = ScreenshotRenderer.render(
            "main.kt", "val message = \"Hello\"\nprintln(message)",
            ScreenshotOptions(
                palette = "Nocturne", aspect = ScreenshotAspect.LANDSCAPE,
                background = "Transparent", highlightLine = 2,
                footer = "CodeLab test"
            )
        )
        try {
            assertEquals(1080, image.width)
            assertEquals(608, image.height)
            assertEquals(Color.TRANSPARENT, image.getPixel(0, 0))
            val bytes = ByteArrayOutputStream()
            assertTrue(image.compress(Bitmap.CompressFormat.PNG, 100, bytes))
            assertTrue(bytes.size() > 1000)
        } finally {
            image.recycle()
        }
    }
}
