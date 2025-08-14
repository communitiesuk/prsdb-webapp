package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PrivacyNoticePageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.PrivacyNotice.urlPathSegment}",
    ) {
    val form = PrivacyNoticeForm(page)

    fun agreeAndSubmit() {
        form.iAgreeCheckbox.check()
        form.submit()
    }

    fun submitWithoutAgreeing() {
        form.submit()
    }

    class PrivacyNoticeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val iAgreeCheckbox = Checkboxes(locator).getCheckbox("true")
    }
}
