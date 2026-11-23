package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

/** Codec compatibility / corrupted-preference guardrails for theme customization. */
class AppearanceRecipesTest {
    @Test fun customColorValidation() {
        assertTrue(AppearanceRecipes.validAccent("#A1b2C3"))
        assertTrue(AppearanceRecipes.validAccent("Mono"))
        assertFalse(AppearanceRecipes.validAccent("#fff"))
        assertFalse(AppearanceRecipes.validAccent("#12345678"))
        assertFalse(AppearanceRecipes.validAccent("#12345Z"))
    }

    @Test fun nameAndLimitValidation() {
        assertFalse(AppearanceRecipes.validName(""))
        assertFalse(AppearanceRecipes.validName(" x "))
        assertFalse(AppearanceRecipes.validName("a".repeat(33)))
        assertTrue(AppearanceRecipes.validName("Neutral Glass"))
        assertEquals("Mono", AppearanceRecipes.defaultAccent("Glass"))
    }

    @Test fun savedRecipesSurviveRoundTrip() {
        val examples = listOf(
            AppearanceRecipe("glass-01", "Monochrome", "Glass", "Dark", "#C0C0C0"),
            AppearanceRecipe("paper-01", "Editorial", "Paper", "Light", "Rose")
        )
        assertEquals(examples, AppearanceRecipes.decode(AppearanceRecipes.encode(examples)))
    }

    @Test fun corruptedPreferencesFailClosed() {
        assertEquals(emptyList<AppearanceRecipe>(), AppearanceRecipes.decode("not json"))
        val invalid = """[{"id":"invalid","name":"Test","style":"Unknown","mode":"Dark","accent":"Mint"}]"""
        assertTrue(AppearanceRecipes.decode(invalid).isEmpty())
    }

    @Test fun cappedSavedRecipes() {
        val entries = (1..40).map {
            AppearanceRecipe(it.toString(), "Preset " + it, "Minimal", "Dark", "Blue")
        }
        assertEquals(AppearanceRecipes.LIMIT,
            AppearanceRecipes.decode(AppearanceRecipes.encode(entries)).size)
    }
}
