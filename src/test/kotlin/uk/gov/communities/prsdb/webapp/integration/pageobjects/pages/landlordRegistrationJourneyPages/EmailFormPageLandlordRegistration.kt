package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.FormBasePage

class EmailFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(page, "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.Email.urlPathSegment}") {
    val emailInput = form.getTextInput("emailAddress")

    fun submitFormAndAssertNextPage(): PhoneNumberFormPageLandlordRegistration = clickElementAndAssertNextPage(form.getSubmitButton())
}
