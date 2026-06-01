package com.example.movieparadiso.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.movieparadiso.data.MovieDatabase
import com.example.movieparadiso.data.MovieEntity
import com.example.movieparadiso.databinding.ActivityMovieDetailsBinding
import com.example.movieparadiso.repository.MovieRepository
import com.example.movieparadiso.viewmodel.MovieViewModel
import com.example.movieparadiso.viewmodel.MovieViewModelFactory


class MovieDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailsBinding
    private lateinit var movieViewModel: MovieViewModel

    private var movieId: Int = -1
    private var currentMovie: MovieEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        setupViewModel()
        readMovieId()
        setupClickListeners()
        observeMovie()
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

    private fun readMovieId() {
        movieId = intent.getIntExtra("movie_id", -1)

        if (movieId == -1) {
            Toast.makeText(this, "Movie not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeMovie() {
        movieViewModel.getMovieById(movieId).observe(this) { movie ->
            if (movie == null) {
                Toast.makeText(this, "Movie was deleted", Toast.LENGTH_SHORT).show()
                finish()
                return@observe
            }

            currentMovie = movie
            showMovieData(movie)
        }
    }

    private fun showMovieData(movie: MovieEntity) {
        binding.tvDetailsTitle.text = movie.title
        binding.tvDetailsDirector.text = "Directed by ${movie.director}"
        binding.tvDetailsGenre.text = movie.genre
        binding.tvDetailsYear.text = movie.year
        binding.tvDetailsRating.text = "★ ${movie.rating}"
        binding.tvDetailsStatus.text = movie.status

        binding.tvDetailsFavorite.text = if (movie.isFavorite) {
            "Favorite"
        } else {
            "Library"
        }

        binding.tvDetailsNotes.text = if (movie.notes.isBlank()) {
            "No notes added."
        } else {
            movie.notes
        }

        if (movie.videoUri.isNullOrBlank()) {
            binding.tvVideoInfo.text = "No video attached"
            binding.btnPlayMovie.visibility = View.GONE
        } else {
            binding.tvVideoInfo.text = "Video attached"
            binding.btnPlayMovie.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEditMovie.setOnClickListener {
            val movie = currentMovie ?: return@setOnClickListener

            val intent = Intent(this, AddEditMovieActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        binding.btnDeleteMovie.setOnClickListener {
            confirmDelete()
        }

        binding.btnShareMovie.setOnClickListener {
            showShareOptions()
        }

        binding.btnPlayMovie.setOnClickListener {
            playMovie()
        }
    }

    private fun confirmDelete() {
        val movie = currentMovie ?: return

        AlertDialog.Builder(this)
            .setTitle("Delete movie")
            .setMessage("Are you sure you want to delete \"${movie.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                movieViewModel.deleteMovie(movie)
                Toast.makeText(this, "Movie deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareMovie() {
        val movie = currentMovie ?: return

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, movie.title)
            putExtra(Intent.EXTRA_TEXT, buildMovieShareText(movie))
        }

        startActivity(Intent.createChooser(shareIntent, "Share movie"))
    }

    private fun showShareOptions() {
        val options = arrayOf(
            "Share as text",
            "Show QR Code"
        )

        AlertDialog.Builder(this)
            .setTitle("Share movie")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareMovie()
                    1 -> openQrCode()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openQrCode() {
        val movie = currentMovie ?: return

        val intent = Intent(this, QrCodeActivity::class.java)
        intent.putExtra("qr_text", buildMovieShareText(movie))
        startActivity(intent)
    }

    private fun buildMovieShareText(movie: MovieEntity): String {
        return """
        Movie: ${movie.title}
        Director: ${movie.director}
        Genre: ${movie.genre}
        Year: ${movie.year}
        Status: ${movie.status}
        Rating: ${movie.rating}/5
        
        Notes:
        ${if (movie.notes.isBlank()) "No notes." else movie.notes}
    """.trimIndent()
    }

    private fun playMovie() {
        val movie = currentMovie ?: return

        if (movie.videoUri.isNullOrBlank()) {
            binding.tvVideoInfo.text = "No video attached"
            binding.btnPlayMovie.visibility = View.GONE
        } else {
            binding.tvVideoInfo.text = if (StreamConstants.isOnlineStream(movie.videoUri)) {
                "Online stream attached"
            } else {
                "Local video attached"
            }

            binding.btnPlayMovie.visibility = View.VISIBLE
        }

        val intent = Intent(this, VlcPlayerActivity::class.java)
        intent.putExtra("video_uri", movie.videoUri)
        intent.putExtra("movie_title", movie.title)
        startActivity(intent)
    }
}