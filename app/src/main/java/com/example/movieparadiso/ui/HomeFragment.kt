package com.example.movieparadiso.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieparadiso.adapter.OnlineMovieAdapter
import com.example.movieparadiso.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var onlineMovieAdapter: OnlineMovieAdapter
    private val onlineMovies = StreamCatalog.getOnlineMovies()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupHero()
        setupOnlineMovies()
    }

    private fun setupHero() {
        val featuredMovie = onlineMovies.firstOrNull()

        if (featuredMovie == null) {
            binding.tvHeroTitle.text = "No Online Streams"
            binding.tvHeroSubtitle.text = "Add online streams in StreamCatalog."
            binding.btnHeroPlay.visibility = View.GONE
            binding.btnHeroDetails.visibility = View.GONE
            return
        }

        binding.tvHeroTitle.text = featuredMovie.title
        binding.tvHeroSubtitle.text = featuredMovie.description

        binding.btnHeroPlay.setOnClickListener {
            playOnlineMovie(featuredMovie)
        }

        binding.btnHeroDetails.setOnClickListener {
            playOnlineMovie(featuredMovie)
        }
    }

    private fun setupOnlineMovies() {
        onlineMovieAdapter = OnlineMovieAdapter { movie ->
            playOnlineMovie(movie)
        }

        binding.rvOnlineMovies.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = onlineMovieAdapter
        }

        onlineMovieAdapter.submitList(onlineMovies)
    }

    private fun playOnlineMovie(movie: com.example.movieparadiso.data.OnlineMovie) {
        val intent = Intent(requireContext(), VlcPlayerActivity::class.java)
        intent.putExtra("video_uri", movie.streamUrl)
        intent.putExtra("movie_title", movie.title)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}