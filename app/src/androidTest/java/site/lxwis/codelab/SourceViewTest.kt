package site.lxwis.codelab

import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.BaseInputConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SourceViewTest {
    @Test fun logicalLineSelectionHandlesEmptyFirstLineAndEdits() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val editor = SourceView(ContextThemeWrapper(instrumentation.targetContext,
                android.R.style.Theme_Material), "main.js")
            editor.setText("\nsecond\n")
            assertEquals(3, editor.logicalLineCount)
            editor.setSelection(0)
            editor.selectCurrentLine()
            assertEquals(0, editor.selectionStart)
            assertEquals(0, editor.selectionEnd)
            editor.goToLogicalLine(2)
            editor.selectCurrentLine()
            assertEquals("second", editor.text.subSequence(editor.selectionStart, editor.selectionEnd).toString())
            editor.text.insert(0, "new\n")
            assertEquals(4, editor.logicalLineCount)
            assertEquals(3, editor.logicalLineAt(5))
            editor.goToLogicalLine(4)
            assertEquals(editor.length(), editor.selectionStart)
        }
    }

    @Test fun duplicateAndDeletePreserveEmptyLinesAndTrailingNewline() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val editor = SourceView(ContextThemeWrapper(instrumentation.targetContext,
                android.R.style.Theme_Material), "main.js")
            editor.setText("\nsecond\n")
            editor.setSelection(0)
            editor.duplicateCurrentLine()
            assertEquals("\n\nsecond\n", editor.text.toString())
            editor.deleteCurrentLine()
            assertEquals("\nsecond\n", editor.text.toString())
            editor.setText("first\nsecond\n")
            editor.goToLogicalLine(2)
            editor.deleteCurrentLine()
            assertEquals("first\n", editor.text.toString())
            editor.goToLogicalLine(2)
            assertEquals(1, editor.logicalColumnAt(editor.selectionEnd))
            editor.deleteCurrentLine()
            assertEquals("first", editor.text.toString())
        }
    }

    @Test fun composingImeTextRemainsLiteralUntilCommit() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val editor = SourceView(ContextThemeWrapper(instrumentation.targetContext,
                android.R.style.Theme_Material), "main.js")
            editor.setText("")
            val connection = editor.onCreateInputConnection(EditorInfo())
            assertNotNull(connection)
            connection!!
            // Composition is not treated as physical keyboard insertion, and
            // must not trigger automatic bracket pairing.
            connection.setComposingText("(", 1)
            assertEquals("(", editor.text.toString())
            assertEquals(0, BaseInputConnection.getComposingSpanStart(editor.text))
            connection.commitText("(", 1)
            assertEquals("(", editor.text.toString())
            assertEquals(-1, BaseInputConnection.getComposingSpanStart(editor.text))
            connection.commitText("(", 1)
            assertEquals("(()", editor.text.toString())
        }
    }

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

    @Test fun userWholeFileEditsNotifyAutosaveButModelSyncDoesNot() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val editor = SourceView(ContextThemeWrapper(instrumentation.targetContext,
                android.R.style.Theme_Material), "main.kt")
            editor.setText("val old = 1")
            val changes = mutableListOf<String>()
            editor.changed = { changes.add(it) }
            editor.replaceSource("val synced = 2")
            assertEquals(emptyList<String>(), changes)
            editor.replaceFromUser("val saved = 3")
            assertEquals(listOf("val saved = 3"), changes)
            editor.replaceFromUser("val saved = 3")
            assertEquals(1, changes.size)
        }
    }
}
