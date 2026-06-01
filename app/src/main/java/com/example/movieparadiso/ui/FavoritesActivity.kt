package com.example.movieparadiso.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieparadiso.adapter.MovieAdapter
import com.example.movieparadiso.data.MovieDatabase
import com.example.movieparadiso.databinding.ActivityFavoritesBinding
import com.example.movieparadiso.repository.MovieRepository
import com.example.movieparadiso.viewmodel.MovieViewModel
import com.example.movieparadiso.viewmodel.MovieViewModelFactory

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeFavoriteMovies()
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

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter { movie ->
            val intent = Intent(this, MovieDetailsActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        binding.rvFavoriteMovies.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = movieAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBackFavorites.setOnClickListener {
            finish()
        }
    }

    private fun observeFavoriteMovies() {
        movieViewModel.favoriteMovies.observe(this) { movies ->
            movieAdapter.submitList(movies)

            if (movies.isEmpty()) {
                binding.tvEmptyFavorites.visibility = View.VISIBLE
                binding.rvFavoriteMovies.visibility = View.GONE
            } else {
                binding.tvEmptyFavorites.visibility = View.GONE
                binding.rvFavoriteMovies.visibility = View.VISIBLE
            }
        }
    }
}