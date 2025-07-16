package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsBasePage

class PropertyDetailsPageLandlordView(
    page: Page,
    urlArguments: Map<String, String>,
) : PropertyDetailsBasePage(
        page,
        PropertyDetailsController.getPropertyDetailsPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            isLaView = false,
        ),
    ) {
    val deleteButton = Button.byText(page, "Delete property record")

    val notificationBanner = NotificationBanner(page)
}
