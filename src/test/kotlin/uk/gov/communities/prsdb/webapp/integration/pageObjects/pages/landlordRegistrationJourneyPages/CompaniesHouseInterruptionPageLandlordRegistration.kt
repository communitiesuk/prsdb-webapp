package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.InterruptionPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep

class CompaniesHouseInterruptionPageLandlordRegistration(
    page: Page,
) : InterruptionPage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT}")
