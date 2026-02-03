package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NoAddressFoundFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep

class NoAddressFoundFormPageLandlordRegistration(
    page: Page,
) : NoAddressFoundFormPage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${NoAddressFoundStep.ROUTE_SEGMENT}",
    )
