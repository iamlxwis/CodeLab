package site.lxwis.codelab

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import java.io.File

/** Imported fonts are local-only, size-bounded and validated before replacing the active font. */
internal object ImportedFonts {
    private fun file(context: Context, editor: Boolean) =
        File(context.filesDir, if (editor) "custom-editor-font.ttf" else "custom-ui-font.ttf")
    fun load(context: Context, editor: Boolean): Typeface? = runCatching {
        val path = file(context, editor)
        if (!path.isFile) null else Typeface.createFromFile(path)
    }.getOrNull()
    fun install(context: Context, uri: Uri, editor: Boolean): Boolean {
        val target = file(context, editor)
        val temporary = File(context.filesDir, target.name + ".tmp")
        return try {
            val limit = 5 * 1024 * 1024
            context.contentResolver.openInputStream(uri)?.use { input ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        total += n
                        require(total <= limit)
                        output.write(buffer, 0, n)
                    }
                    require(total >= 12)
                }
            } ?: return false
            // Reject arbitrary non-font data before calling native parser.
            val header = temporary.inputStream().use { stream -> ByteArray(4).also { require(stream.read(it) == 4) } }
            val signature = String(header, Charsets.ISO_8859_1)
            require(signature in listOf("OTTO", "ttcf", "true") || header.contentEquals(byteArrayOf(0,1,0,0)))
            Typeface.createFromFile(temporary)
            temporary.renameTo(target)
        } catch (_: Exception) { false }
        finally { temporary.delete() }
    }
}
