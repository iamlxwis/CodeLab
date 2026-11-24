package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DocumentPreviewTest {
    private fun archive(vararg entries: Pair<String, ByteArray>): ByteArray =
        ByteArrayOutputStream().also { out ->
            ZipOutputStream(out).use { zip ->
                entries.forEach { (path, data) ->
                    zip.putNextEntry(ZipEntry(path)); zip.write(data); zip.closeEntry()
                }
            }
        }.toByteArray()

    @Test fun readsNamesWithoutExtracting() {
        val result = DocumentPreviews.archive(ByteArrayInputStream(archive(
            "main.js" to "print(42)".toByteArray(), "src/main.py" to "print(42)".toByteArray())))
        assertEquals(listOf("main.js", "src/main.py"), result.entries)
        assertFalse(result.limited)
    }
    @Test fun boundsLargeExpansionAndEntryCount() {
        val large = DocumentPreviews.archive(ByteArrayInputStream(archive("large.txt" to ByteArray(1024 * 1024))))
        assertTrue(large.limited)
        val many = DocumentPreviews.archive(ByteArrayInputStream(archive(*Array(40) { "file$it" to ByteArray(0) })))
        assertTrue(many.limited)
        assertEquals(16, many.entries.size)
    }
    @Test fun boundedTextAndBinaryHandling() {
        assertEquals("hello\nworld", DocumentPreviews.text(ByteArrayInputStream("hello\nworld".toByteArray())).text)
        assertTrue(DocumentPreviews.text(ByteArrayInputStream(ByteArray(9000) { 65 })).limited)
        assertNull(DocumentPreviews.text(ByteArrayInputStream(byteArrayOf(0, 1, 2))).text)
        val utf16 = byteArrayOf(0xff.toByte(), 0xfe.toByte()) + "hello".toByteArray(Charsets.UTF_16LE)
        assertEquals("hello", DocumentPreviews.text(ByteArrayInputStream(utf16)).text)
    }
}
