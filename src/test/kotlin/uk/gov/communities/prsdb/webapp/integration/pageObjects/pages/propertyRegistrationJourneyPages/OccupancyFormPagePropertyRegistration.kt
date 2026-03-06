package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OccupancyFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep

class OccupancyFormPagePropertyRegistration(
    page: Page,
) : OccupancyFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${OccupiedStep.ROUTE_SEGMENT}",
    )
