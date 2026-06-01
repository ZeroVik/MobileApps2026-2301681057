package com.example.movieparadiso.repository

import androidx.lifecycle.LiveData
import com.example.movieparadiso.data.MovieDao
import com.example.movieparadiso.data.MovieEntity

class MovieRepository(private val movieDao: MovieDao) {

    val allMovies: LiveData<List<MovieEntity>> = movieDao.getAllMovies()
    val favoriteMovies: LiveData<List<MovieEntity>> = movieDao.getFavoriteMovies()

    val moviesCount: LiveData<Int> = movieDao.getMoviesCount()
    val favoriteMoviesCount: LiveData<Int> = movieDao.getFavoriteMoviesCount()

    fun getMovieById(movieId: Int): LiveData<MovieEntity> {
        return movieDao.getMovieById(movieId)
    }

    fun getMoviesCountByStatus(status: String): LiveData<Int> {
        return movieDao.getMoviesCountByStatus(status)
    }

    suspend fun insertMovie(movie: MovieEntity) {
        movieDao.insertMovie(movie)
    }

    suspend fun updateMovie(movie: MovieEntity) {
        movieDao.updateMovie(movie)
    }

    suspend fun deleteMovie(movie: MovieEntity) {
        movieDao.deleteMovie(movie)
    }
}