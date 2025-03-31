package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.complianceProvisionJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ProvideComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class TaskListPageComplianceProvision(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        "${ProvideComplianceController.getProvideCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong())}/$TASK_LIST_PATH_SEGMENT",
    )
