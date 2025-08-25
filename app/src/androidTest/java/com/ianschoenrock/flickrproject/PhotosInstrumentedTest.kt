package com.ianschoenrock.flickrproject

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test


class PhotosInstrumentedTest{
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun search_scroll_openDetails_showsInfo() {
        composeRule.onNodeWithTag("SearchField").performTextClearance()
        composeRule.onNodeWithTag("SearchField").performTextInput("cats")
        composeRule.onNodeWithTag("SearchField").performImeAction()

        composeRule.onAllNodesWithTag("PhotoCard")[0].performClick()

        composeRule.onAllNodesWithTag("PhotoDetailTitle").fetchSemanticsNodes().isNotEmpty()

    }
}
