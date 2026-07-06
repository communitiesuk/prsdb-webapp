package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordType

class LandlordTypePageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${LandlordTypeStep.ROUTE_SEGMENT}") {
    val form = LandlordTypeForm(page)

    fun submitLandlordType(landlordType: LandlordType) {
        form.landlordTypeRadios.selectValue(landlordType)
        form.submit()
    }

    class LandlordTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val landlordTypeRadios = Radios(locator)
    }
}
