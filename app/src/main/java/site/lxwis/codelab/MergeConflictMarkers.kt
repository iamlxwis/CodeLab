package site.lxwis.codelab

/**
 * Resolves conventional two-way Git conflict markers in an open source file.
 * It never changes Git's index or performs a merge; only the editor's text changes.
 * Non-conflict text and original line endings are preserved exactly.
 */
internal object MergeConflictMarkers {
    enum class Choice { OURS, THEIRS, BOTH }

    data class Hunk(
        val line: Int,
        val oursLabel: String,
        val theirsLabel: String,
        val ours: String,
        val theirs: String,
        val start: Int,
        val endExclusive: Int
    )

    private data class SourceLine(val raw: String, val marker: String, val start: Int)

    private fun sourceLines(source: String): List<SourceLine> {
        val result = mutableListOf<SourceLine>()
        var start = 0
        while (start < source.length) {
            val newline = source.indexOf('\n', start)
            val end = if (newline < 0) source.length else newline + 1
            val raw = source.substring(start, end)
            result.add(SourceLine(raw, raw.removeSuffix("\n").removeSuffix("\r"), start))
            start = end
        }
        return result
    }

    fun parse(source: String): List<Hunk> {
        require(source.length <= 2_000_000) { "Conflict preview supports files up to 2 MB." }
        val lines = sourceLines(source)
        val hunks = mutableListOf<Hunk>()
        var i = 0
        while (i < lines.size) {
            val marker = lines[i].marker
            if (marker.startsWith("<<<<<<< ")) {
                require(hunks.size < 100) { "Resolve at most 100 conflicts at a time." }
                val start = i
                var divider = -1
                var end = -1
                var j = i + 1
                while (j < lines.size) {
                    val current = lines[j].marker
                    require(!current.startsWith("<<<<<<< ") &&
                        !current.startsWith("||||||| ")) {
                        "Nested or three-way conflict at line " + (j + 1) + "; resolve it manually."
                    }
                    if (current == "=======") {
                        require(divider < 0) { "Multiple dividers at line " + (j + 1) + "." }
                        divider = j
                    } else if (current.startsWith(">>>>>>> ")) {
                        end = j
                        break
                    }
                    j++
                }
                require(divider > start && end > divider) {
                    "Incomplete conflict starting at line " + (start + 1) + "."
                }
                hunks.add(Hunk(
                    line = start + 1,
                    oursLabel = marker.removePrefix("<<<<<<< ").take(80),
                    theirsLabel = lines[end].marker.removePrefix(">>>>>>> ").take(80),
                    ours = lines.subList(start + 1, divider).joinToString("") { it.raw },
                    theirs = lines.subList(divider + 1, end).joinToString("") { it.raw },
                    start = lines[start].start,
                    endExclusive = lines[end].start + lines[end].raw.length
                ))
                i = end + 1
            } else {
                require(marker != "=======" && !marker.startsWith(">>>>>>> ") &&
                    !marker.startsWith("||||||| ")) {
                    "Unexpected conflict marker at line " + (i + 1) + "."
                }
                i++
            }
        }
        return hunks
    }

    fun resolve(source: String, choices: List<Choice>): String {
        val hunks = parse(source)
        require(hunks.size == choices.size) { "Conflict choices are out of date." }
        if (hunks.isEmpty()) return source
        val result = StringBuilder(source.length)
        var offset = 0
        hunks.forEachIndexed { index, hunk ->
            result.append(source, offset, hunk.start)
            result.append(when (choices[index]) {
                Choice.OURS -> hunk.ours
                Choice.THEIRS -> hunk.theirs
                Choice.BOTH -> hunk.ours + hunk.theirs
            })
            offset = hunk.endExclusive
        }
        result.append(source, offset, source.length)
        return result.toString()
    }
}
