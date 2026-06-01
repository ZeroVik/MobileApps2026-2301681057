package com.example.movieparadiso.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.movieparadiso.data.MovieDatabase
import com.example.movieparadiso.databinding.FragmentStatsBinding
import com.example.movieparadiso.repository.MovieRepository
import com.example.movieparadiso.viewmodel.MovieViewModel
import com.example.movieparadiso.viewmodel.MovieViewModelFactory

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var movieViewModel: MovieViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        observeStats()
    }

    private fun setupViewModel() {
        val movieDao = MovieDatabase.getDatabase(requireContext().applicationContext).movieDao()
        val repository = MovieRepository(movieDao)
        val factory = MovieViewModelFactory(repository)

        movieViewModel = ViewModelProvider(this, factory)[MovieViewModel::class.java]
    }

    private fun observeStats() {
        movieViewModel.moviesCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalMovies.text = count.toString()
        }

        movieViewModel.favoriteMoviesCount.observe(viewLifecycleOwner) { count ->
            binding.tvFavoriteMovies.text = count.toString()
        }

        movieViewModel.getMoviesCountByStatus("To Watch").observe(viewLifecycleOwner) { count ->
            binding.tvToWatch.text = count.toString()
        }

        movieViewModel.getMoviesCountByStatus("Watching").observe(viewLifecycleOwner) { count ->
            binding.tvWatching.text = count.toString()
        }

        movieViewModel.getMoviesCountByStatus("Watched").observe(viewLifecycleOwner) { count ->
            binding.tvWatched.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
