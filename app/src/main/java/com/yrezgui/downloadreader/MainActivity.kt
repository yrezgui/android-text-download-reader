package com.yrezgui.downloadreader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yrezgui.downloadreader.ui.theme.DownloadReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var hasStorageAccess = MutableStateFlow(false)
    private val requestPermission = registerForActivityResult(RequestPermission()) { isGranted ->
        lifecycleScope.launch {
            hasStorageAccess.emit(isGranted)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                hasStorageAccess.emit(refreshStorageAccess())
            }
        }

        setContent {
            val hasAccess by hasStorageAccess.collectAsState()
            var folder by remember { mutableStateOf<Folder>(Folder.NotLoaded) }

            LaunchedEffect(hasAccess) {
                if (hasAccess) {
                    folder = Folder.Loaded(folderFetch())
                }
            }

            DownloadReaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (hasAccess) {
                        ReaderScreen(folder = folder)
                    } else {
                        PermissionScreen(requestPermission = ::requestStorageAccess)
                    }
                }
            }
        }
    }

    private fun refreshStorageAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                addCategory("android.intent.category.DEFAULT")
                data = Uri.parse(String.format("package:%s", applicationContext.packageName))
            }

            startActivity(intent)
        } else {
            requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

sealed class Folder {
    object NotLoaded : Folder()
    class Loaded(val items: List<TextFile>) : Folder()
}