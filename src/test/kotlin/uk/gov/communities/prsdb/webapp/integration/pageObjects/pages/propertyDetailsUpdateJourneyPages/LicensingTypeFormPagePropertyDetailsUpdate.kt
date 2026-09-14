package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateLicensingController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicensingTypeFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep

class LicensingTypeFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LicensingTypeFormPage(
        page,
        LandlordUpdateLicensingController.getUpdateLicensingBaseRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${LicensingTypeStep.ROUTE_SEGMENT}",
    )
