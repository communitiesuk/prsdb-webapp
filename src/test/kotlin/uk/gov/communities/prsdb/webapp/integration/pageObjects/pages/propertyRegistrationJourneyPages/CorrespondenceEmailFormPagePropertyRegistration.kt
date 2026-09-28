package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.CorrespondenceEmailOption
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ErrorSummary
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep

class CorrespondenceEmailFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CorrespondenceEmailStep.ROUTE_SEGMENT}") {
    val form = CorrespondenceEmailForm(page)

    val errorSummary = ErrorSummary(page)

    fun submitAccountEmail() {
        form.whichEmailRadios.selectValue(CorrespondenceEmailOption.ACCOUNT_EMAIL)
        form.submit()
    }

    fun submitDifferentEmail(email: String) {
        form.whichEmailRadios.selectValue(CorrespondenceEmailOption.DIFFERENT_EMAIL)
        form.differentEmailInput.fill(email)
        form.submit()
    }

    class CorrespondenceEmailForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val whichEmailRadios = Radios(locator)
        val differentEmailInput = TextInput.emailByFieldName(locator, "differentEmailAddress")
    }
}
