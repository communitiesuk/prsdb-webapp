package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgCharityNumberFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep

abstract class OrgCharityNumberFormPageLandlordRegistration(
    page: Page,
    routeSegment: String,
) : OrgCharityNumberFormBasePage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/$routeSegment")

class OrgCharityNumberEnglandAndWalesFormPageLandlordRegistration(
    page: Page,
) : OrgCharityNumberFormPageLandlordRegistration(page, OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT)

class OrgCharityNumberNorthernIrelandFormPageLandlordRegistration(
    page: Page,
) : OrgCharityNumberFormPageLandlordRegistration(page, OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT)

class OrgCharityNumberScotlandFormPageLandlordRegistration(
    page: Page,
) : OrgCharityNumberFormPageLandlordRegistration(page, OrgCharityNumberScotlandStep.ROUTE_SEGMENT)
