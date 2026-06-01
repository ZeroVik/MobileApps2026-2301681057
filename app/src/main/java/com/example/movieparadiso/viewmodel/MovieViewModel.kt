package com.example.movieparadiso.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieparadiso.data.MovieEntity
import com.example.movieparadiso.repository.MovieRepository
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    val allMovies: LiveData<List<MovieEntity>> = repository.allMovies
    val favoriteMovies: LiveData<List<MovieEntity>> = repository.favoriteMovies

    val moviesCount: LiveData<Int> = repository.moviesCount
    val favoriteMoviesCount: LiveData<Int> = repository.favoriteMoviesCount

    fun getMovieById(movieId: Int): LiveData<MovieEntity> {
        return repository.getMovieById(movieId)
    }

    fun getMoviesCountByStatus(status: String): LiveData<Int> {
        return repository.getMoviesCountByStatus(status)
    }

    fun insertMovie(movie: MovieEntity) {
        viewModelScope.launch {
            repository.insertMovie(movie)
        }
    }

    fun updateMovie(movie: MovieEntity) {
        viewModelScope.launch {
            repository.updateMovie(movie)
        }
    }

    fun deleteMovie(movie: MovieEntity) {
        viewModelScope.launch {
            repository.deleteMovie(movie)
        }
    }
}