package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep

class OrgIsRegisteredCompanyFormPageLandlordRegistration(
    page: Page,
) : PageWithYesNoRadios(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}")
