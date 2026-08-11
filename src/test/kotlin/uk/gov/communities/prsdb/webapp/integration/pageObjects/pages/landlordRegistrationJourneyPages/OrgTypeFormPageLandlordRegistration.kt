package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgTypeFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

class OrgTypeFormPageLandlordRegistration(
    page: Page,
) : OrgTypeFormPage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgTypeStep.ROUTE_SEGMENT}")
