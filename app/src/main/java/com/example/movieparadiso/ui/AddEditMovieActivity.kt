package com.example.movieparadiso.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.movieparadiso.data.MovieDatabase
import com.example.movieparadiso.data.MovieEntity
import com.example.movieparadiso.databinding.ActivityAddEditMovieBinding
import com.example.movieparadiso.repository.MovieRepository
import com.example.movieparadiso.viewmodel.MovieViewModel
import com.example.movieparadiso.viewmodel.MovieViewModelFactory

class AddEditMovieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditMovieBinding
    private lateinit var movieViewModel: MovieViewModel

    private var selectedVideoUri: String? = null
    private var currentMovieId: Int = -1
    private var existingCreatedAt: Long = System.currentTimeMillis()

    private val movieStatuses = listOf(
        "To Watch",
        "Watching",
        "Watched"
    )

    private val videoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                selectedVideoUri = uri.toString()
                binding.tvSelectedVideo.text = "Local video selected"

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Some providers do not support persistable permissions.
                }
            } else {
                Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        setupViewModel()
        setupStatusSpinner()
        readIntentData()
        setupClickListeners()
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

    private fun setupStatusSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            movieStatuses
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerStatus.adapter = adapter
    }

    private fun readIntentData() {
        currentMovieId = intent.getIntExtra("movie_id", -1)

        if (currentMovieId != -1) {
            binding.tvScreenTitle.text = "Edit Movie"
            binding.btnSaveMovie.text = "Update Movie"

            movieViewModel.getMovieById(currentMovieId).observe(this) { movie ->
                if (movie != null) {
                    fillMovieData(movie)
                }
            }
        } else {
            binding.tvScreenTitle.text = "Add Movie"
            binding.btnSaveMovie.text = "Save Movie"
            binding.spinnerStatus.setSelection(0)
        }
    }

    private fun fillMovieData(movie: MovieEntity) {
        binding.etTitle.setText(movie.title)
        binding.etDirector.setText(movie.director)
        binding.etGenre.setText(movie.genre)
        binding.etYear.setText(movie.year)
        binding.etRating.setText(movie.rating.toString())
        binding.etNotes.setText(movie.notes)
        binding.cbFavorite.isChecked = movie.isFavorite

        val statusIndex = movieStatuses.indexOf(movie.status)
        binding.spinnerStatus.setSelection(
            if (statusIndex >= 0) statusIndex else 0
        )

        selectedVideoUri = movie.videoUri
        existingCreatedAt = movie.createdAt

        binding.tvSelectedVideo.text = if (movie.videoUri.isNullOrBlank()) {
            "No local video selected"
        } else {
            "Local video selected"
        }
    }

    private fun setupClickListeners() {
        binding.btnChooseVideo.setOnClickListener {
            videoPickerLauncher.launch(arrayOf("video/*"))
        }

        binding.btnSaveMovie.setOnClickListener {
            saveMovie()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveMovie() {
        val title = binding.etTitle.text.toString().trim()
        val director = binding.etDirector.text.toString().trim()
        val genre = binding.etGenre.text.toString().trim()
        val year = binding.etYear.text.toString().trim()
        val status = binding.spinnerStatus.selectedItem.toString()
        val ratingText = binding.etRating.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        val isFavorite = binding.cbFavorite.isChecked

        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            return
        }

        if (director.isEmpty()) {
            binding.etDirector.error = "Director is required"
            return
        }

        if (genre.isEmpty()) {
            binding.etGenre.error = "Genre is required"
            return
        }

        if (year.isEmpty()) {
            binding.etYear.error = "Year is required"
            return
        }

        val yearNumber = year.toIntOrNull()

        if (yearNumber == null || yearNumber < 1888 || yearNumber > 2100) {
            binding.etYear.error = "Enter a valid movie year"
            return
        }

        val rating = ratingText.toFloatOrNull()

        if (rating == null || rating < 0f || rating > 5f) {
            binding.etRating.error = "Rating must be between 0 and 5"
            return
        }

        val movie = MovieEntity(
            id = if (currentMovieId == -1) 0 else currentMovieId,
            title = title,
            director = director,
            genre = genre,
            year = year,
            status = status,
            rating = rating,
            notes = notes,
            isFavorite = isFavorite,
            videoUri = selectedVideoUri,
            createdAt = if (currentMovieId == -1) {
                System.currentTimeMillis()
            } else {
                existingCreatedAt
            }
        )

        if (currentMovieId == -1) {
            movieViewModel.insertMovie(movie)
            Toast.makeText(this, "Movie added", Toast.LENGTH_SHORT).show()
        } else {
            movieViewModel.updateMovie(movie)
            Toast.makeText(this, "Movie updated", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}