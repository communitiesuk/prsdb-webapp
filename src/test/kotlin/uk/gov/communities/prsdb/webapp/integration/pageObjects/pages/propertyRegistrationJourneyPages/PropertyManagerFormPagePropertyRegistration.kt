package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep

class PropertyManagerFormPagePropertyRegistration(
    page: Page,
) : PageWithYesNoRadios(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PropertyManagerStep.ROUTE_SEGMENT}")
