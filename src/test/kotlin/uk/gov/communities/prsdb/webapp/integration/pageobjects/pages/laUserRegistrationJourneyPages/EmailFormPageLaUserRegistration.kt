package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.PageNotFoundPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.FormBasePage

class EmailFormPageLaUserRegistration(
    page: Page,
) : FormBasePage(page, "/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.Email.urlPathSegment}") {
    val emailInput = form.getTextInput("emailAddress")

    fun submitFormAndAssertNextPage(): PageNotFoundPage = clickElementAndAssertNextPage(form.getSubmitButton())
}
