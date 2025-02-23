package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class OccupancyFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.Occupancy.urlPathSegment}") {
    val form = OccupancyForm(page)

    fun submitIsOccupied() {
        form.occupancyRadios.selectValue("true")
        form.submit()
    }

    fun submitIsVacant() {
        form.occupancyRadios.selectValue("false")
        form.submit()
    }

    class OccupancyForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val occupancyRadios = Radios(locator)
    }
}
