package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep

class OrgAddressFormPageLandlordRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgAddressStep.ROUTE_SEGMENT}",
    ) {
    val pageHeader: Locator? = page.locator("#section-header")
    val pageTitle: Locator? = page.locator("h1")
    val pageAddressHint: Locator? = page.locator("#manualAddress-hint")
    val pageAddressLineOne: Locator? = page.locator("label[for='addressLineOne']")
    val pageAddressLineTwo: Locator? = page.locator("label[for='addressLineTwo']")
    val pageTownOrCity: Locator? = page.locator("label[for='townOrCity']")
    val pageCounty: Locator? = page.locator("label[for='county']")
    val pagePostcode: Locator? = page.locator("label[for='postcode']")
}
