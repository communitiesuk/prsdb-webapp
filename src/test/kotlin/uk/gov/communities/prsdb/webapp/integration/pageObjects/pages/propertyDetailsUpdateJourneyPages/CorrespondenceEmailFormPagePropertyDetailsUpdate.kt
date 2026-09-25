package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCorrespondenceEmailController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CorrespondenceEmailFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep

class CorrespondenceEmailFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CorrespondenceEmailFormBasePage(
        page,
        UpdateCorrespondenceEmailController.getUpdateCorrespondenceEmailRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${CorrespondenceEmailStep.ROUTE_SEGMENT}",
    )
