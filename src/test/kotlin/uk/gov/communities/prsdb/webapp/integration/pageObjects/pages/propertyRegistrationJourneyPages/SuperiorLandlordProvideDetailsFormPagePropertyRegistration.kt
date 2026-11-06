package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideSuperiorLandlordDetailsStep

class SuperiorLandlordProvideDetailsFormPagePropertyRegistration(
    page: Page,
) : PageWithYesNoRadios(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${ProvideSuperiorLandlordDetailsStep.ROUTE_SEGMENT}",
    ) {
    val backLink = BackLink.default(page)
}
