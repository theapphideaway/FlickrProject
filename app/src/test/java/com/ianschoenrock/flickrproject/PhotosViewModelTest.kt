package com.ianschoenrock.flickrproject

import com.ianschoenrock.flickrproject.ui.viewmodels.PhotosViewModel
import com.ianschoenrock.networking.FlickrApi
import com.ianschoenrock.networking.models.details.PhotoContent
import com.ianschoenrock.networking.models.details.PhotoDates
import com.ianschoenrock.networking.models.details.PhotoInfo
import com.ianschoenrock.networking.models.details.PhotoInfoResponse
import com.ianschoenrock.networking.models.details.PhotoOwner
import com.ianschoenrock.networking.models.search.Photo
import com.ianschoenrock.networking.models.search.PhotoSearchResponse
import com.ianschoenrock.networking.models.search.Photos
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
    fun `selectPhoto triggers photo info fetch`() = runTest {
        val photo = Photo("1", "owner", "secret", "server", 1, "title", 1, 0, 0)
        val mockPhotoInfo = createMockPhotoInfo()
        val mockResponse = PhotoInfoResponse(photo = mockPhotoInfo, stat = "ok")

        whenever(flickrApi.getPhotoInfo(
            method = eq("flickr.photos.getInfo"),
            apiKey = any(),
            photoId = eq("1"),
            secret = eq("secret"),
            format = eq("json"),
            nojsoncallback = eq(1)
        )).thenReturn(mockResponse)

        viewModel.selectPhoto(photo)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(photo, viewModel.selectedPhoto.first())
        Assert.assertEquals(mockPhotoInfo, viewModel.selectedPhotoInfo.first())
        Assert.assertFalse(viewModel.isLoadingPhotoInfo.first())
        Assert.assertNull(viewModel.photoInfoError.first())
        verify(flickrApi).getPhotoInfo(
            method = eq("flickr.photos.getInfo"),
            apiKey = any(),
            photoId = eq("1"),
            secret = eq("secret"),
            format = eq("json"),
            nojsoncallback = eq(1)
        )
    }

    @Test
    fun `selectPhoto handles photo info fetch error`() = runTest {
        val photo = Photo("1", "owner", "secret", "server", 1, "title", 1, 0, 0)

        whenever(flickrApi.getPhotoInfo(
            method = eq("flickr.photos.getInfo"),
            apiKey = any(),
            photoId = eq("1"),
            secret = eq("secret"),
            format = eq("json"),
            nojsoncallback = eq(1)
        )).thenThrow(RuntimeException("Network error"))

        viewModel.selectPhoto(photo)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(photo, viewModel.selectedPhoto.first())
        Assert.assertNull(viewModel.selectedPhotoInfo.first())
        Assert.assertFalse(viewModel.isLoadingPhotoInfo.first())
        Assert.assertEquals("Network Error: Network error", viewModel.photoInfoError.first())
    }

    @Test
    fun `selectPhoto handles API failure response for photo info`() = runTest {
        val photo = Photo("1", "owner", "secret", "server", 1, "title", 1, 0, 0)
        val mockResponse = PhotoInfoResponse(
            photo = createMockPhotoInfo(), // This won't be used since stat is fail
            stat = "fail"
        )

        whenever(flickrApi.getPhotoInfo(
            method = eq("flickr.photos.getInfo"),
            apiKey = any(),
            photoId = eq("1"),
            secret = eq("secret"),
            format = eq("json"),
            nojsoncallback = eq(1)
        )).thenReturn(mockResponse)

        viewModel.selectPhoto(photo)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(photo, viewModel.selectedPhoto.first())
        Assert.assertNull(viewModel.selectedPhotoInfo.first())
        Assert.assertFalse(viewModel.isLoadingPhotoInfo.first())
        Assert.assertEquals("Failed to load photo details", viewModel.photoInfoError.first())
    }

    @Test
    fun `photo info loading completes correctly`() = runTest {
        val photo = Photo("1", "owner", "secret", "server", 1, "title", 1, 0, 0)
        val mockPhotoInfo = createMockPhotoInfo()
        val mockResponse = PhotoInfoResponse(photo = mockPhotoInfo, stat = "ok")

        whenever(flickrApi.getPhotoInfo(
            method = eq("flickr.photos.getInfo"),
            apiKey = any(),
            photoId = eq("1"),
            secret = eq("secret"),
            format = eq("json"),
            nojsoncallback = eq(1)
        )).thenReturn(mockResponse)

        Assert.assertFalse(viewModel.isLoadingPhotoInfo.first())

        viewModel.selectPhoto(photo)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertFalse(viewModel.isLoadingPhotoInfo.first())
        Assert.assertEquals(mockPhotoInfo, viewModel.selectedPhotoInfo.first())
        Assert.assertNull(viewModel.photoInfoError.first())
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

    @Test
    fun `getHeroPhotoUrl returns correct URL format`() {
        val photo = Photo("123", "owner", "abc123", "456", 7, "title", 1, 0, 0)

        val heroUrl = viewModel.getHeroPhotoUrl(photo)

        Assert.assertEquals("https://live.staticflickr.com/456/123_abc123_b.jpg", heroUrl)
    }

    private fun createMockPhotoInfo(): PhotoInfo {
        return PhotoInfo(
            id = "1",
            secret = "secret",
            server = "server",
            farm = 1,
            dateUploaded = "1634567890",
            isFavorite = 0,
            license = "0",
            safetyLevel = "0",
            rotation = 0,
            title = PhotoContent("Test Photo Title"),
            description = PhotoContent("This is a test photo description"),
            dates = PhotoDates(
                posted = "1634567890",
                taken = "2021-10-18 14:30:45",
                takenGranularity = "0"
            ),
            owner = PhotoOwner(
                nsId = "12345@N00",
                username = "testuser",
                realName = "Test User",
                location = "Test Location"
            )
        )
    }
}