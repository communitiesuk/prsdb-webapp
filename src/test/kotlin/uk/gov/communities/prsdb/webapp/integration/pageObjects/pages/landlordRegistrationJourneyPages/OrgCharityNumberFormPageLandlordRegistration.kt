package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep

open class OrgCharityNumberFormPageLandlordRegistration(
    page: Page,
    routeSegment: String,
) : BasePage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/$routeSegment") {
    val heading: Locator = page.locator("h1")
    val bodyLink: Locator = page.locator("h2.govuk-heading-s + p a.govuk-link")
    val form = CharityNumberForm(page)

    fun submitCharityNumber(charityNumber: String) {
        form.charityNumberInput.fill(charityNumber)
        form.submit()
    }

    class CharityNumberForm(
        page: Page,
    ) : PostForm(page) {
        val charityNumberInput = TextInput.textByFieldName(locator, "charityNumber")
    }
}

class OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration(
    page: Page,
) : OrgCharityNumberFormPageLandlordRegistration(page, OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT)

class OrgCharityNumberNorthernIrelandFormPageLandlordRegistration(
    page: Page,
) : OrgCharityNumberFormPageLandlordRegistration(page, OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT)

class OrgCharityNumberScotlandFormPageLandlordRegistration(
    page: Page,
) : OrgCharityNumberFormPageLandlordRegistration(page, OrgCharityNumberScotlandStep.ROUTE_SEGMENT)
