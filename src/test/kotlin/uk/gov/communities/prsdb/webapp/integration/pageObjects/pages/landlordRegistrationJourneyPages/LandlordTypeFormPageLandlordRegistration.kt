package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep

class LandlordTypeFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordTypeStep.ROUTE_SEGMENT}",
    ) {
    val form = LandlordTypeFormLandlord(page)

    fun submitIndividual() {
        form.selectIndividual()
        form.submit()
    }

    fun submitOrganisation() {
        form.selectOrganisation()
        form.submit()
    }

    class LandlordTypeFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val landlordTypeRadios = Radios(locator)

        fun selectIndividual() = landlordTypeRadios.selectValue("INDIVIDUAL")

        fun selectOrganisation() = landlordTypeRadios.selectValue("ORGANISATION")
    }
}
