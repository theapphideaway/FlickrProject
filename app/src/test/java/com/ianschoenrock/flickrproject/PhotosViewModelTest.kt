package com.ianschoenrock.flickrproject

import com.ianschoenrock.flickrproject.ui.viewmodels.PhotosViewModel
import com.ianschoenrock.networking.BuildConfig
import com.ianschoenrock.networking.FlickrApi
import com.ianschoenrock.networking.models.Photo
import com.ianschoenrock.networking.models.PhotoSearchResponse
import com.ianschoenrock.networking.models.Photos
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModelTest {

    @Mock
    private lateinit var flickrApi: FlickrApi

    private lateinit var viewModel: PhotosViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = PhotosViewModel(flickrApi)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchPhotos success updates photos state`() = runTest {
        val mockPhotos = listOf(
            Photo(
                id = "1",
                owner = "owner1",
                secret = "secret1",
                server = "server1",
                farm = 1,
                title = "Cat Photo 1",
                isPublic = 1,
                isFriend = 0,
                isFamily = 0
            ),
            Photo(
                id = "2",
                owner = "owner2",
                secret = "secret2",
                server = "server2",
                farm = 2,
                title = "Cat Photo 2",
                isPublic = 1,
                isFriend = 0,
                isFamily = 0
            )
        )

        val mockResponse = PhotoSearchResponse(
            photos = Photos(
                page = 1,
                pages = 10,
                perpage = 100,
                total = "1000",
                photo = mockPhotos
            ),
            stat = "ok"
        )

        whenever(flickrApi.searchPhotos(searchText = "cats")).thenReturn(mockResponse)

        viewModel.searchPhotos("cats")
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(mockPhotos, viewModel.photos.first())
        Assert.assertFalse(viewModel.isLoading.first())
        Assert.assertNull(viewModel.errorMessage.first())
        verify(flickrApi).searchPhotos(searchText = "cats")
    }

    @Test
    fun `searchPhotos with blank query does nothing`() = runTest {
        val initialPhotos = viewModel.photos.first()

        viewModel.searchPhotos("")
        viewModel.searchPhotos("   ")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(initialPhotos, viewModel.photos.first())
        verifyNoInteractions(flickrApi)
    }

    @Test
    fun `searchPhotos network error updates error state`() = runTest {
        whenever(
            flickrApi.searchPhotos(
                method = any(),
                apiKey = any(),
                searchText = eq("cats"),
                format = any(),
                noCallback = any()
            )
        ).thenThrow(RuntimeException("Network error"))

        viewModel.searchPhotos("cats")
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertTrue(viewModel.photos.first().isEmpty())
        Assert.assertFalse(viewModel.isLoading.first())
        Assert.assertEquals("Network Error: Network error", viewModel.errorMessage.first())
    }

    @Test
    fun `searchPhotos API failure response updates error state`() = runTest {
        val mockResponse = PhotoSearchResponse(
            photos = Photos(
                page = 0,
                pages = 0,
                perpage = 0,
                total = "0",
                photo = emptyList()
            ),
            stat = "fail"
        )

        whenever(flickrApi.searchPhotos(searchText = "cats")).thenReturn(mockResponse)

        viewModel.searchPhotos("cats")
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertTrue(viewModel.photos.first().isEmpty())
        Assert.assertFalse(viewModel.isLoading.first())
        Assert.assertEquals("Search Failed", viewModel.errorMessage.first())
    }

    @Test
    fun `searchPhotos completes with loading false`() = runTest {
        val mockResponse = PhotoSearchResponse(
            photos = Photos(1, 1, 1, "1", emptyList()),
            stat = "ok"
        )
        whenever(
            flickrApi.searchPhotos(
                method = any(),
                apiKey = any(),
                searchText = eq("cats"),
                format = any(),
                noCallback = any()
            )
        ).thenReturn(mockResponse)

        viewModel.searchPhotos("cats")
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertFalse(viewModel.isLoading.first())
    }

    @Test
    fun `selectPhoto updates selectedPhoto state`() = runTest {
        val photo = Photo("1", "owner", "secret", "server", 1, "title", 1, 0, 0)

        viewModel.selectPhoto(photo)

        Assert.assertEquals(photo, viewModel.selectedPhoto.first())
    }

    @Test
    fun `clearSelection resets selectedPhoto state`() = runTest {
        val photo = Photo("1", "owner", "secret", "server", 1, "title", 1, 0, 0)
        viewModel.selectPhoto(photo)

        viewModel.clearSelection()

        Assert.assertNull(viewModel.selectedPhoto.first())
    }

    @Test
    fun `clearError resets error state`() = runTest {
        whenever(
            flickrApi.searchPhotos(
                method = any(),
                apiKey = any(),
                searchText = eq("cats"),
                format = any(),
                noCallback = any()
            )
        ).thenThrow(RuntimeException("Network error"))  // Changed from IOException to RuntimeException

        viewModel.searchPhotos("cats")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearError()

        Assert.assertNull(viewModel.errorMessage.first())
    }

    @Test
    fun `getPhotoUrl returns correct URL format`() {
        val photo = Photo("123", "owner", "abc123", "456", 7, "title", 1, 0, 0)

        val url = viewModel.getPhotoUrl(photo)
        val customSizeUrl = viewModel.getPhotoUrl(photo, "z")

        Assert.assertEquals("https://live.staticflickr.com/456/123_abc123_m.jpg", url)
        Assert.assertEquals("https://live.staticflickr.com/456/123_abc123_z.jpg", customSizeUrl)
    }
}