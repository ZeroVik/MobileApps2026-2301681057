package com.example.movieparadiso.ui

object StreamConstants {

    const val DEFAULT_ONLINE_STREAM_URL = "https://pub-222091578f1c433fa5e19df561fa1391.r2.dev/the-seventh-seal/TheSeventhSeal.avi"

    fun isOnlineStream(uri: String?): Boolean {
        return uri != null &&
                (uri.startsWith("http://") || uri.startsWith("https://"))
    }
}