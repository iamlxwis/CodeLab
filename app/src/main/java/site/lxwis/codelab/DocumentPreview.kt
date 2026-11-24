package site.lxwis.codelab

import java.io.ByteArrayOutputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

/** Read-only preview, not an import validator. Never extract or open archive entries as files. */
internal data class DocumentPreview(val entries: List<String> = emptyList(), val text: String? = null,
    val limited: Boolean = false, val message: String? = null)

internal object DocumentPreviews {
    private const val INPUT_LIMIT = 512 * 1024
    private const val EXPANDED_LIMIT = 256 * 1024
    private const val ENTRY_LIMIT = 16
    private const val TEXT_LIMIT = 4096
    private class PreviewLimit : IOException()
    private class LimitedInput(input: InputStream) : FilterInputStream(input) {
        private var left = INPUT_LIMIT
        override fun read(): Int {
            if (left == 0) throw PreviewLimit()
            val result = super.read()
            if (result >= 0) left--
            return result
        }
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            if (left == 0) throw PreviewLimit()
            val result = super.read(b, off, minOf(len, left))
            if (result > 0) left -= result
            return result
        }
        override fun skip(n: Long): Long {
            if (n <= 0) return 0
            if (left == 0) throw PreviewLimit()
            val result = super.skip(minOf(n, left.toLong()))
            left -= result.toInt()
            return result
        }
    }

    fun archive(input: InputStream): DocumentPreview {
        val entries = mutableListOf<String>()
        return try {
            ZipInputStream(LimitedInput(input)).use { zip ->
                var expanded = 0
                val buffer = ByteArray(4096)
                repeat(ENTRY_LIMIT) {
                    val entry = zip.nextEntry ?: return DocumentPreview(entries.toList(),
                        message = if (entries.isEmpty()) "No entries available for preview." else null)
                    val path = entry.name.map { if (it.isISOControl()) '?' else it }.joinToString("").take(180)
                    entries.add(path)
                    if (entry.size > EXPANDED_LIMIT - expanded) return DocumentPreview(entries.toList(), limited = true)
                    while (true) {
                        val remaining = EXPANDED_LIMIT - expanded
                        if (remaining == 0) return DocumentPreview(entries.toList(), limited = true)
                        val count = zip.read(buffer, 0, minOf(remaining, buffer.size))
                        if (count < 0) break
                        if (count == 0) return DocumentPreview(entries.toList(), limited = true)
                        expanded += count
                    }
                }
                DocumentPreview(entries.toList(), limited = true)
            }
        } catch (_: PreviewLimit) { DocumentPreview(entries.toList(), limited = true) }
        catch (_: IOException) { DocumentPreview(entries.toList(), message = "Archive preview unavailable. Import checks the complete archive.") }
        catch (_: IllegalArgumentException) { DocumentPreview(entries.toList(), message = "Archive preview unavailable. Import checks the complete archive.") }
    }

    fun text(input: InputStream): DocumentPreview = input.use {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (out.size() <= TEXT_LIMIT) {
            val count = it.read(buffer, 0, minOf(buffer.size, TEXT_LIMIT + 1 - out.size()))
            if (count <= 0) break
            out.write(buffer, 0, count)
        }
        val bytes = out.toByteArray()
        val clipped = bytes.copyOf(minOf(bytes.size, TEXT_LIMIT))
        val content = when {
            clipped.size >= 2 && clipped[0] == 0xff.toByte() && clipped[1] == 0xfe.toByte() ->
                String(clipped, 2, clipped.size - 2, Charsets.UTF_16LE)
            clipped.size >= 2 && clipped[0] == 0xfe.toByte() && clipped[1] == 0xff.toByte() ->
                String(clipped, 2, clipped.size - 2, Charsets.UTF_16BE)
            else -> String(clipped, Charsets.UTF_8).removePrefix("\uFEFF")
        }
        if (content.any { it.isISOControl() && it !in "\n\r\t" } || content.count { it == '\uFFFD' } > 2)
            DocumentPreview(message = "Binary or unsupported text preview.")
        else DocumentPreview(text = content.lineSequence().take(12).joinToString("\n"),
            limited = bytes.size > TEXT_LIMIT || content.lineSequence().drop(12).any())
    }
}
