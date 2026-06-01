package com.example.movieparadiso.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieparadiso.data.OnlineMovie
import com.example.movieparadiso.databinding.ItemOnlineMovieBinding

class OnlineMovieAdapter(
    private val onPlayClick: (OnlineMovie) -> Unit
) : RecyclerView.Adapter<OnlineMovieAdapter.OnlineMovieViewHolder>() {

    private val movies = mutableListOf<OnlineMovie>()

    fun submitList(newMovies: List<OnlineMovie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineMovieViewHolder {
        val binding = ItemOnlineMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return OnlineMovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnlineMovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    inner class OnlineMovieViewHolder(
        private val binding: ItemOnlineMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: OnlineMovie) {
            binding.tvOnlineMovieTitle.text = movie.title
            binding.tvOnlineMovieDescription.text = movie.description

            binding.btnPlayOnlineMovie.setOnClickListener {
                onPlayClick(movie)
            }

            binding.root.setOnClickListener {
                onPlayClick(movie)
            }
        }
    }
}