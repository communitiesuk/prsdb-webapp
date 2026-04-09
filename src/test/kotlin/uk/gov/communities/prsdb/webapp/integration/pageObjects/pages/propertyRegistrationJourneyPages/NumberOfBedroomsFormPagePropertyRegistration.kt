package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfBedroomsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep

class NumberOfBedroomsFormPagePropertyRegistration(
    page: Page,
) : NumberOfBedroomsFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${BedroomsStep.ROUTE_SEGMENT}",
    )
