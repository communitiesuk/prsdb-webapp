package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Page

open class FormWithTwoSubmitButtons(
    page: Page,
) : PostForm(page) {
    fun submitPrimaryButton(primaryButtonValue: String = "continue") = submitSelectedButton(primaryButtonValue)

    fun submitSecondaryButton(secondaryButtonValue: String = "provideThisLater") = submitSelectedButton(secondaryButtonValue)
}
