package com.ianschoenrock.networking.models

import com.ianschoenrock.networking.models.details.PhotoContent
import com.ianschoenrock.networking.models.details.PhotoDates
import com.ianschoenrock.networking.models.details.PhotoInfo
import com.ianschoenrock.networking.models.details.PhotoInfoResponse
import com.ianschoenrock.networking.models.details.PhotoOwner
import com.ianschoenrock.networking.models.details.getFormattedDatePosted
import com.ianschoenrock.networking.models.details.getFormattedDateTaken
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PhotoInfoDataModelTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun `PhotoInfo model serializes and deserializes correctly`() {
        val json = """
            {
                "id": "54738342399",
                "secret": "c335081470",
                "server": "65535",
                "farm": 66,
                "dateuploaded": "1634567890",
                "isfavorite": 0,
                "license": "0",
                "safety_level": "0",
                "rotation": 0,
                "title": {
                    "_content": "Beautiful Sunset Over Mountains"
                },
                "description": {
                    "_content": "A stunning sunset captured during my hiking trip in the Rocky Mountains."
                },
                "dates": {
                    "posted": "1634567890",
                    "taken": "2021-10-18 18:30:45",
                    "takengranularity": "0"
                },
                "owner": {
                    "nsid": "203444619@N08",
                    "username": "photographer123",
                    "realname": "John Smith",
                    "location": "Colorado, USA"
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(PhotoInfo::class.java)
        val photoInfo = adapter.fromJson(json)

        assertNotNull(photoInfo)
        assertEquals("54738342399", photoInfo!!.id)
        assertEquals("c335081470", photoInfo.secret)
        assertEquals("65535", photoInfo.server)
        assertEquals(66, photoInfo.farm)
        assertEquals("1634567890", photoInfo.dateUploaded)
        assertEquals(0, photoInfo.isFavorite)
        assertEquals("0", photoInfo.license)
        assertEquals("0", photoInfo.safetyLevel)
        assertEquals(0, photoInfo.rotation)
        assertEquals("Beautiful Sunset Over Mountains", photoInfo.title._content)
        assertEquals("A stunning sunset captured during my hiking trip in the Rocky Mountains.", photoInfo.description._content)
        assertEquals("1634567890", photoInfo.dates.posted)
        assertEquals("2021-10-18 18:30:45", photoInfo.dates.taken)
        assertEquals("0", photoInfo.dates.takenGranularity)
        assertEquals("203444619@N08", photoInfo.owner.nsId)
        assertEquals("photographer123", photoInfo.owner.username)
        assertEquals("John Smith", photoInfo.owner.realName)
        assertEquals("Colorado, USA", photoInfo.owner.location)
    }

    @Test
    fun `PhotoInfoResponse model serializes and deserializes correctly`() {
        val json = """
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
                "stat": "ok"
            }
        """.trimIndent()

        val adapter = moshi.adapter(PhotoInfoResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("ok", response!!.stat)
        assertEquals("123", response.photo.id)
        assertEquals("Test Title", response.photo.title._content)
        assertEquals("Test Description", response.photo.description._content)
        assertEquals("testuser", response.photo.owner.username)
    }

    @Test
    fun `PhotoInfo handles empty optional fields`() {
        val json = """
            {
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
                    "_content": ""
                },
                "description": {
                    "_content": ""
                },
                "dates": {
                    "posted": "1634567890",
                    "taken": "2021-10-18 12:00:00",
                    "takengranularity": "0"
                },
                "owner": {
                    "nsid": "12345@N00",
                    "username": "testuser",
                    "realname": null,
                    "location": null
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(PhotoInfo::class.java)
        val photoInfo = adapter.fromJson(json)

        assertNotNull(photoInfo)
        assertEquals("", photoInfo!!.title._content)
        assertEquals("", photoInfo.description._content)
        assertNull(photoInfo.owner.realName)
        assertNull(photoInfo.owner.location)
    }

    @Test
    fun `PhotoInfo handles missing urls field`() {
        val json = """
            {
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
                    "_content": "Test"
                },
                "description": {
                    "_content": "Test"
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
            }
        """.trimIndent()

        val adapter = moshi.adapter(PhotoInfo::class.java)
        val photoInfo = adapter.fromJson(json)

        assertNotNull(photoInfo)
        assertNull(photoInfo!!.urls)
    }

    @Test
    fun `PhotoInfo with URLs field serializes correctly`() {
        val json = """
            {
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
                    "_content": "Test"
                },
                "description": {
                    "_content": "Test"
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
                },
                "urls": {
                    "url": [
                        {
                            "type": "photopage",
                            "_content": "https://www.flickr.com/photos/testuser/123/"
                        }
                    ]
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(PhotoInfo::class.java)
        val photoInfo = adapter.fromJson(json)

        assertNotNull(photoInfo)
        assertNotNull(photoInfo!!.urls)
        assertEquals(1, photoInfo.urls!!.url.size)
        assertEquals("photopage", photoInfo.urls!!.url[0].type)
        assertEquals("https://www.flickr.com/photos/testuser/123/", photoInfo.urls!!.url[0]._content)
    }

    @Test
    fun `PhotoInfoResponse handles error response`() {
        val json = """
            {
              "stat": "fail",
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

        val adapter = moshi.adapter(PhotoInfoResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("fail", response!!.stat)
    }

    @Test
    fun `getFormattedDateTaken formats date correctly`() {
        val dates = PhotoDates(
            posted = "1634567890",
            taken = "2021-10-18 14:30:45",
            takenGranularity = "0"
        )

        val formatted = dates.getFormattedDateTaken()

        assertTrue(formatted.contains("Oct") || formatted.contains("10"))
        assertTrue(formatted.contains("18"))
        assertTrue(formatted.contains("2021"))
    }

    @Test
    fun `getFormattedDateTaken handles malformed date`() {
        val dates = PhotoDates(
            posted = "1634567890",
            taken = "invalid-date",
            takenGranularity = "0"
        )

        val formatted = dates.getFormattedDateTaken()

        assertEquals("invalid-date", formatted)
    }

    @Test
    fun `getFormattedDatePosted formats timestamp correctly`() {
        val dates = PhotoDates(
            posted = "1634567890",
            taken = "2021-10-18 14:30:45",
            takenGranularity = "0"
        )

        val formatted = dates.getFormattedDatePosted()

        assertTrue(formatted.contains("Oct") || formatted.contains("10"))
        assertTrue(formatted.contains("18"))
        assertTrue(formatted.contains("2021"))
    }

    @Test
    fun `getFormattedDatePosted handles invalid timestamp`() {
        val dates = PhotoDates(
            posted = "invalid-timestamp",
            taken = "2021-10-18 14:30:45",
            takenGranularity = "0"
        )

        val formatted = dates.getFormattedDatePosted()

        assertEquals("invalid-timestamp", formatted)
    }

    @Test
    fun `getFormattedDatePosted handles empty timestamp`() {
        val dates = PhotoDates(
            posted = "",
            taken = "2021-10-18 14:30:45",
            takenGranularity = "0"
        )

        val formatted = dates.getFormattedDatePosted()

        assertEquals("", formatted)
    }

    @Test
    fun `PhotoInfo serialization roundtrip works correctly`() {
        val originalPhotoInfo = PhotoInfo(
            id = "test123",
            secret = "testsecret",
            server = "testserver",
            farm = 42,
            dateUploaded = "1634567890",
            isFavorite = 0,
            license = "0",
            safetyLevel = "0",
            rotation = 0,
            title = PhotoContent("Test Title"),
            description = PhotoContent("Test Description"),
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
            ),
            urls = null
        )

        val adapter = moshi.adapter(PhotoInfo::class.java)
        val json = adapter.toJson(originalPhotoInfo)
        val deserializedPhotoInfo = adapter.fromJson(json)

        assertEquals(originalPhotoInfo, deserializedPhotoInfo)
    }
}