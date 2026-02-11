package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasJointLandlordsFormBasePage

class HasJointLandlordsFormBasePagePropertyRegistration(
    page: Page,
) : HasJointLandlordsFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.HasJointLandlords.urlPathSegment}",
    )
