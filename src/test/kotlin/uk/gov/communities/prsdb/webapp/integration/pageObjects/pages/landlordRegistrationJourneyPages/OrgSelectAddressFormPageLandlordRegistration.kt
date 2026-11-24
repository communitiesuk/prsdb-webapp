package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.OrgInternationalAddressTask

class OrgSelectAddressFormPageLandlordRegistration(
    page: Page,
) : SelectAddressFormPage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgInternationalAddressTask.ROUTE_SEGMENT}/${SelectAddressStep.ROUTE_SEGMENT}",
    )
