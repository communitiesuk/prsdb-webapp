package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
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
    val deleteButton = Button.byText(page, "Delete property")

    val notificationBanner = NotificationBannerPropertyDetailsLandlordView(page)

    class NotificationBannerPropertyDetailsLandlordView(
        page: Page,
    ) : NotificationBanner(page) {
        val updateMissingGasSafetyLink = Link.byText(page, "Upload a certificate")
        val updateMissingEicrLink = Link.byText(page, "Upload an EICR")
        val addEpcLink = Link.byText(page, "Add a new certificate")
        val updateExpiredGasSafetyLink = Link.byText(page, "Upload a new certificate")
        val updateExpiredEicrLink = Link.byText(page, "Upload a new EICR")
        val addEpcOrMeesExemptionLink = Link.byText(page, "add a new certificate or add a MEES exemption")
    }
}
