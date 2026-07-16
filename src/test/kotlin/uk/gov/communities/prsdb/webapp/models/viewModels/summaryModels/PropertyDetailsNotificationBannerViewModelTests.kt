package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class PropertyDetailsNotificationBannerViewModelTests {
    @Test
    fun `fromState returns null when the provide-later flag is disabled`() {
        assertNull(
            PropertyDetailsNotificationBannerViewModel.fromState(
                provideLaterEnabled = false,
                isOccupied = true,
                isLicensingProvideLater = true,
                isTenancyProvideLater = true,
                hasComplianceIssue = true,
            ),
        )
    }

    @Test
    fun `fromState returns null when the property is not occupied`() {
        assertNull(
            PropertyDetailsNotificationBannerViewModel.fromState(
                provideLaterEnabled = true,
                isOccupied = false,
                isLicensingProvideLater = true,
                isTenancyProvideLater = true,
                hasComplianceIssue = true,
            ),
        )
    }

    @Test
    fun `fromState returns null when there is no provide-later state and no compliance issue`() {
        assertNull(
            PropertyDetailsNotificationBannerViewModel.fromState(
                provideLaterEnabled = true,
                isOccupied = true,
                isLicensingProvideLater = false,
                isTenancyProvideLater = false,
                hasComplianceIssue = false,
            ),
        )
    }

    @Test
    fun `fromState returns null when only a compliance issue exists (compliance banner handles it)`() {
        assertNull(
            PropertyDetailsNotificationBannerViewModel.fromState(
                provideLaterEnabled = true,
                isOccupied = true,
                isLicensingProvideLater = false,
                isTenancyProvideLater = false,
                hasComplianceIssue = true,
            ),
        )
    }

    @ParameterizedTest(name = "licensing={1} tenancy={2} complianceIssue={3} -> {4}")
    @MethodSource("bannerVariantCases")
    fun `fromState resolves the expected banner variant`(
        @Suppress("UNUSED_PARAMETER") description: String,
        isLicensingProvideLater: Boolean,
        isTenancyProvideLater: Boolean,
        hasComplianceIssue: Boolean,
        expectedVariant: PropertyDetailsNotificationBannerViewModel.Variant,
    ) {
        val banner =
            PropertyDetailsNotificationBannerViewModel.fromState(
                provideLaterEnabled = true,
                isOccupied = true,
                isLicensingProvideLater = isLicensingProvideLater,
                isTenancyProvideLater = isTenancyProvideLater,
                hasComplianceIssue = hasComplianceIssue,
            )

        assertEquals(expectedVariant, banner?.variant)
    }

    @Test
    fun `suppressesComplianceBanner is true only for the combined variant`() {
        val combined =
            PropertyDetailsNotificationBannerViewModel.fromState(
                provideLaterEnabled = true,
                isOccupied = true,
                isLicensingProvideLater = true,
                isTenancyProvideLater = true,
                hasComplianceIssue = true,
            )
        val both =
            PropertyDetailsNotificationBannerViewModel.fromState(
                provideLaterEnabled = true,
                isOccupied = true,
                isLicensingProvideLater = true,
                isTenancyProvideLater = true,
                hasComplianceIssue = false,
            )

        assertEquals(PropertyDetailsNotificationBannerViewModel.Variant.COMBINED, combined?.variant)
        assertEquals(true, combined?.suppressesComplianceBanner)
        assertEquals(PropertyDetailsNotificationBannerViewModel.Variant.BOTH, both?.variant)
        assertEquals(false, both?.suppressesComplianceBanner)
    }

    companion object {
        @JvmStatic
        fun bannerVariantCases() =
            listOf(
                Arguments.of("licensing only", true, false, false, PropertyDetailsNotificationBannerViewModel.Variant.LICENSING),
                Arguments.of("tenancy only", false, true, false, PropertyDetailsNotificationBannerViewModel.Variant.TENANCY),
                Arguments.of("both", true, true, false, PropertyDetailsNotificationBannerViewModel.Variant.BOTH),
                Arguments.of(
                    "licensing plus compliance issue",
                    true,
                    false,
                    true,
                    PropertyDetailsNotificationBannerViewModel.Variant.COMBINED,
                ),
                Arguments.of(
                    "tenancy plus compliance issue",
                    false,
                    true,
                    true,
                    PropertyDetailsNotificationBannerViewModel.Variant.COMBINED,
                ),
                Arguments.of("both plus compliance issue", true, true, true, PropertyDetailsNotificationBannerViewModel.Variant.COMBINED),
            )
    }
}
