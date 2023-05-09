package com.yrezgui.downloadreader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(folder: Folder) {
    var selectedFile by remember {
        mutableStateOf<TextFile?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download Reader", fontFamily = FontFamily.Serif) }
            )
        }
    ) { paddingValues ->
        if (folder is Folder.Loaded) {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                items(folder.items) { item ->
                    ListItem(
                        modifier = Modifier.clickable { selectedFile = item },
                        headlineContent = { Text(item.filename) },
                        supportingContent = { Text("${item.size} B") },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Note,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surfaceTint
                            )
                        }
                    )
                    Divider()
                }
            }
        }
    }

    if (selectedFile != null) {
        AlertDialog(
            onDismissRequest = {
                selectedFile = null
            }
        ) {
            var content by remember { mutableStateOf("") }

            LaunchedEffect(selectedFile) {
                withContext(Dispatchers.IO) {
                    content = selectedFile!!.content
                }
            }

            Surface(
                modifier = Modifier.size(200.dp),
                shape = MaterialTheme.shapes.large
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(content)
                    }
                }
            }
        }
    }
}