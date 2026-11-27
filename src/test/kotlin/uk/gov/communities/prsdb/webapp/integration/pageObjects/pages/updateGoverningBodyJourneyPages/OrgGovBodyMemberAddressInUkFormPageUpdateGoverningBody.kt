package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGoverningBodyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController.Companion.UPDATE_GOVERNING_BODY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask

class OrgGovBodyMemberAddressInUkFormPageUpdateGoverningBody(
    page: Page,
) : PageWithYesNoRadios(
        page,
        "$UPDATE_GOVERNING_BODY_ROUTE/${GovBodyMemberAddressTask.ROUTE_SEGMENT}/${AddressInUkStep.ROUTE_SEGMENT}",
    )
