package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NoAddressFoundFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep

class SuperiorLandlordNoAddressFoundFormPagePropertyRegistration(
    page: Page,
) : NoAddressFoundFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
            "${SuperiorLandlordAddressTask.ROUTE_SEGMENT}/${NoAddressFoundStep.ROUTE_SEGMENT}",
    )
