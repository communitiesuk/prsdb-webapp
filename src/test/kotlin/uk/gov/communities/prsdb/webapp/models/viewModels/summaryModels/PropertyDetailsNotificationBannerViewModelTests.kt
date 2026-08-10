package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationBannerLink
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyDetailsNotificationBannerViewModelTests {
    companion object {
        private const val PREFIX = "propertyDetails.propertyRecord.notificationBanner"
    }

    private val complianceMessage =
        NotificationMessage(
            mainText = "compliance.mainText",
            links = listOf(NotificationBannerLink(linkUrl = "#compliance-information", linkText = "compliance.linkText")),
        )

    private fun fromState(
        isLandlordView: Boolean = true,
        isOccupied: Boolean = true,
        isLicensingProvideLater: Boolean = false,
        isTenancyProvideLater: Boolean = false,
        complianceMessages: List<NotificationMessage> = emptyList(),
    ) = PropertyDetailsNotificationBannerViewModel.fromState(
        isLandlordView = isLandlordView,
        isOccupied = isOccupied,
        isLicensingProvideLater = isLicensingProvideLater,
        isTenancyProvideLater = isTenancyProvideLater,
        complianceMessages = complianceMessages,
    )

    @Test
    fun `messages is empty when there is no provide-later state and no compliance issue`() {
        assertTrue(fromState().messages.isEmpty())
    }

    @Test
    fun `shows only the compliance messages when there is no provide-later state`() {
        val banner = fromState(complianceMessages = listOf(complianceMessage))

        assertEquals(listOf(complianceMessage), banner.messages)
    }

    @Test
    fun `ignores provide-later state and shows compliance messages when the property is not occupied`() {
        val banner =
            fromState(
                isOccupied = false,
                isLicensingProvideLater = true,
                complianceMessages = listOf(complianceMessage),
            )

        assertEquals(listOf(complianceMessage), banner.messages)
    }

    @Test
    fun `shows the landlord licensing provide-later message`() {
        val banner = fromState(isLicensingProvideLater = true)

        assertEquals(
            listOf(
                NotificationMessage(
                    mainText = "$PREFIX.landlord.licensing.mainText",
                    links =
                        listOf(
                            NotificationBannerLink(
                                linkUrl = "#property-details",
                                linkText = "$PREFIX.landlord.licensing.linkText",
                                afterLinkText = "$PREFIX.afterLinkText",
                            ),
                        ),
                ),
            ),
            banner.messages,
        )
    }

    @Test
    fun `shows the local council tenancy provide-later message`() {
        val banner = fromState(isLandlordView = false, isTenancyProvideLater = true)

        assertEquals(
            listOf(
                NotificationMessage(
                    mainText = "$PREFIX.localCouncil.tenancy.mainText",
                    links =
                        listOf(
                            NotificationBannerLink(
                                linkUrl = "#property-details",
                                linkText = "$PREFIX.localCouncil.tenancy.linkText",
                                afterLinkText = "$PREFIX.afterLinkText",
                            ),
                        ),
                ),
            ),
            banner.messages,
        )
    }

    @Test
    fun `shows the landlord both provide-later message with two links`() {
        val banner = fromState(isLicensingProvideLater = true, isTenancyProvideLater = true)

        assertEquals(
            listOf(
                NotificationMessage(
                    links =
                        listOf(
                            NotificationBannerLink(
                                linkUrl = "#property-details",
                                linkText = "$PREFIX.landlord.licensingAndTenancy.licensingLinkText",
                                beforeLinkText = "$PREFIX.landlord.licensingAndTenancy.beforeLinkText",
                                afterLinkText = "$PREFIX.landlord.licensingAndTenancy.middleText",
                            ),
                            NotificationBannerLink(
                                linkUrl = "#property-details",
                                linkText = "$PREFIX.landlord.licensingAndTenancy.tenancyLinkText",
                                afterLinkText = "$PREFIX.afterLinkText",
                            ),
                        ),
                ),
            ),
            banner.messages,
        )
    }

    @Test
    fun `shows the local council both provide-later message with a single link`() {
        val banner = fromState(isLandlordView = false, isLicensingProvideLater = true, isTenancyProvideLater = true)

        assertEquals(
            listOf(
                NotificationMessage(
                    mainText = "$PREFIX.localCouncil.licensingAndTenancy.mainText",
                    links =
                        listOf(
                            NotificationBannerLink(
                                linkUrl = "#property-details",
                                linkText = "$PREFIX.localCouncil.licensingAndTenancy.linkText",
                                afterLinkText = "$PREFIX.afterLinkText",
                            ),
                        ),
                ),
            ),
            banner.messages,
        )
    }

    @Test
    fun `shows the landlord combined message when there is both a provide-later state and a compliance issue`() {
        val banner =
            fromState(
                isLicensingProvideLater = true,
                complianceMessages = listOf(complianceMessage),
            )

        assertEquals(
            listOf(
                NotificationMessage(
                    links =
                        listOf(
                            NotificationBannerLink(
                                linkUrl = "#property-details",
                                linkText = "$PREFIX.landlord.propertyAndCompliance.propertyLinkText",
                                beforeLinkText = "$PREFIX.landlord.propertyAndCompliance.beforeLinkText",
                                afterLinkText = "$PREFIX.landlord.propertyAndCompliance.middleText",
                            ),
                            NotificationBannerLink(
                                linkUrl = "#compliance-information",
                                linkText = "$PREFIX.landlord.propertyAndCompliance.complianceLinkText",
                                afterLinkText = "$PREFIX.afterLinkText",
                            ),
                        ),
                ),
            ),
            banner.messages,
        )
    }

    @Test
    fun `shows the local council combined message when there is both a provide-later state and a compliance issue`() {
        val banner =
            fromState(
                isLandlordView = false,
                isTenancyProvideLater = true,
                complianceMessages = listOf(complianceMessage),
            )

        assertEquals(
            listOf(
                NotificationMessage(
                    links =
                        listOf(
                            NotificationBannerLink(
                                linkUrl = "#property-details",
                                linkText = "$PREFIX.localCouncil.propertyAndCompliance.propertyLinkText",
                                beforeLinkText = "$PREFIX.localCouncil.propertyAndCompliance.beforeLinkText",
                                afterLinkText = "$PREFIX.localCouncil.propertyAndCompliance.middleText",
                            ),
                            NotificationBannerLink(
                                linkUrl = "#compliance-information",
                                linkText = "$PREFIX.localCouncil.propertyAndCompliance.complianceLinkText",
                                afterLinkText = "$PREFIX.afterLinkText",
                            ),
                        ),
                ),
            ),
            banner.messages,
        )
    }
}
