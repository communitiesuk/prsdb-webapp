package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep

class FreeholderAddressInUkFormPagePropertyRegistration(
    page: Page,
) : PageWithYesNoRadios(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
            "${FreeholderAddressTask.ROUTE_SEGMENT}/${AddressInUkStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val backLink = BackLink.default(page)
}
