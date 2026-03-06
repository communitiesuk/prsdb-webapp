package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfHouseholdsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep

class NumberOfHouseholdsFormPagePropertyRegistration(
    page: Page,
) : NumberOfHouseholdsFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${HouseholdStep.ROUTE_SEGMENT}",
    )
