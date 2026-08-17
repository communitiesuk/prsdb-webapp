package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController.Companion.UPDATE_GOVERNING_BODY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.TextFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberNameStep

class OrgGovBodyMemberNameFormPageUpdateGoverningBody(
    page: Page,
) : TextFormPage(page, "$UPDATE_GOVERNING_BODY_ROUTE/${OrgGovBodyMemberNameStep.ROUTE_SEGMENT}")
