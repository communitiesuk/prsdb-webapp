package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgCompanyNumberFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep

class OrgCompanyNumberFormPageLandlordRegistration(
    page: Page,
) : OrgCompanyNumberFormBasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgCompanyNumberStep.ROUTE_SEGMENT}")
