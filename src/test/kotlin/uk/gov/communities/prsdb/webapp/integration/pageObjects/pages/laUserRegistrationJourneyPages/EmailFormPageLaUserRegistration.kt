package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FormBasePage

class EmailFormPageLaUserRegistration(
    page: Page,
) : FormBasePage(page, "/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.Email.urlPathSegment}") {
    val emailInput = form.getTextInput("emailAddress")
}
