package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep

class SuperiorLandlordManualAddressFormPagePropertyRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
            "${SuperiorLandlordAddressTask.ROUTE_SEGMENT}/${ManualAddressStep.ROUTE_SEGMENT}",
    )
