package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPagePropertyRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.CheckAnswers.urlPathSegment}") {
    val form = FormWithSectionHeader(page)

    fun confirm() = form.submit()
}
