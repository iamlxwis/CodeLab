package site.lxwis.codelab

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class EditorRapidInputTest {
    @get:Rule val compose = createComposeRule()
    private fun find(view: View): SourceView? {
        if (view is SourceView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) find(view.getChildAt(i))?.let { return it }
        return null
    }

    @Test fun rapidPhysicalTypingSurvivesViewportChangeAndExternalReplacementStillWorks() {
        val source = mutableStateOf("")
        val paneHeight = mutableStateOf(500.dp)
        lateinit var root: View
        compose.setContent {
            val view = LocalView.current
            SideEffect { root = view.rootView }
            MaterialTheme { Box(Modifier.height(paneHeight.value)) {
                CodeEditor("rapid-input", "main.js", source.value, {
                    source.value = it
                    paneHeight.value = 220.dp
                }, readCurrentText = { source.value }, motionMillis = 0)
            } }
        }
        lateinit var native: SourceView
        compose.runOnIdle {
            native = checkNotNull(find(root))
            native.requestFocus()
        }
        InstrumentationRegistry.getInstrumentation().sendStringSync("print(6*7)")
        compose.waitForIdle()
        compose.runOnIdle {
            assertSame("Viewport changes must retain the native editor", native, find(root))
            assertEquals("print(6*7)", native.text.toString())
            assertEquals("print(6*7)", source.value)
            source.value = "print(42) // external replacement"
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(source.value, native.text.toString()) }
    }
}
