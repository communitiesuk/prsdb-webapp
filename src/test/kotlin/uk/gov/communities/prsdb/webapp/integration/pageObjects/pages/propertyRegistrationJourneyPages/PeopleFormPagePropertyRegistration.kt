package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PeopleFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.NumberOfPeople.urlPathSegment}",
    ) {
    val form = NumOfPeopleForm(page)

    fun submitNumOfPeople(num: Int) = submitNumOfPeople(num.toString())

    fun submitNumOfPeople(num: String) {
        form.peopleInput.fill(num)
        form.submit()
    }

    class NumOfPeopleForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val peopleInput = TextInput.textByFieldName(locator, "numberOfPeople")
    }
}
