package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentAmountFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep

class RentAmountFormPagePropertyRegistration(
    page: Page,
) : RentAmountFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RentAmountStep.ROUTE_SEGMENT}",
    )
