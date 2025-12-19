package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PrivacyNoticePageLocalCouncilUserRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}" +
            "/${RegisterLocalCouncilUserStepId.PrivacyNotice.urlPathSegment}",
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
