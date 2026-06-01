package com.example.movieparadiso

import com.example.movieparadiso.ui.StreamCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local unit tests for [StreamCatalog] – the catalog lookup and stream
 * detection logic. These run on the JVM (no device needed).
 */
class StreamCatalogTest {

    @Test
    fun catalog_isNotEmpty() {
        assertTrue(StreamCatalog.getOnlineMovies().isNotEmpty())
    }

    @Test
    fun getStreamUrl_withExactTitle_returnsMatchingUrl() {
        val movie = StreamCatalog.getOnlineMovies().first()

        assertEquals(movie.streamUrl, StreamCatalog.getStreamUrlForMovie(movie.title))
    }

    @Test
    fun getStreamUrl_isCaseAndWhitespaceInsensitive() {
        val movie = StreamCatalog.getOnlineMovies().first()
        val messyTitle = "   " + movie.title.uppercase() + "   "

        assertEquals(movie.streamUrl, StreamCatalog.getStreamUrlForMovie(messyTitle))
    }

    @Test
    fun getStreamUrl_withUnknownTitle_returnsNull() {
        assertNull(StreamCatalog.getStreamUrlForMovie("A Movie That Does Not Exist"))
    }

    @Test
    fun isOnlineStream_detectsHttpAndHttps() {
        assertTrue(StreamCatalog.isOnlineStream("http://example.com/video.mp4"))
        assertTrue(StreamCatalog.isOnlineStream("https://example.com/video.mp4"))
    }

    @Test
    fun isOnlineStream_rejectsLocalUriNullAndBlank() {
        assertFalse(StreamCatalog.isOnlineStream("content://media/external/video/42"))
        assertFalse(StreamCatalog.isOnlineStream(null))
        assertFalse(StreamCatalog.isOnlineStream(""))
    }
}