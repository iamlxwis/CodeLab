package site.lxwis.codelab

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test

class ThemeOperationsTest {
    @Test fun renamingAndDuplicatingKeepAllThemeFieldsAndPersistDistinctIds() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences("appearance", 0)
        val before = prefs.getString("saved-appearance-recipes", null)
        try {
            val model = WorkspaceModel(app)
            val name = "Test " + java.util.UUID.randomUUID().toString().take(8)
            assertTrue(model.saveAppearanceRecipe(name))
            val original = model.savedAppearanceRecipes.last()
            val activeBefore = model.studioAppearance
            assertTrue(model.duplicateAppearanceRecipe(original.id))
            val copy = model.savedAppearanceRecipes.last()
            assertNotEquals(original.id, copy.id)
            assertEquals(original.copy(id = copy.id, name = copy.name), copy)
            assertTrue(model.renameAppearanceRecipe(copy.id, "Renamed copy"))
            assertEquals(copy.copy(name = "Renamed copy"), model.savedAppearanceRecipes.last())
            assertEquals(original, model.savedAppearanceRecipes.first { it.id == original.id })
            assertEquals(activeBefore, model.studioAppearance)
            assertEquals(model.savedAppearanceRecipes, WorkspaceModel(app).savedAppearanceRecipes)
        } finally {
            if (before == null) prefs.edit().remove("saved-appearance-recipes").commit()
            else prefs.edit().putString("saved-appearance-recipes", before).commit()
        }
    }
}
