package com.ndhunju.relay.util

import android.database.Cursor

/**
 * Returns String value for passed [column] if it exists.
 * Otherwise, empty string is returned
 */
fun Cursor.getStringForColumn(column: String): String {
    val columnIndex = getColumnIndex(column)
    if (columnIndex >= 0) {
        return getString(columnIndex)
    }

    return ""
}