package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PrivacyNoticePageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        RegisterLandlordController.LANDLORD_REGISTRATION_PRIVACY_NOTICE_ROUTE,
    ) {
    val form = PrivacyNoticeForm(page)

    val sectionHeader = SectionHeader(page.locator("html"))

    fun agreeAndSubmit() {
        form.iAgreeCheckbox.check()
        form.submit()
    }

    fun submitWithoutAgreeing() {
        form.submit()
    }

    class PrivacyNoticeForm(
        page: Page,
    ) : PostForm(page) {
        val iAgreeCheckbox = Checkboxes(locator).getCheckbox("true")
    }
}
