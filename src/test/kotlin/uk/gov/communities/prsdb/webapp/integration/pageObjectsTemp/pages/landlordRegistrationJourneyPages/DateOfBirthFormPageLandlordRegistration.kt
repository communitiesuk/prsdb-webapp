package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.FormBasePage

class DateOfBirthFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.DateOfBirth.urlPathSegment}",
    ) {
    val dayInput = form.getTextInput("day")
    val monthInput = form.getTextInput("month")
    val yearInput = form.getTextInput("year")
}
