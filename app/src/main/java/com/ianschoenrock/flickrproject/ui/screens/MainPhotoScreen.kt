package com.ianschoenrock.flickrproject.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ianschoenrock.flickrproject.ui.viewmodels.PhotosViewModel

@Composable
fun MainPhotoScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotosViewModel = hiltViewModel()
){
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.searchPhotos("cats")
    }
    LaunchedEffect(photos) {
        println(photos)
    }

    Column {
        when{
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null-> {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }

            photos.isNotEmpty() -> {
                LazyColumn {
                    items(photos) { photo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Title: ${photo.title}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "ID: ${photo.id}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Owner: ${photo.owner}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "URL: ${viewModel.getPhotoUrl(photo)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}