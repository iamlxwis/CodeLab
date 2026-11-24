package site.lxwis.codelab

internal data class ExplorerRow(val path: String, val folder: Boolean, val depth: Int)

/** Build the visible tree once per expansion/search change, not per row draw. */
internal fun explorerRows(files: List<String>, directories: List<String>, expanded: Set<String>,
    search: String = ""): List<ExplorerRow> {
    if (search.isNotBlank()) return files.filter { it.contains(search, ignoreCase = true) }
        .distinct().sorted().map { ExplorerRow(it, false, 0) }
    val allFolders = linkedSetOf<String>()
    fun ancestors(path: String) {
        var parent = path.substringBeforeLast('/', "")
        while (parent.isNotEmpty()) {
            allFolders.add(parent + "/")
            parent = parent.substringBeforeLast('/', "")
        }
    }
    directories.forEach {
        val path = it.trimEnd('/')
        if (path.isNotBlank()) { allFolders.add(path + "/"); ancestors(path) }
    }
    files.forEach(::ancestors)
    val byParent = allFolders.groupBy { it.trimEnd('/').substringBeforeLast('/', "").let { p -> if (p.isEmpty()) "" else p + "/" } }
    val fileParents = files.distinct().groupBy { it.substringBeforeLast('/', "").let { p -> if (p.isEmpty()) "" else p + "/" } }
    return buildList {
        fun visit(parent: String, depth: Int) {
            byParent[parent].orEmpty().sorted().forEach { folder ->
                add(ExplorerRow(folder, true, depth))
                if (folder in expanded) visit(folder, depth + 1)
            }
            fileParents[parent].orEmpty().sorted().forEach { add(ExplorerRow(it, false, depth)) }
        }
        visit("", 0)
    }
}
