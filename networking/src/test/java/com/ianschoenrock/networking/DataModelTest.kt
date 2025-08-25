package com.ianschoenrock.networking

import com.ianschoenrock.networking.models.search.Photo
import com.ianschoenrock.networking.models.search.PhotoSearchResponse
import com.ianschoenrock.networking.models.search.Photos
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class DataModelTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun `Photo model serializes and deserializes correctly`() {

        val json = """
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
            }
        """.trimIndent()

        val adapter = moshi.adapter(Photo::class.java)
        val photo = adapter.fromJson(json)

        assertNotNull(photo)
        assertEquals("54738342399", photo!!.id)
        assertEquals("203444619@N08", photo.owner)
        assertEquals("c335081470", photo.secret)
        assertEquals("65535", photo.server)
        assertEquals(66, photo.farm)
        assertEquals("Cat performing trick", photo.title)
        assertEquals(1, photo.isPublic)
        assertEquals(0, photo.isFriend)
        assertEquals(0, photo.isFamily)
    }

    @Test
    fun `Photos model serializes and deserializes correctly`() {

        val json = """
            {
                "page": 1,
                "pages": 1225,
                "perpage": 100,
                "total": "122441",
                "photo": [
                    {
                        "id": "1",
                        "owner": "owner1",
                        "secret": "secret1",
                        "server": "server1",
                        "farm": 1,
                        "title": "title1",
                        "ispublic": 1,
                        "isfriend": 0,
                        "isfamily": 0
                    }
                ]
            }
        """.trimIndent()

        val adapter = moshi.adapter(Photos::class.java)
        val photos = adapter.fromJson(json)

        assertNotNull(photos)
        assertEquals(1, photos!!.page)
        assertEquals(1225, photos.pages)
        assertEquals(100, photos.perpage)
        assertEquals("122441", photos.total)
        assertEquals(1, photos.photo.size)
        assertEquals("1", photos.photo[0].id)
    }

    @Test
    fun `FlickrResponse model serializes and deserializes correctly`() {
        val json = """
            {
                "photos": {
                    "page": 1,
                    "pages": 10,
                    "perpage": 50,
                    "total": "500",
                    "photo": []
                },
                "stat": "ok"
            }
        """.trimIndent()

        val adapter = moshi.adapter(PhotoSearchResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("ok", response!!.stat)
        assertEquals(1, response.photos.page)
        assertEquals(10, response.photos.pages)
        assertEquals(50, response.photos.perpage)
        assertEquals("500", response.photos.total)
        assertTrue(response.photos.photo.isEmpty())
    }

    @Test
    fun `Photo model handles missing optional fields`() {
        val json = """
            {
                "id": "123",
                "owner": "owner123",
                "secret": "secret123",
                "server": "server123",
                "farm": 5,
                "title": "",
                "ispublic": 1,
                "isfriend": 0,
                "isfamily": 0
            }
        """.trimIndent()

        val adapter = moshi.adapter(Photo::class.java)
        val photo = adapter.fromJson(json)

        assertNotNull(photo)
        assertEquals("123", photo!!.id)
        assertEquals("", photo.title)
    }

    @Test
    fun `Photos model handles empty photo array`() {
        val json = """
            {
                "page": 1,
                "pages": 0,
                "perpage": 100,
                "total": "0",
                "photo": []
            }
        """.trimIndent()

        val adapter = moshi.adapter(Photos::class.java)
        val photos = adapter.fromJson(json)

        assertNotNull(photos)
        assertEquals("0", photos!!.total)
        assertTrue(photos.photo.isEmpty())
    }

    @Test
    fun `FlickrResponse handles error status`() {
        val json = """
            {
                "photos": {
                    "page": 0,
                    "pages": 0,
                    "perpage": 0,
                    "total": "0",
                    "photo": []
                },
                "stat": "fail"
            }
        """.trimIndent()

        val adapter = moshi.adapter(PhotoSearchResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("fail", response!!.stat)
        assertTrue(response.photos.photo.isEmpty())
    }

    @Test
    fun `Photo model serialization roundtrip works correctly`() {
        val originalPhoto = Photo(
            id = "test123",
            owner = "testowner",
            secret = "testsecret",
            server = "testserver",
            farm = 42,
            title = "Test Photo",
            isPublic = 1,
            isFriend = 0,
            isFamily = 0
        )

        val adapter = moshi.adapter(Photo::class.java)
        val json = adapter.toJson(originalPhoto)
        val deserializedPhoto = adapter.fromJson(json)

        assertEquals(originalPhoto, deserializedPhoto)
    }
}