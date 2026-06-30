package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityStep

class OrgCharityFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgCharityStep.ROUTE_SEGMENT}",
    ) {
    val form = OrgCharityFormLandlord(page)

    fun submitYes() {
        form.selectYes()
        form.submit()
    }

    fun submitNo() {
        form.selectNo()
        form.submit()
    }

    class OrgCharityFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val charityRadios = Radios(locator)
        val hint = Heading(locator.locator("#charity-hint"))
        val yesRadioLabel = Heading(locator.locator("label[for='charity-yes']"))
        val noRadioLabel = Heading(locator.locator("label[for='charity-no']"))

        fun selectYes() = charityRadios.selectValue("true")

        fun selectNo() = charityRadios.selectValue("false")
    }
}
