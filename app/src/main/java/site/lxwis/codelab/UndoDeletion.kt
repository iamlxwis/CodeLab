package site.lxwis.codelab

data class UndoDeletion(val id: String, val label: String)

/**
 * Identify only the trash record created by the current operation.
 * Never undo an unrelated older deletion if a write fails or races.
 */
internal fun newTrashItem(
    before: List<ProjectStore.TrashItem>,
    after: List<ProjectStore.TrashItem>,
    project: String,
    path: String
): ProjectStore.TrashItem? {
    val existing = before.mapTo(HashSet()) { it.id }
    return after.firstOrNull {
        it.id !in existing && it.project == project && it.path == path
    }
}
