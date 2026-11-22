package site.lxwis.codelab

/**
 * Cheap visible-window invalidation. Android Editable automatically shifts existing
 * spans when text is inserted or deleted outside the highlighted window.
 *
 * The viewport bounds are offsets from the previous snapshot. An edit before or
 * inside that window may move the visible offsets or alter a token, so it must
 * invalidate the highlight. An edit at/after the exclusive end cannot do either.
 */
internal fun shouldRehighlightVisible(windowStart: Int, windowEnd: Int, editStart: Int): Boolean =
    windowStart < 0 || windowEnd < windowStart || editStart < 0 || editStart < windowEnd
