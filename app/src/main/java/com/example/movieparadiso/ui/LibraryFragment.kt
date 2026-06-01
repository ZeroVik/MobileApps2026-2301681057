package com.example.movieparadiso.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieparadiso.R
import com.example.movieparadiso.adapter.MovieAdapter
import com.example.movieparadiso.data.MovieDatabase
import com.example.movieparadiso.data.MovieEntity
import com.example.movieparadiso.databinding.FragmentLibraryBinding
import com.example.movieparadiso.repository.MovieRepository
import com.example.movieparadiso.viewmodel.MovieViewModel
import com.example.movieparadiso.viewmodel.MovieViewModelFactory

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var movieViewModel: MovieViewModel
    private lateinit var movieAdapter: MovieAdapter

    private var allMovies: List<MovieEntity> = emptyList()
    private var selectedFilter: String = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeMovies()
        updateFilterButtons()
    }

    private fun setupViewModel() {
        val movieDao = MovieDatabase.getDatabase(requireContext().applicationContext).movieDao()
        val repository = MovieRepository(movieDao)
        val factory = MovieViewModelFactory(repository)

        movieViewModel = ViewModelProvider(this, factory)[MovieViewModel::class.java]
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter { movie ->
            val intent = Intent(requireContext(), MovieDetailsActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        binding.rvLibraryMovies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movieAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddMovie.setOnClickListener {
            val intent = Intent(requireContext(), AddEditMovieActivity::class.java)
            startActivity(intent)
        }

        binding.btnFilterAll.setOnClickListener {
            selectedFilter = "All"
            applyFilter()
            updateFilterButtons()
        }

        binding.btnFilterToWatch.setOnClickListener {
            selectedFilter = "To Watch"
            applyFilter()
            updateFilterButtons()
        }

        binding.btnFilterWatching.setOnClickListener {
            selectedFilter = "Watching"
            applyFilter()
            updateFilterButtons()
        }

        binding.btnFilterWatched.setOnClickListener {
            selectedFilter = "Watched"
            applyFilter()
            updateFilterButtons()
        }
    }

    private fun observeMovies() {
        movieViewModel.allMovies.observe(viewLifecycleOwner) { movies ->
            allMovies = movies
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filteredMovies = if (selectedFilter == "All") {
            allMovies
        } else {
            allMovies.filter { it.status == selectedFilter }
        }

        movieAdapter.submitList(filteredMovies)

        if (filteredMovies.isEmpty()) {
            binding.tvEmptyLibrary.visibility = View.VISIBLE
            binding.rvLibraryMovies.visibility = View.GONE
        } else {
            binding.tvEmptyLibrary.visibility = View.GONE
            binding.rvLibraryMovies.visibility = View.VISIBLE
        }
    }

    private fun updateFilterButtons() {
        val primary = ContextCompat.getColor(requireContext(), R.color.app_primary)
        val primaryText = ContextCompat.getColor(requireContext(), R.color.app_primary_text)
        val normal = ContextCompat.getColor(requireContext(), R.color.app_button_dark)
        val normalText = ContextCompat.getColor(requireContext(), R.color.app_button_dark_text)

        val buttons = mapOf(
            "All" to binding.btnFilterAll,
            "To Watch" to binding.btnFilterToWatch,
            "Watching" to binding.btnFilterWatching,
            "Watched" to binding.btnFilterWatched
        )

        buttons.forEach { (filter, button) ->
            if (filter == selectedFilter) {
                button.setBackgroundColor(primary)
                button.setTextColor(primaryText)
            } else {
                button.setBackgroundColor(normal)
                button.setTextColor(normalText)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::movieAdapter.isInitialized) {
            applyFilter()
            updateFilterButtons()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}