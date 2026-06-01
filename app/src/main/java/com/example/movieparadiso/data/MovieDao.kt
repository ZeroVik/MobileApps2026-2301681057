package com.example.movieparadiso.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MovieDao {

    @Insert
    suspend fun insertMovie(movie: MovieEntity)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies ORDER BY createdAt DESC")
    fun getAllMovies(): LiveData<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteMovies(): LiveData<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun getMovieById(movieId: Int): LiveData<MovieEntity>

    @Query("SELECT COUNT(*) FROM movies")
    fun getMoviesCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM movies WHERE isFavorite = 1")
    fun getFavoriteMoviesCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM movies WHERE status = :status")
    fun getMoviesCountByStatus(status: String): LiveData<Int>
}