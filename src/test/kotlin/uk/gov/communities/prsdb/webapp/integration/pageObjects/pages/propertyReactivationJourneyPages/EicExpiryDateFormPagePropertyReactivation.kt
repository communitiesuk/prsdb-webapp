package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiryDateStep

// TODO PDJB-1679: Remove this page object when the EIC_ISSUE_DATE flag is removed
class EicExpiryDateFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : DateFormPage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${EicExpiryDateStep.ROUTE_SEGMENT}",
    )
