package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class PhoneNumberFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = PhoneNumberFormLandlord(page)

    fun submitPhoneNumber(phoneNumber: String) {
        form.phoneNumberInput.fill(phoneNumber)
        form.submit()
    }

    class PhoneNumberFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val phoneNumberInput = TextInput.textByFieldName(locator, "phoneNumber")
    }
}
