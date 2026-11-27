package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask

class LeadTrusteeAddressInUkFormPageUpdateOrganisationType(
    page: Page,
) : PageWithYesNoRadios(
        page,
        "$UPDATE_ORG_TYPE_ROUTE/${TrusteeAddressTask.ROUTE_SEGMENT}/${AddressInUkStep.ROUTE_SEGMENT}",
    )
