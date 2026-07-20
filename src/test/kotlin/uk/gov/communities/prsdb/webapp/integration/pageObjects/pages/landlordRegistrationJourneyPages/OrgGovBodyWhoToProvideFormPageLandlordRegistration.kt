package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep

class OrgGovBodyWhoToProvideFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT}") {
    val form = OrgGovBodyWhoToProvideForm(page)

    fun submitWhoToProvide(option: GoverningBodyMemberType) {
        form.radios.selectValue(option)
        form.submit()
    }

    class OrgGovBodyWhoToProvideForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val radios = Radios(locator)
    }
}
