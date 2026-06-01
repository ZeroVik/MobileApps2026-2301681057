package com.example.movieparadiso.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.example.movieparadiso.R
import com.example.movieparadiso.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager.applySavedTheme(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        // The app is fully immersive, so prevent the bottom navigation from
        // applying system window insets as internal padding. Otherwise on real
        // devices the gesture-bar inset squeezes the icons to the top of the bar.
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { _, insets ->
            insets
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            openFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            FullscreenHelper.enableFullscreen(this)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    openFragment(HomeFragment())
                    true
                }

                R.id.nav_library -> {
                    openFragment(LibraryFragment())
                    true
                }

                R.id.nav_favorites -> {
                    openFragment(FavoritesFragment())
                    true
                }

                R.id.nav_stats -> {
                    openFragment(StatsFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}