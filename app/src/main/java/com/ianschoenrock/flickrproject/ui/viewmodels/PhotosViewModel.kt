package com.ianschoenrock.flickrproject.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ianschoenrock.networking.FlickrApi
import com.ianschoenrock.networking.models.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val flickrApi: FlickrApi
) : ViewModel() {

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun searchPhotos(query: String) {
        if(query.isBlank()) return

        viewModelScope.launch{
            _isLoading.value = true
            _errorMessage.value = null

            try{
                val response= flickrApi.searchPhotos(searchText = query)

                if(response.stat == "ok"){
                    _photos.value = response.photos.photo
                } else{
                    _errorMessage.value = "Search Failed"
                    _photos.value = emptyList()
                }
            } catch (e: Exception){
                _errorMessage.value = "Network Error: ${e.message}"
                Log.println(Log.ERROR, "API FAILURE", "Error: ${e.message}")
                _photos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPhotoUrl(photo: Photo, size: String = "m"): String {
        return "https://live.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_${size}.jpg"
    }
}