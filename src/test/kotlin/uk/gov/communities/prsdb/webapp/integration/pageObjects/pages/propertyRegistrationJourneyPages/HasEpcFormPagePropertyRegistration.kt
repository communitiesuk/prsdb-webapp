package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasEpcFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep

class HasEpcFormPagePropertyRegistration(
    page: Page,
) : HasEpcFormBasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${HasEpcStep.ROUTE_SEGMENT}")
