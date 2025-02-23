package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class HouseholdsFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment}",
    ) {
    val form = HouseholdsForm(page)

    fun submitNumberOfHouseholds(num: Int) = submitNumberOfHouseholds(num.toString())

    fun submitNumberOfHouseholds(num: String) {
        form.householdsInput.fill(num)
        form.submit()
    }

    class HouseholdsForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val householdsInput = TextInput.textByFieldName(locator, "numberOfHouseholds")
    }
}
