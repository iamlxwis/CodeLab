package site.lxwis.codelab

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeOperationsTest {
    @Test fun legacyModeFontsAndSyntaxMigrateIndependently() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences("appearance", 0)
        val keys = listOf("mode", "visual-style", "studio:Titanium", "app-appearance", "editor-and-fonts")
        val before = keys.associateWith { prefs.getString(it, null) }
        try {
            prefs.edit().putString("mode", "Light").putString("visual-style", "Clay")
                .putString("studio:Titanium", """{"background":"#AB12CD","uiFont":"Serif","editorFont":"System Mono","comments":"#52735F","editorTextSize":17}""")
                .remove("app-appearance").remove("editor-and-fonts").commit()
            val model = WorkspaceModel(app)
            assertEquals("Light", model.appearance)
            assertEquals("Serif", model.studioAppearance.uiFont)
            assertEquals("System Mono", model.studioAppearance.editorFont)
            assertEquals("#52735F", model.studioAppearance.comments)
            assertEquals(17, model.studioAppearance.editorTextSize)
            assertEquals("", model.studioAppearance.background)
            model.setMode("Dark")
            model.updateStudioAppearance(model.studioAppearance.copy(editorMode = "Light"))
            val restored = WorkspaceModel(app)
            assertEquals("Dark", restored.appearance)
            assertEquals("Light", restored.studioAppearance.editorMode)
            assertEquals("#52735F", restored.studioAppearance.comments)
            assertEquals("Clay", prefs.getString("visual-style", null))
        } finally {
            val editor = prefs.edit()
            before.forEach { (key, value) -> if (value == null) editor.remove(key) else editor.putString(key, value) }
            editor.commit()
        }
    }
}
