package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    fun getTracksSortedByDate(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun getTracksSortedByName(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY sizeBytes DESC")
    fun getTracksSortedBySize(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY artist COLLATE NOCASE ASC, title COLLATE NOCASE ASC")
    fun getTracksSortedByArtist(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY durationMs DESC")
    fun getTracksSortedByDuration(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isLossless = 1 OR format IN ('FLAC', 'WAV') ORDER BY title COLLATE NOCASE ASC")
    fun getLosslessTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM tracks")
    suspend fun clearAll()
}
