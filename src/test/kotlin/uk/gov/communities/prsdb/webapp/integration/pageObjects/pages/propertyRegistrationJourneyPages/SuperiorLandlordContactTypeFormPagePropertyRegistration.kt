package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SuperiorLandlordContactTypeFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordContactTypeStep

class SuperiorLandlordContactTypeFormPagePropertyRegistration(
    page: Page,
) : SuperiorLandlordContactTypeFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${SuperiorLandlordContactTypeStep.ROUTE_SEGMENT}",
    )
