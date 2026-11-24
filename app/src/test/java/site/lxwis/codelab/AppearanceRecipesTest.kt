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

    @Test fun studioOverridesRoundTrip() {
        val studio = StudioAppearance(background = "#112233", corners = 27, uiFont = "Serif",
            keywords = "#AA7722", glassQuality = "Lightweight", blur = 0)
        val recipe = AppearanceRecipe("test", "Custom", "Crystal", "System", "#102030", studio)
        assertEquals(listOf(recipe), AppearanceRecipes.decode(AppearanceRecipes.encode(listOf(recipe))))
    }

    @Test fun corruptedPreferencesFailClosed() {
        assertEquals(emptyList<AppearanceRecipe>(), AppearanceRecipes.decode("not json"))
        val invalid = """[{"id":"invalid","name":"Test","style":"Unknown","mode":"Dark","accent":"Mint"}]"""
        assertTrue(AppearanceRecipes.decode(invalid).isEmpty())
    }

    @Test fun savedRecipesAreNotSilentlyTruncated() {
        val entries = (1..40).map {
            AppearanceRecipe(it.toString(), "Preset " + it, "Minimal", "Dark", "Blue")
        }
        assertEquals(entries.size,
            AppearanceRecipes.decode(AppearanceRecipes.encode(entries)).size)
    }
    @Test fun motionRecipeRoundTripAndLegacyCompatibility() {
        val original = AppearanceRecipe("motion", "Quiet", "Crystal", "System", "Mono",
            StudioAppearance(), ThemeMotionRecipe("Reduced", 0, 80, 10))
        assertEquals(listOf(original), AppearanceRecipes.decode(AppearanceRecipes.encode(listOf(original))))
        val legacy = AppearanceRecipe("legacy", "Old", "Glass", "Dark", "Mono")
        assertNull(AppearanceRecipes.decode(AppearanceRecipes.encode(listOf(legacy))).single().motion)
    }

    @Test fun importedMotionBoundsAndInvalidMode() {
        val json = org.json.JSONObject("""{"preference":"Full","intensity":-10,"speed":999,"spring":101}""")
        assertEquals(ThemeMotionRecipe("Full", 0, 200, 100), ThemeMotionRecipe.decode(json))
        assertNull(ThemeMotionRecipe.decode(org.json.JSONObject("""{"preference":"Unknown"}""")))
    }

    @Test fun newMotionPresetsAndTransitionsRetainLegacyDefaults() {
        for (preset in listOf("Reduced", "Standard", "Fluid")) {
            for (effect in listOf("None", "Fade", "Slide")) {
                val motion = ThemeMotionRecipe(preset, 80, 120, 40, effect)
                assertEquals(motion, ThemeMotionRecipe.decode(motion.json()))
            }
        }
        val legacy = org.json.JSONObject("""{"preference":"Full"}""")
        assertEquals("Fade", ThemeMotionRecipe.decode(legacy)?.transition)
        legacy.put("transition", "unknown")
        assertEquals("Fade", ThemeMotionRecipe.decode(legacy)?.transition)
    }

}
