package site.lxwis.codelab

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import java.io.File

/** Local, independently retained font assets. A saved theme never points at a mutable replacement. */
internal object ImportedFonts {
    private fun file(context: Context, editor: Boolean, revision: Long) =
        File(context.filesDir, if (revision > 0)
            "custom-${if (editor) "editor" else "ui"}-font-$revision.ttf"
            else if (editor) "custom-editor-font.ttf" else "custom-ui-font.ttf")
    fun load(context: Context, editor: Boolean, revision: Long = 0L): Typeface? = runCatching {
        val path = file(context, editor, revision)
        val legacy = file(context, editor, 0L)
        val selected = if (path.isFile) path else if (legacy.isFile) legacy else null
        selected?.let { Typeface.createFromFile(it) }
    }.getOrNull()
    @Synchronized
    fun install(context: Context, uri: Uri, editor: Boolean): Long? {
        var revision = System.currentTimeMillis().coerceAtLeast(1L)
        while (file(context, editor, revision).exists()) revision++
        val target = file(context, editor, revision)
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
            } ?: return null
            val header = temporary.inputStream().use { stream -> ByteArray(4).also { require(stream.read(it) == 4) } }
            val signature = String(header, Charsets.ISO_8859_1)
            require(signature in listOf("OTTO", "ttcf", "true") || header.contentEquals(byteArrayOf(0,1,0,0)))
            Typeface.createFromFile(temporary)
            if (temporary.renameTo(target)) revision else null
        } catch (_: Exception) { null }
        finally { temporary.delete() }
    }
}
