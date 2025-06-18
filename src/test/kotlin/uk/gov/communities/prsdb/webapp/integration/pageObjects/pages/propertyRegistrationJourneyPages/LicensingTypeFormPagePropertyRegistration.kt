package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicensingTypeFormPage

class LicensingTypeFormPagePropertyRegistration(
    page: Page,
) : LicensingTypeFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.LicensingType.urlPathSegment}",
    )
