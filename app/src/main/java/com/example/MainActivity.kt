package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.player.MusicsPlaybackService
import com.example.ui.MusicPlayerViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Start foreground service so ExoPlayer can live inside it
        val serviceIntent = Intent(this, MusicsPlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                startForegroundService(serviceIntent)
            } catch (e: Exception) {
                // Ignore background start restrictions if activity is not fully visible yet
                startService(serviceIntent)
            }
        } else {
            startService(serviceIntent)
        }

        setContent {
            MyApplicationTheme {
                val viewModel: MusicPlayerViewModel = viewModel()

                // Permission request launcher for MediaStore access
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.triggerSmartScan()
                    }
                }

                val requestStoragePermission = {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.triggerSmartScan()
                    } else {
                        permissionLauncher.launch(permission)
                    }
                }

                MainScreen(
                    viewModel = viewModel,
                    onRequestStoragePermission = requestStoragePermission
                )
            }
        }
    }
}
