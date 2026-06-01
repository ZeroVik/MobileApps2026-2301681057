package com.example.movieparadiso.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieparadiso.adapter.MovieAdapter
import com.example.movieparadiso.data.MovieDatabase
import com.example.movieparadiso.databinding.ActivityMainBinding
import com.example.movieparadiso.repository.MovieRepository
import com.example.movieparadiso.viewmodel.MovieViewModel
import com.example.movieparadiso.viewmodel.MovieViewModelFactory
import kotlin.jvm.java
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var movieViewModel: MovieViewModel
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        updateThemeButtonText()
        observeMovies()
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

        binding.rvMovies.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = movieAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddMovie.setOnClickListener {
            val intent = Intent(this, AddEditMovieActivity::class.java)
            startActivity(intent)
        }

        binding.btnFavorites.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        binding.btnStats.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        binding.btnTheme.setOnClickListener {
            showThemeDialog()
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("System Default", "Light", "Dark")

        val checkedItem = when (ThemeManager.getSavedTheme(this)) {
            ThemeManager.MODE_LIGHT -> 1
            ThemeManager.MODE_DARK -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle("Choose theme")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                when (which) {
                    0 -> ThemeManager.saveTheme(this, ThemeManager.MODE_SYSTEM)
                    1 -> ThemeManager.saveTheme(this, ThemeManager.MODE_LIGHT)
                    2 -> ThemeManager.saveTheme(this, ThemeManager.MODE_DARK)
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateThemeButtonText() {
        binding.btnTheme.text = ThemeManager.getThemeLabel(this)
    }

    private fun observeMovies() {
        movieViewModel.allMovies.observe(this) { movies ->
            movieAdapter.submitList(movies)

            if (movies.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvMovies.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvMovies.visibility = View.VISIBLE
            }
        }
    }
}