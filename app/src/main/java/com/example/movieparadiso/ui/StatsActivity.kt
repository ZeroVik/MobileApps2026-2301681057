package com.example.movieparadiso.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.movieparadiso.data.MovieDatabase
import com.example.movieparadiso.databinding.ActivityStatsBinding
import com.example.movieparadiso.repository.MovieRepository
import com.example.movieparadiso.viewmodel.MovieViewModel
import com.example.movieparadiso.viewmodel.MovieViewModelFactory

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var movieViewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        setupViewModel()
        setupClickListeners()
        observeStats()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            FullscreenHelper.enableFullscreen(this)
        }
    }

    private fun setupViewModel() {
        val movieDao = MovieDatabase.getDatabase(applicationContext).movieDao()
        val repository = MovieRepository(movieDao)
        val factory = MovieViewModelFactory(repository)

        movieViewModel = ViewModelProvider(this, factory)[MovieViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.btnBackStats.setOnClickListener {
            finish()
        }
    }

    private fun observeStats() {
        movieViewModel.moviesCount.observe(this) { count ->
            binding.tvTotalMovies.text = "Total movies: $count"
        }

        movieViewModel.favoriteMoviesCount.observe(this) { count ->
            binding.tvFavoriteMovies.text = "Favorite movies: $count"
        }

        movieViewModel.getMoviesCountByStatus("To Watch").observe(this) { count ->
            binding.tvToWatch.text = "To Watch: $count"
        }

        movieViewModel.getMoviesCountByStatus("Watching").observe(this) { count ->
            binding.tvWatching.text = "Watching: $count"
        }

        movieViewModel.getMoviesCountByStatus("Watched").observe(this) { count ->
            binding.tvWatched.text = "Watched: $count"
        }
    }
}