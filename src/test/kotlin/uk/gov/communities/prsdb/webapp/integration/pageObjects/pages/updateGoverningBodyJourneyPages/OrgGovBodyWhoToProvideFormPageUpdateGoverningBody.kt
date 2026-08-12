package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController.Companion.UPDATE_GOVERNING_BODY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgGovBodyWhoToProvideFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep

class OrgGovBodyWhoToProvideFormPageUpdateGoverningBody(
    page: Page,
) : OrgGovBodyWhoToProvideFormPage(page, "$UPDATE_GOVERNING_BODY_ROUTE/${OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT}")
