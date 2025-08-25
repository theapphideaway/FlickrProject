package com.ianschoenrock.networking.endpoints

import com.ianschoenrock.networking.FlickrApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class DetailsEndpointTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var flickrApi: FlickrApi
    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        flickrApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FlickrApi::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getPhotoInfo returns successful response`() = runBlocking {
        val mockResponse = """
        {
          "stat": "ok",
          "photo": {
            "id": "test123",
            "secret": "testsecret",
            "server": "testserver",
            "farm": 42,
            "dateuploaded": "1634567890",
            "isfavorite": 0,
            "license": "0",
            "safety_level": "0",
            "rotation": 0,
            "title": { "_content": "Test Title" },
            "description": { "_content": "Test Description" },
            "dates": {
              "posted": "1634567890",
              "taken": "2021-10-18 14:30:45",
              "takengranularity": "0"
            },
            "owner": {
              "nsid": "12345@N00",
              "username": "testuser",
              "realname": "Test User",
              "location": "Test Location"
            }
          }
        }
    """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )

        val response = flickrApi.getPhotoInfo(photoId = "test123", secret = "testsecret")

        assertEquals("ok", response.stat)
        assertNotNull(response.photo)

        val photo = response.photo!!
        assertEquals("test123", photo.id)
        assertEquals("testsecret", photo.secret)
        assertEquals("testserver", photo.server)
        assertEquals(42, photo.farm)
        assertEquals("1634567890", photo.dateUploaded)
        assertEquals(0, photo.isFavorite)
        assertEquals("0", photo.license)
        assertEquals("0", photo.safetyLevel)
        assertEquals(0, photo.rotation)
        assertEquals("Test Title", photo.title._content)
        assertEquals("Test Description", photo.description._content)
        assertEquals("1634567890", photo.dates.posted)
        assertEquals("2021-10-18 14:30:45", photo.dates.taken)
        assertEquals("0", photo.dates.takenGranularity)
        assertEquals("12345@N00", photo.owner.nsId)
        assertEquals("testuser", photo.owner.username)
        assertEquals("Test User", photo.owner.realName)
        assertEquals("Test Location", photo.owner.location)
        assertNull(photo.urls)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        val url = request.requestUrl.toString()
        assertTrue(url.contains("method=flickr.photos.getInfo"))
        assertTrue(url.contains("photo_id=test123"))
        assertTrue(url.contains("secret=testsecret"))
        assertTrue(url.contains("format=json"))
        assertTrue(url.contains("nojsoncallback=1"))
    }

    @Test
    fun `getPhotoInfo handles error response without photo`() = runBlocking {
        val mockResponse = """
        {
        "photo": {
                    "id": "123",
                    "secret": "abc",
                    "server": "456",
                    "farm": 1,
                    "dateuploaded": "1634567890",
                    "isfavorite": 0,
                    "license": "0",
                    "safety_level": "0",
                    "rotation": 0,
                    "title": {
                        "_content": "Test Title"
                    },
                    "description": {
                        "_content": "Test Description"
                    },
                    "dates": {
                        "posted": "1634567890",
                        "taken": "2021-10-18 12:00:00",
                        "takengranularity": "0"
                    },
                    "owner": {
                        "nsid": "12345@N00",
                        "username": "testuser",
                        "realname": "Test User",
                        "location": "Test City"
                    }
                },
          "stat": "fail",
        }
    """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )

        val response = flickrApi.getPhotoInfo(photoId = "does-not-exist")

        assertEquals("fail", response.stat)
        assertNull(response.photo)
    }

    @Test
    fun `getPhotoInfo omits secret when null`() = runBlocking {
        val okResponse = """
        {
          "stat": "ok",
          "photo": {
            "id": "abc",
            "secret": "zzz",
            "server": "1",
            "farm": 1,
            "dateuploaded": "0",
            "isfavorite": 0,
            "license": "0",
            "safety_level": "0",
            "rotation": 0,
            "title": { "_content": "" },
            "description": { "_content": "" },
            "dates": {"posted":"0","taken":"","takengranularity":"0"},
            "owner": {"nsid":"","username":"","realname":"","location":""}
          }
        }
    """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(okResponse)
                .addHeader("Content-Type", "application/json")
        )

        flickrApi.getPhotoInfo(photoId = "abc", secret = null)

        val request = mockWebServer.takeRequest()
        val url = request.requestUrl.toString()
        assertTrue(url.contains("method=flickr.photos.getInfo"))
        assertTrue(url.contains("photo_id=abc"))
        assertFalse(url.contains("secret="))
    }

}