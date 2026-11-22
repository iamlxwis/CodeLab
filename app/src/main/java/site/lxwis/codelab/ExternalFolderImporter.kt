package site.lxwis.codelab

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Copies a user-selected SAF folder into a NEW app-private project.
 * It never modifies the external folder or holds a persistent tree permission.
 */
internal object ExternalFolderImporter {
    private const val MAX_FILES = 1024
    private const val MAX_TOTAL = 16 * 1024 * 1024
    private const val MAX_DEPTH = 16

    fun validSegment(name: String): Boolean = name.isNotBlank() && name.length <= 100 &&
        name != "." && name != ".." &&
        name.none { it == '/' || it == '\\' || it == ':' || it.isISOControl() }

    fun archive(context: Context, treeUri: Uri): ByteArray {
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IOException("Selected folder cannot be opened.")
        if (!root.isDirectory) throw IOException("Select a folder instead of a file.")
        val seen = mutableSetOf<String>()
        var count = 0
        var expanded = 0
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            fun visit(folder: DocumentFile, prefix: String, depth: Int) {
                if (depth >= MAX_DEPTH) throw IOException("Folder is too deeply nested.")
                for (child in folder.listFiles()) {
                    val name = child.name ?: throw IOException("File has no name.")
                    if (!validSegment(name)) throw IOException("Invalid file name in selected folder.")
                    if (name in listOf(".git", ".codelab")) continue
                    val path = prefix + name
                    if (path.length > 512) throw IOException("File path is too long.")
                    if (!seen.add(child.uri.toString()))
                        throw IOException("Folder provider returned a cyclic directory reference.")
                    if (++count > MAX_FILES)
                        throw IOException("Selected folder exceeds 1024 files and folders.")
                    if (child.isDirectory) {
                        zip.putNextEntry(ZipEntry(path + "/"))
                        zip.closeEntry()
                        visit(child, path + "/", depth + 1)
                    } else if (child.isFile) {
                        val maxFileSize = ProjectStore.importLimitFor(path)
                        if (child.length() > maxFileSize)
                            throw IOException("File exceeds its supported size: " + path)
                        zip.putNextEntry(ZipEntry(path))
                        val stream = context.contentResolver.openInputStream(child.uri)
                            ?: throw IOException("Cannot read " + path)
                        stream.use { input ->
                            val buffer = ByteArray(8192)
                            var length = 0
                            while (true) {
                                val size = input.read(buffer)
                                if (size < 0) break
                                length += size
                                expanded += size
                                if (length > maxFileSize || expanded > MAX_TOTAL)
                                    throw IOException("Folder exceeds import size limits.")
                                zip.write(buffer, 0, size)
                            }
                        }
                        zip.closeEntry()
                    }
                }
            }
            visit(root, "", 0)
        }
        if (output.size() > MAX_TOTAL + 1024 * 1024)
            throw IOException("Compressed folder is too large.")
        return output.toByteArray()
    }

    fun copyToProject(context: Context, treeUri: Uri, name: String, store: ProjectStore) {
        val bytes = archive(context, treeUri)
        store.importZip(name, ByteArrayInputStream(bytes))
    }
}
