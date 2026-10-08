package com.example.data.model

enum class SortOrder(val label: String) {
    DATE_ADDED("Date Added (Recent)"),
    ALPHABETICAL("Alphabetical (A-Z)"),
    FILE_SIZE("File Weight (Size)"),
    ARTIST("Artist (A-Z)"),
    DURATION("Duration (Length)")
}

enum class LibraryTab(val label: String) {
    ALL("All Tracks"),
    LOSSLESS("Hi-Res Lossless"),
    FAVORITES("Favorites"),
    ARTISTS("Artists"),
    ALBUMS("Albums")
}

data class ScanSettings(
    val bypass60SecondRule: Boolean = false,
    val includeVoiceNotesAndRingtones: Boolean = false
)
