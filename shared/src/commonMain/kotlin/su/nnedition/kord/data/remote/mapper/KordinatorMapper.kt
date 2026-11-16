package su.nnedition.kord.data.remote.mapper

import su.nnedition.kord.data.model.Album
import su.nnedition.kord.data.model.Artist
import su.nnedition.kord.data.model.MusicFolder
import su.nnedition.kord.data.model.NowPlayingItem
import su.nnedition.kord.data.model.PlayQueue
import su.nnedition.kord.data.model.Playlist
import su.nnedition.kord.data.model.ScanStatus
import su.nnedition.kord.data.model.SearchResult
import su.nnedition.kord.data.model.ServerHealthComponent
import su.nnedition.kord.data.model.ServerHealthInfo
import su.nnedition.kord.data.model.Track
import su.nnedition.kord.data.model.User
import su.nnedition.kord.data.remote.ServerConfig
import su.nnedition.kord.data.remote.dto.AlbumDetailDto
import su.nnedition.kord.data.remote.dto.AlbumDto
import su.nnedition.kord.data.remote.dto.ArtistDetailDto
import su.nnedition.kord.data.remote.dto.ArtistDto
import su.nnedition.kord.data.remote.dto.MusicFolderDto
import su.nnedition.kord.data.remote.dto.NowPlayingEntryDto
import su.nnedition.kord.data.remote.dto.PlayQueueDto
import su.nnedition.kord.data.remote.dto.PlaylistDetailDto
import su.nnedition.kord.data.remote.dto.PlaylistDto
import su.nnedition.kord.data.remote.dto.ScanStatusDto
import su.nnedition.kord.data.remote.dto.SearchResult3Dto
import su.nnedition.kord.data.remote.dto.ServerHealthDto
import su.nnedition.kord.data.remote.dto.SongDto
import su.nnedition.kord.data.remote.dto.UserDto

class KordinatorMapper(
    private val configProvider: () -> ServerConfig
) {
    fun toTrack(dto: SongDto): Track {
        return Track(
            id = dto.id,
            title = dto.title ?: "Без названия",
            artist = dto.artist ?: "Неизвестный исполнитель",
            artistId = dto.artistId,
            album = dto.album ?: "Без альбома",
            albumId = dto.albumId,
            durationSeconds = dto.duration ?: 0L,
            trackNumber = dto.track,
            discNumber = dto.discNumber,
            year = dto.year,
            genre = dto.genre,
            coverArtUrl = dto.coverArt?.let { getCoverArtUrl(it) },
            streamUrl = getStreamUrl(dto.id),
            bitRate = dto.bitRate,
            contentType = dto.contentType,
            accentColor = dto.accentColor
        )
    }

    fun toAlbum(dto: AlbumDto): Album {
        return Album(
            id = dto.id,
            title = dto.title ?: dto.name ?: "Без названия",
            artist = dto.artist ?: "Неизвестный исполнитель",
            artistId = dto.artistId,
            coverArtUrl = dto.coverArt?.let { getCoverArtUrl(it) },
            songCount = dto.songCount ?: 0,
            durationSeconds = dto.duration ?: 0L,
            year = dto.year,
            genre = dto.genre,
            accentColor = dto.accentColor,
            tracks = emptyList()
        )
    }

    fun toAlbum(dto: AlbumDetailDto): Album {
        return Album(
            id = dto.id,
            title = dto.title ?: dto.name ?: "Без названия",
            artist = dto.artist ?: "Неизвестный исполнитель",
            artistId = dto.artistId,
            coverArtUrl = dto.coverArt?.let { getCoverArtUrl(it) },
            songCount = dto.songCount ?: dto.song.size,
            durationSeconds = dto.duration ?: dto.song.sumOf { it.duration ?: 0L },
            year = dto.year,
            genre = dto.genre,
            accentColor = dto.accentColor,
            tracks = dto.song.map { toTrack(it) }
        )
    }

    fun toArtist(dto: ArtistDto): Artist {
        return Artist(
            id = dto.id,
            name = dto.name,
            albumCount = dto.albumCount ?: 0,
            coverArtUrl = (dto.coverArt ?: dto.artistImageUrl)?.let { getCoverArtUrl(it) },
            albums = emptyList()
        )
    }

    fun toArtist(dto: ArtistDetailDto): Artist {
        return Artist(
            id = dto.id,
            name = dto.name,
            albumCount = dto.albumCount ?: dto.album.size,
            coverArtUrl = (dto.coverArt ?: dto.artistImageUrl)?.let { getCoverArtUrl(it) },
            albums = dto.album.map { toAlbum(it) }
        )
    }

    fun toSearchResult(dto: SearchResult3Dto): SearchResult {
        return SearchResult(
            artists = dto.artist.map { toArtist(it) },
            albums = dto.album.map { toAlbum(it) },
            tracks = dto.song.map { toTrack(it) }
        )
    }

    fun toPlaylist(dto: PlaylistDto): Playlist {
        return Playlist(
            id = dto.id,
            name = dto.name,
            comment = dto.comment,
            owner = dto.owner,
            isPublic = dto.public ?: false,
            songCount = dto.songCount ?: 0,
            durationSeconds = dto.duration?.toLong() ?: 0L,
            created = dto.created,
            changed = dto.changed,
            coverArtUrl = dto.coverArt?.let { getCoverArtUrl(it) },
            tracks = emptyList()
        )
    }

    fun toPlaylist(dto: PlaylistDetailDto): Playlist {
        return Playlist(
            id = dto.id,
            name = dto.name,
            comment = dto.comment,
            owner = dto.owner,
            isPublic = dto.public ?: false,
            songCount = dto.songCount ?: dto.entry.size,
            durationSeconds = dto.duration?.toLong() ?: dto.entry.sumOf { it.duration ?: 0L },
            created = dto.created,
            changed = dto.changed,
            coverArtUrl = dto.coverArt?.let { getCoverArtUrl(it) },
            tracks = dto.entry.map { toTrack(it) }
        )
    }

    fun toPlayQueue(dto: PlayQueueDto): PlayQueue {
        return PlayQueue(
            currentTrackId = dto.current?.toString(),
            positionMs = dto.position ?: 0L,
            username = dto.username,
            changed = dto.changed,
            changedBy = dto.changedBy,
            tracks = dto.entry.map { toTrack(it) }
        )
    }

    fun toUser(dto: UserDto): User {
        return User(
            username = dto.username,
            email = dto.email,
            isAdmin = dto.adminRole || dto.username.equals("admin", ignoreCase = true),
            canStream = dto.streamRole,
            canDownload = dto.downloadRole,
            canUpload = dto.uploadRole,
            canSettings = dto.settingsRole,
            allowedFolderIds = emptyList(),
            maxBitRate = dto.maxBitRate
        )
    }

    fun toMusicFolder(dto: MusicFolderDto): MusicFolder {
        return MusicFolder(
            id = dto.id,
            name = dto.name,
            path = dto.path
        )
    }

    fun toScanStatus(dto: ScanStatusDto): ScanStatus {
        return ScanStatus(
            isScanning = dto.scanning,
            scannedCount = dto.count
        )
    }

    fun toNowPlayingItem(dto: NowPlayingEntryDto): NowPlayingItem {
        return NowPlayingItem(
            username = dto.username ?: "Неизвестный",
            title = dto.title ?: "Без названия",
            artist = dto.artist ?: "Неизвестный исполнитель",
            album = dto.album ?: "",
            playerName = dto.playerName ?: "Клиент",
            minutesAgo = dto.minutesAgo ?: 0,
            coverArtUrl = dto.coverArt?.let { getCoverArtUrl(it) }
        )
    }

    fun toServerHealthInfo(dto: ServerHealthDto): ServerHealthInfo {
        val isUp = dto.status.equals("UP", ignoreCase = true)
        val components = dto.component.map { comp ->
            ServerHealthComponent(
                name = comp.name,
                status = comp.status,
                isUp = comp.status.equals("UP", ignoreCase = true)
            )
        }
        return ServerHealthInfo(
            status = dto.status,
            isUp = isUp,
            components = components
        )
    }

    fun getStreamUrl(trackId: String): String {
        val config = configProvider()
        val cleanBase = config.baseUrl.trim().trimEnd('/')
        val authParams = buildAuthQueryParams(config)
        return "$cleanBase/rest/stream?id=$trackId&$authParams"
    }

    fun getCoverArtUrl(coverArtId: String, size: Int? = null): String {
        val config = configProvider()
        val cleanBase = config.baseUrl.trim().trimEnd('/')
        val authParams = buildAuthQueryParams(config)
        val sizeParam = if (size != null) "&size=$size" else ""
        return "$cleanBase/rest/getCoverArt?id=$coverArtId&$authParams$sizeParam"
    }

    private fun buildAuthQueryParams(config: ServerConfig): String {
        val username = config.username
        val password = config.token.orEmpty()
        return "u=$username&p=$password&v=1.15.0&c=kord&f=json"
    }
}
