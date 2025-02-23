package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator

interface TextFillable {
    val locator: Locator

    fun fill(text: String) {
        locator.fill(text)
    }
}
