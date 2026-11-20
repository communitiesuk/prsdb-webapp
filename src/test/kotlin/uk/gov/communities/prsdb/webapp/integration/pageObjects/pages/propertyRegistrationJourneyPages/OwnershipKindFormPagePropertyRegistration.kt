package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OwnershipKindFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep

class OwnershipKindFormPagePropertyRegistration(
    page: Page,
) : OwnershipKindFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${OwnershipKindStep.ROUTE_SEGMENT}",
    )
