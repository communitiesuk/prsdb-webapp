package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask

class LeadTrusteeAddressFormPageUpdateOrganisationType(
    page: Page,
) : LookupAddressFormPage(
        page,
        "$UPDATE_ORG_TYPE_ROUTE/${TrusteeAddressTask.ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}",
    )
