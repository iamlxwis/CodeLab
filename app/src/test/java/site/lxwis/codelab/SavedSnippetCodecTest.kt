package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class SavedSnippetCodecTest {
    @Test fun preservesMultilineUnicodeSelectedSource() {
        val selected = "fun café() {\n    println(\"日本語\")\n}\n"
        val item = StoredSnippet("Example", selected)
        val saved = SavedSnippetCodec.add(emptyList(), item)!!
        assertEquals(listOf(item), SavedSnippetCodec.decode(SavedSnippetCodec.encode(saved)))
    }

    @Test fun rejectsDuplicateAndOversizedSnippets() {
        val first = StoredSnippet("Example", "print(1)")
        assertNull(SavedSnippetCodec.add(listOf(first), StoredSnippet("example", "print(2)")))
        assertNull(SavedSnippetCodec.add(emptyList(), StoredSnippet("Long", "a".repeat(4097))))
        assertNull(SavedSnippetCodec.add(emptyList(), StoredSnippet("Bad\nTitle", "x")))
        val full = (1..24).map { StoredSnippet("Snippet $it", "x") }
        assertNull(SavedSnippetCodec.add(full, StoredSnippet("More", "y")))
    }

    @Test fun malformedSavedEntriesCannotCrashOrInjectExtraSnippets() {
        assertEquals(emptyList<StoredSnippet>(), SavedSnippetCodec.decode("not-base64:%%%"))
        val valid = SavedSnippetCodec.encode(listOf(StoredSnippet("Safe", "x")))
        assertEquals(listOf(StoredSnippet("Safe", "x")),
            SavedSnippetCodec.decode(valid + "\ncorrupted"))
        assertTrue(SavedSnippetCodec.remove(listOf(StoredSnippet("Safe", "x")), "Safe").isEmpty())
    }
}
