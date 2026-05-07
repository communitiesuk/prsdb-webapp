package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcInDateAtStartOfTenancyCheckBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep

class EpcInDateAtStartOfTenancyCheckPagePropertyRegistration(
    page: Page,
) : EpcInDateAtStartOfTenancyCheckBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT}",
    )
