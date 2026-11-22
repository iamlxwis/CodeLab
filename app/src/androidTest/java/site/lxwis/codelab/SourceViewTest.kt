package site.lxwis.codelab

import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SourceViewTest {
    @Test fun keyboardInputPairsAndIndentsCode() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val editor = SourceView(ContextThemeWrapper(instrumentation.targetContext,
                android.R.style.Theme_Material), "main.js")
            editor.setText("if (ready) ")
            editor.setSelection(editor.length())
            val connection = editor.onCreateInputConnection(EditorInfo())
            assertNotNull(connection)
            connection!!.commitText("{", 1)
            assertEquals("if (ready) {}", editor.text.toString())
            connection.commitText("\n", 1)
            assertEquals("if (ready) {\n    }", editor.text.toString())
            connection.commitText("print(42)", 1)
            connection.commitText("}", 1)
            assertEquals("if (ready) {\n    print(42)}", editor.text.toString())
            assertEquals(editor.length(), editor.selectionStart)
            editor.onKeyDown(KeyEvent.KEYCODE_TAB, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
            assertEquals("if (ready) {\n    print(42)}    ", editor.text.toString())
        }
    }
}
