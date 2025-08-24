package com.ianschoenrock.networking

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

class FlickrApiTest {

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
    fun `searchPhotos returns successful response with photos`() = runBlocking {
        val mockResponse = """
            {
                "photos": {
                    "page": 1,
                    "pages": 1225,
                    "perpage": 2,
                    "total": "122441",
                    "photo": [
                        {
                            "id": "54738342399",
                            "owner": "203444619@N08",
                            "secret": "c335081470",
                            "server": "65535",
                            "farm": 66,
                            "title": "Cat performing trick",
                            "ispublic": 1,
                            "isfriend": 0,
                            "isfamily": 0
                        },
                        {
                            "id": "54738317458",
                            "owner": "169222263@N02",
                            "secret": "8c7449e258",
                            "server": "65535",
                            "farm": 66,
                            "title": "Tree cat Meow",
                            "ispublic": 1,
                            "isfriend": 0,
                            "isfamily": 0
                        }
                    ]
                },
                "stat": "ok"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )


        val response = flickrApi.searchPhotos(searchText = "cats")

        assertEquals("ok", response.stat)
        assertEquals(1, response.photos.page)
        assertEquals(1225, response.photos.pages)
        assertEquals(2, response.photos.perpage)
        assertEquals("122441", response.photos.total)
        assertEquals(2, response.photos.photo.size)

        val firstPhoto = response.photos.photo[0]
        assertEquals("54738342399", firstPhoto.id)
        assertEquals("Cat performing trick", firstPhoto.title)
        assertEquals("203444619@N08", firstPhoto.owner)
        assertEquals(1, firstPhoto.isPublic)

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        val requestUrl = request.requestUrl.toString()
        assertTrue(requestUrl.contains("method=flickr.photos.search"))
        assertTrue(requestUrl.contains("text=cats"))
        assertTrue(requestUrl.contains("format=json"))
        assertTrue(requestUrl.contains("nojsoncallback=1"))
    }

    @Test
    fun `searchPhotos handles empty photo array`() = runBlocking {
        val mockResponse = """
            {
                "photos": {
                    "page": 1,
                    "pages": 0,
                    "perpage": 100,
                    "total": "0",
                    "photo": []
                },
                "stat": "ok"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )

        val response = flickrApi.searchPhotos(searchText = "nonexistentterm")

        assertEquals("ok", response.stat)
        assertEquals("0", response.photos.total)
        assertTrue(response.photos.photo.isEmpty())
    }

    @Test
    fun `searchPhotos handles error response`() = runBlocking {
        val mockResponse = """
            {
                "photos": {
                    "page": 0,
                    "pages": 0,
                    "perpage": 0,
                    "total": "0",
                    "photo": []
                },
                "stat": "fail",
                "code": 100,
                "message": "Invalid API Key"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )

        val response = flickrApi.searchPhotos(searchText = "cats")

        assertEquals("fail", response.stat)
        assertTrue(response.photos.photo.isEmpty())
    }

    @Test
    fun `searchPhotos uses default parameters correctly`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"photos":{"page":1,"pages":0,"perpage":100,"total":"0","photo":[]},"stat":"ok"}""")
                .addHeader("Content-Type", "application/json")
        )

        flickrApi.searchPhotos(searchText = "test")

        val request = mockWebServer.takeRequest()
        val requestUrl = request.requestUrl.toString()
        assertTrue(requestUrl.contains("method=flickr.photos.search"))
        assertTrue(requestUrl.contains("format=json"))
        assertTrue(requestUrl.contains("nojsoncallback=1"))
        assertTrue(requestUrl.contains("text=test"))
    }
}