package su.nnedition.kord.data.model

enum class AlbumListType(val value: String) {
    RECENT("recent"),
    FREQUENT("frequent"),
    RANDOM("random"),
    NEWEST("newest"),
    HIGHEST("highest"),
    STARRED("starred"),
    ALPHABETICAL_BY_NAME("alphabeticalByName"),
    ALPHABETICAL_BY_ARTIST("alphabeticalByArtist")
}
