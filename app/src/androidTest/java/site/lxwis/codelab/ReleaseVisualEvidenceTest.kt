package site.lxwis.codelab

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Captures the real activity on a disposable emulator, never a synthetic preview. */
@RunWith(AndroidJUnit4::class)
class ReleaseVisualEvidenceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private fun await(check: () -> Boolean) {
        val deadline = android.os.SystemClock.elapsedRealtime() + 20_000
        while (android.os.SystemClock.elapsedRealtime() < deadline) {
            if (check()) return
            android.os.SystemClock.sleep(80)
        }
        assertTrue("Timed out waiting for rendered activity/IME", check())
    }
    private fun source(view: View): SourceView? {
        if (view is SourceView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) source(view.getChildAt(i))?.let { return it }
        return null
    }
    @Test fun captureActualEditorAndKeyboardViewport() {
        val args = InstrumentationRegistry.getArguments()
        // Invoked explicitly by layout_screenshots.py; ordinary instrumentation keeps its state.
        if (args.getString("visualEvidence") != "true") return
        val style = args.getString("visualTheme") ?: "Dark"
        val size = args.getString("visualSize") ?: "phone"
        val context = instrumentation.targetContext
        context.getSharedPreferences("first-run-guide", Context.MODE_PRIVATE).edit().putBoolean("complete", true).commit()
        val output = File(context.getExternalFilesDir(null), "release-evidence").apply { mkdirs() }
        val projectName = "Release review"
        val createProject = !File(context.filesDir, "workspace/projects/$projectName").exists()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[WorkspaceModel::class.java]
                model.setMode(style)
                model.updateStudioAppearance(StudioAppearance(editorTextSize = 15, editorMode = style))
                model.chooseFocusMode(false)
                model.openProject(projectName, create = createProject, template = "JavaScript", starter = StarterExamples.HELLO)
            }
            await {
                var ready = false
                scenario.onActivity { activity ->
                    val model = ViewModelProvider(activity)[WorkspaceModel::class.java]
                    ready = model.project == projectName && model.active != null && source(activity.window.decorView) != null
                }
                ready
            }
            scenario.onActivity { activity ->
                val model = ViewModelProvider(activity)[WorkspaceModel::class.java]
                model.edit((1..40).joinToString("\n") { "const line$it = $it;" })
                model.flush()
            }
            instrumentation.waitForIdleSync()
            fun capture(suffix: String) {
                val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                File(output, "$size-${style.lowercase()}-$suffix.png").outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
                bitmap.recycle()
            }
            // Give the rendered surface a frame after the editor's background analysis.
            android.os.SystemClock.sleep(300)
            capture("editor")
            if (size == "landscape") {
                var visibleLines = 0
                scenario.onActivity { activity ->
                    val editor = checkNotNull(source(activity.window.decorView))
                    val rect = Rect()
                    assertTrue("Landscape editor must be visible", editor.getLocalVisibleRect(rect))
                    visibleLines = (rect.height() - editor.compoundPaddingTop - editor.compoundPaddingBottom)
                        .coerceAtLeast(0) / editor.lineHeight.coerceAtLeast(1)
                }
                File(output, "$size-${style.lowercase()}-viewport.json").writeText(
                    JSONObject().put("visible_complete_lines", visibleLines).toString(2))
                assertTrue("800x360dp must retain >=4 complete source lines, measured $visibleLines", visibleLines >= 4)
            }
            if (size == "phone") {
                scenario.onActivity { activity ->
                    val editor = checkNotNull(source(activity.window.decorView))
                    editor.requestFocus()
                    (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)
                }
                await {
                    var visible = false
                    scenario.onActivity { activity -> visible = ViewCompat.getRootWindowInsets(activity.window.decorView)
                        ?.isVisible(WindowInsetsCompat.Type.ime()) == true }
                    visible
                }
                android.os.SystemClock.sleep(300)
                capture("keyboard")
                var lines = 0
                val metrics = JSONObject()
                scenario.onActivity { activity ->
                    val editor = checkNotNull(source(activity.window.decorView))
                    val rect = Rect()
                    assertTrue(editor.getLocalVisibleRect(rect))
                    val usable = (rect.height() - editor.compoundPaddingTop - editor.compoundPaddingBottom).coerceAtLeast(0)
                    lines = usable / editor.lineHeight.coerceAtLeast(1)
                    metrics.put("visible_height_px", rect.height()).put("line_height_px", editor.lineHeight)
                        .put("visible_complete_lines", lines).put("theme", style)
                        .put("font_scale", activity.resources.configuration.fontScale.toDouble())
                        .put("text_size_px", editor.textSize.toDouble())
                        .put("screen_width_dp", activity.resources.configuration.screenWidthDp)
                        .put("screen_height_dp", activity.resources.configuration.screenHeightDp)
                }
                File(output, "$size-${style.lowercase()}-viewport.json").writeText(metrics.toString(2))
                assertTrue("360x800dp with IME must show >=12 complete code lines, measured $lines", lines >= 12)
            }
        }
    }
}
