package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UpdateEpcCheckYourAnswersBasePage

class UpdateMeesCheckYourAnswersPagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : UpdateEpcCheckYourAnswersBasePage(
        page,
        PropertyComplianceController.getUpdatePropertyComplianceStepPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            PropertyComplianceStepId.UpdateMeesCheckYourAnswers,
        ),
    )
