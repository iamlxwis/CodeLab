package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageVisualCoverageTest {
    @Test fun everyEditingLanguageHasAnOfflineLogoOrUniqueMonogram() {
        val fallbackLabels = mutableSetOf<String>()
        LanguageCatalog.entries.forEach { language ->
            val visual = languageVisual(language.extension)
            assertNotNull(language.name, visual)
            requireNotNull(visual)
            assertTrue(language.name, (visual.logo != null) xor (visual.monogram != null))
            visual.monogram?.let { assertTrue("Duplicate fallback: $it", fallbackLabels.add(it)) }
        }
    }

    @Test fun filenameAliasesUseTheSameLanguageVisual() {
        assertEquals("dockerfile", languageExtension("infra/Dockerfile"))
        assertEquals("mk", languageExtension("GNUmakefile"))
        assertEquals(languageVisual("kt"), languageVisual("kts"))
        assertEquals(languageVisual("cpp"), languageVisual("hxx"))
        assertEquals(languageVisual("yaml"), languageVisual("yml"))
    }
}
