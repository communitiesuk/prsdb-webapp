package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep

class AnyoneInOwnershipChainFormPagePropertyRegistration(
    page: Page,
) : PageWithYesNoRadios(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${AnyoneInOwnershipChainStep.ROUTE_SEGMENT}") {
    val backLink = BackLink.default(page)
}
