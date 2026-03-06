package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BillsIncludedFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep

class BillsIncludedFormPagePropertyRegistration(
    page: Page,
) : BillsIncludedFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${BillsIncludedStep.ROUTE_SEGMENT}",
    )
