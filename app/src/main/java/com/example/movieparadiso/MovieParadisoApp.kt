package com.example.movieparadiso

import android.app.Application
import com.example.movieparadiso.ui.ThemeManager

class MovieParadisoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        ThemeManager.applySavedTheme(this)
    }
}