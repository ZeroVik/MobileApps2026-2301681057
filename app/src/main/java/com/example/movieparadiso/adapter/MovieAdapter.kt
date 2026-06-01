package com.example.movieparadiso.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieparadiso.data.MovieEntity
import com.example.movieparadiso.databinding.ItemMovieBinding
import android.view.View

class MovieAdapter(
    private val onMovieClick: (MovieEntity) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    private val movies = mutableListOf<MovieEntity>()

    fun submitList(newMovies: List<MovieEntity>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieEntity) {
            binding.tvMovieTitle.text = movie.title
            binding.tvMovieDirector.text = movie.director
            binding.tvMovieGenre.text = movie.genre
            binding.tvMovieStatus.text = movie.status
            binding.tvMovieRating.text = "★ ${movie.rating}"

            binding.tvFavorite.visibility = if (movie.isFavorite) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.tvFavorite.text = "★ Favorite"

            binding.root.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }
}