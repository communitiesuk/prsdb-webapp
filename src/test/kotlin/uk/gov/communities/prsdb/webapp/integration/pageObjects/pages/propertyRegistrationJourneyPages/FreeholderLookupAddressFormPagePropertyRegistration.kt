package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep

class FreeholderLookupAddressFormPagePropertyRegistration(
    page: Page,
) : LookupAddressFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
            "${FreeholderAddressTask.ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}",
    )
