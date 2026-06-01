package com.example.movieparadiso.ui

import com.example.movieparadiso.data.OnlineMovie

object StreamCatalog {

    private val onlineMovies = listOf(
        OnlineMovie(
            title = "The Seventh Seal",
            description = "A classic online stream from your R2 catalog.",
            streamUrl = "https://pub-222091578f1c433fa5e19df561fa1391.r2.dev/the-seventh-seal/TheSeventhSeal.avi"
        )
    )

    fun getOnlineMovies(): List<OnlineMovie> {
        return onlineMovies
    }

    fun getStreamUrlForMovie(title: String): String? {
        val normalizedTitle = title.trim().lowercase()

        return onlineMovies
            .firstOrNull { it.title.trim().lowercase() == normalizedTitle }
            ?.streamUrl
    }

    fun isOnlineStream(uri: String?): Boolean {
        return uri != null &&
                (uri.startsWith("http://") || uri.startsWith("https://"))
    }
}