package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CountryOfResidenceFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}",
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
