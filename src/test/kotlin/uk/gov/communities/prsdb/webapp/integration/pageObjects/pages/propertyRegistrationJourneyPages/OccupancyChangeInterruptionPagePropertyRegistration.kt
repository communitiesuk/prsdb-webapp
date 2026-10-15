package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.InterruptionPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupancyChangeInterruptionStep

class OccupancyChangeInterruptionPagePropertyRegistration(
    page: Page,
) : InterruptionPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${OccupancyChangeInterruptionStep.ROUTE_SEGMENT}",
    ) {
    override val expectedTitleHeading = "Are you sure you want to change this property to unoccupied?"
}
