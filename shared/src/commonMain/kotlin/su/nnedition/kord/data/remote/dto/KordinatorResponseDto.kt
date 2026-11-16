package su.nnedition.kord.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class KordinatorResponseDto(
    val status: String,
    val version: String? = null,
    val type: String? = null,
    val serverVersion: String? = null,
    val openSubsonic: Boolean? = null,
    val error: KordinatorErrorDto? = null,
    val artists: ArtistsRootDto? = null,
    val artist: ArtistDetailDto? = null,
    val albumList2: AlbumList2Dto? = null,
    val album: AlbumDetailDto? = null,
    val song: SongDto? = null,
    val searchResult3: SearchResult3Dto? = null,
    val playlists: PlaylistsRootDto? = null,
    val playlist: PlaylistDetailDto? = null,
    val playQueue: PlayQueueDto? = null,
    val user: UserDto? = null,
    val users: UsersRootDto? = null,
    val musicFolders: MusicFoldersRootDto? = null,
    val scanStatus: ScanStatusDto? = null,
    val nowPlaying: NowPlayingRootDto? = null,
    val serverHealth: ServerHealthDto? = null,
    val starred2: Starred2Dto? = null,
    val lyrics: LyricsDto? = null
)
