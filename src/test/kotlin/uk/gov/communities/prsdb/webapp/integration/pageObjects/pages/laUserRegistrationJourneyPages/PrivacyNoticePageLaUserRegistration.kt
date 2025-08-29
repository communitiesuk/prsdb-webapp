package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PrivacyNoticePageLaUserRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.PrivacyNotice.urlPathSegment}",
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
