package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationBannerLink
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage
import kotlin.test.assertEquals

class PropertyDetailsBeforePdjb939NotificationBannerViewModelTests {
    private val complianceMessage =
        NotificationMessage(
            mainText = "compliance.mainText",
            links = listOf(NotificationBannerLink(linkUrl = "#compliance-information", linkText = "compliance.linkText")),
        )

    @Test
    fun `shows the compliance messages unchanged when there is a compliance record`() {
        val banner =
            PropertyDetailsBeforePdjb939NotificationBannerViewModel.fromState(
                isLandlordView = true,
                complianceMessages = listOf(complianceMessage),
            )

        assertEquals(listOf(complianceMessage), banner.messages)
    }

    @Test
    fun `shows no messages when there is a compliance record with no issues`() {
        val banner =
            PropertyDetailsBeforePdjb939NotificationBannerViewModel.fromState(
                isLandlordView = true,
                complianceMessages = emptyList(),
            )

        assertEquals(emptyList(), banner.messages)
    }

    @Test
    fun `shows the landlord no-compliance message when there is no compliance record`() {
        val banner =
            PropertyDetailsBeforePdjb939NotificationBannerViewModel.fromState(
                isLandlordView = true,
                complianceMessages = null,
            )

        assertEquals(
            listOf(NotificationMessage(mainText = "propertyDetails.complianceInformation.noCompliance.landlordView.mainText")),
            banner.messages,
        )
    }

    @Test
    fun `shows the local council no-compliance message when there is no compliance record`() {
        val banner =
            PropertyDetailsBeforePdjb939NotificationBannerViewModel.fromState(
                isLandlordView = false,
                complianceMessages = null,
            )

        assertEquals(
            listOf(NotificationMessage(mainText = "propertyDetails.complianceInformation.noCompliance.localCouncilView.mainText")),
            banner.messages,
        )
    }
}
