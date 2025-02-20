package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Select
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CountryOfResidenceFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
    ) {
    val form = CountryOfResidenceFormLandlord(page)

    fun submitUk() {
        form.selectUk()
        form.submit()
    }

    fun submitNonUkFromPartial(
        countryFragment: String,
        countryNameFull: String,
    ) {
        form.selectNonUk()
        form.countrySelect.autocompleteInput.fill(countryFragment)
        form.countrySelect.selectValue(countryNameFull)
        form.submit()
    }

    class CountryOfResidenceFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val residentInUkRadios = Radios(locator)
        val countrySelect = Select(locator)

        fun selectUk() = residentInUkRadios.selectValue("true")

        fun selectNonUk() = residentInUkRadios.selectValue("false")
    }
}
