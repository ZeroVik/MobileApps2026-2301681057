package com.example.movieparadiso

import com.example.movieparadiso.data.MovieEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local unit tests for the [MovieEntity] data model: default values and the
 * immutable update (`copy`) semantics used by the edit / favorite flows.
 */
class MovieEntityTest {

    private fun sampleMovie() = MovieEntity(
        title = "The Seventh Seal",
        director = "Ingmar Bergman",
        genre = "Drama",
        year = "1957",
        status = "To Watch",
        rating = 4.5f,
        notes = "A classic.",
        isFavorite = false,
        videoUri = null
    )

    @Test
    fun newMovie_appliesDefaultIdAndTimestamp() {
        val movie = sampleMovie()

        assertEquals(0, movie.id)
        assertTrue(movie.createdAt > 0L)
    }

    @Test
    fun copy_updatesStatusAndFavoriteWhileKeepingOtherFields() {
        val original = sampleMovie()

        val updated = original.copy(status = "Watched", isFavorite = true)

        assertEquals("Watched", updated.status)
        assertTrue(updated.isFavorite)
        // Untouched fields are preserved.
        assertEquals(original.title, updated.title)
        assertEquals(original.director, updated.director)
        assertEquals(original.createdAt, updated.createdAt)
    }

    @Test
    fun equality_holdsForIdenticalData_andBreaksOnChange() {
        val original = sampleMovie()
        val identical = original.copy()
        val different = original.copy(rating = 1.0f)

        assertEquals(original, identical)
        assertNotEquals(original, different)
    }
}