package com.ianschoenrock.flickrproject.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ianschoenrock.networking.FlickrApi
import com.ianschoenrock.networking.models.details.PhotoInfo
import com.ianschoenrock.networking.models.search.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val flickrApi: FlickrApi
) : ViewModel() {

    companion object { private const val PER_PAGE = 20 }

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos

    private val _selectedPhoto = MutableStateFlow<Photo?>(null)
    val selectedPhoto: StateFlow<Photo?> = _selectedPhoto

    private val _selectedPhotoInfo = MutableStateFlow<PhotoInfo?>(null)
    val selectedPhotoInfo: StateFlow<PhotoInfo?> = _selectedPhotoInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore


    private val _endReached = MutableStateFlow(false)
    val endReached: StateFlow<Boolean> = _endReached

    private val _isLoadingPhotoInfo = MutableStateFlow(false)
    val isLoadingPhotoInfo: StateFlow<Boolean> = _isLoadingPhotoInfo

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _photoInfoError = MutableStateFlow<String?>(null)
    val photoInfoError: StateFlow<String?> = _photoInfoError


    private var currentQuery: String = ""
    private var currentPage: Int = 0
    private var totalPages: Int = Int.MAX_VALUE

    fun searchPhotos(query: String) {
        if(query.isBlank()) return
        currentQuery = query
        currentPage = 0
        totalPages = Int.MAX_VALUE
        _endReached.value = false
        _photos.value = emptyList()
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

    fun loadNextPage() {
        if (currentQuery.isBlank() || _isLoading.value || _isLoadingMore.value || _endReached.value) return
        val next = if (currentPage == 0) 1 else currentPage + 1
        if (next > totalPages) { _endReached.value = true; return }

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                loadPage(page = next, firstPage = false)
            } catch (e: Exception) {
                _errorMessage.value = "Network Error: ${e.message}"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private suspend fun loadPage(page: Int, firstPage: Boolean) {
        val response = flickrApi.searchPhotos(
            searchText = currentQuery,
            page = page,
            perPage = PER_PAGE
        )

        if (response.stat != "ok") {
            if (firstPage) _photos.value = emptyList()
            _endReached.value = true
            _errorMessage.value = response.stat
            return
        }

        val p = response.photos
        currentPage = p.page
        totalPages = p.pages

        val newItems = p.photo
        _photos.value = if (firstPage) newItems else _photos.value + newItems

        if (currentPage >= totalPages || newItems.isEmpty()) {
            _endReached.value = true
        }
    }

    fun selectPhoto(photo: Photo) {
        _selectedPhoto.value = photo
        fetchPhotoInfo(photo)
    }

    private fun fetchPhotoInfo(photo: Photo) {
        viewModelScope.launch {
            _isLoadingPhotoInfo.value = true
            _photoInfoError.value = null
            _selectedPhotoInfo.value = null

            try {
                val response = flickrApi.getPhotoInfo(
                    photoId = photo.id,
                    secret = photo.secret
                )

                if (response.stat == "ok") {
                    _selectedPhotoInfo.value = response.photo
                } else {
                    _photoInfoError.value = "Failed to load photo details"
                }
            } catch (e: Exception) {
                _photoInfoError.value = "Network Error: ${e.message}"
                Log.println(Log.ERROR, "PHOTO_INFO_FAILURE", "Error: ${e.message}")
            } finally {
                _isLoadingPhotoInfo.value = false
            }
        }
    }

    fun clearSelection() {
        _selectedPhoto.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getPhotoUrl(photo: Photo, size: String = "m"): String {
        return "https://live.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_${size}.jpg"
    }

    fun getHeroPhotoUrl(photo: Photo): String {
        return "https://live.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_b.jpg"
    }
}