package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep

class CountryOfResidenceFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${CountryOfResidenceStep.ROUTE_SEGMENT}",
    ) {
    val form = CountryOfResidenceFormLandlord(page)

    fun submitUk() {
        form.selectUk()
        form.submit()
    }

    fun submitNonUk() {
        form.selectNonUk()
        form.submit()
    }

    class CountryOfResidenceFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val residentInUkRadios = Radios(locator)

        fun selectUk() = residentInUkRadios.selectValue("true")

        fun selectNonUk() = residentInUkRadios.selectValue("false")
    }
}
