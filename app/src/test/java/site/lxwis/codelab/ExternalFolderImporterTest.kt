package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class ExternalFolderImporterTest {
    @Test fun blocksDirectoryTraversalAndReservedFileNameCharacters() {
        assertTrue(ExternalFolderImporter.validSegment("main.kt"))
        assertTrue(ExternalFolderImporter.validSegment("source assets"))
        for (name in listOf("", ".", "..", "../abc", "a/b", "a\\b", "a:b", "bad\nname")) {
            assertFalse(name, ExternalFolderImporter.validSegment(name))
        }
    }
}
