package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationBannerLink
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationBannerViewModelServiceTests {
    private val service = NotificationBannerViewModelService()

    private val expectedLinks =
        listOf(
            NotificationBannerLink(
                linkUrl = "#compliance-information",
                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
            ),
        )

    private fun expectedCertLinkMessage(linkTextKey: String) =
        listOf(
            NotificationBannerLink(
                linkUrl = "#compliance-information",
                linkText = linkTextKey,
                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
            ),
        )

    abstract inner class ComplianceMessageTests {
        abstract val landlordView: Boolean

        abstract val expectedMissingMainText: String

        @Test
        fun `returns multipleExpired banner when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns multipleExpired banner when gas and electrical safety certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndElectricalSafetyExpiredCerts()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns multipleExpired banner when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns multipleExpired banner when electrical safety and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyAndEpcExpiredCerts()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns gasCert expired banner when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        links =
                            expectedCertLinkMessage(
                                "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                            ),
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns electricalCert expired banner when electrical safety cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyExpiredAfterUpload()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.electricalCert.expired.mainText",
                        links =
                            expectedCertLinkMessage(
                                "propertyDetails.complianceInformation.notificationBanner.electricalCert.expired.linkText",
                            ),
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns epc expired banner when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        links =
                            expectedCertLinkMessage(
                                "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                            ),
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns empty list when all certs are missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            assertEquals(emptyList(), service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns empty list when gas and electrical safety certs are missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndElectricalSafetyMissingCerts()

            assertEquals(emptyList(), service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns empty list when gas and epc certs are missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            assertEquals(emptyList(), service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns empty list when electrical safety and epc certs are missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyAndEpcMissingCerts()

            assertEquals(emptyList(), service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns empty list when gas cert is missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            assertEquals(emptyList(), service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns empty list when electrical safety cert is missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyElectricalSafetyMissingCert()

            assertEquals(emptyList(), service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns empty list when epc cert is missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            assertEquals(emptyList(), service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns missing banner when occupied property has all certs missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = expectedMissingMainText,
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns missing banner when occupied property has one cert missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert(propertyIsOccupied = true)

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = expectedMissingMainText,
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns missingAndExpired banner when occupied property has missing and expired certs`() {
            val propertyCompliance =
                PropertyComplianceBuilder()
                    .withOccupiedPropertyOwnership()
                    .withExpiredGasSafetyCert()
                    .withElectricalCertType()
                    .withEpc()
                    .build()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missingAndExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns missing banner when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(propertyIsOccupied = true)

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = expectedMissingMainText,
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }

        @Test
        fun `returns missing banner when occupied property has provide-later certs`() {
            val propertyCompliance =
                PropertyComplianceBuilder()
                    .withOccupiedPropertyOwnership(LocalDate.now().minusDays(7))
                    .withGasSafetyCertProvideLater()
                    .withElectricalSafetyCertProvideLater()
                    .withEpcProvideLater()
                    .build()

            val expected =
                listOf(
                    NotificationMessage(
                        mainText = expectedMissingMainText,
                        links = expectedLinks,
                    ),
                )

            assertEquals(expected, service.getComplianceNotificationMessageKeys(propertyCompliance, landlordView))
        }
    }

    @Nested
    inner class LandlordComplianceMessages : ComplianceMessageTests() {
        override val landlordView = true
        override val expectedMissingMainText = "propertyDetails.complianceInformation.notificationBanner.missing.landlord.mainText"
    }

    @Nested
    inner class LocalCouncilComplianceMessages : ComplianceMessageTests() {
        override val landlordView = false
        override val expectedMissingMainText = "propertyDetails.complianceInformation.notificationBanner.missing.localCouncil.mainText"
    }

    @Nested
    inner class BeforePdjb939ComplianceMessages {
        private fun expectedBeforePdjb939Message(mainTextKey: String) =
            listOf(
                NotificationMessage(
                    mainText = mainTextKey,
                    links = expectedLinks,
                ),
            )

        @Test
        fun `landlord flag-off view uses the legacy missing banner`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            assertEquals(
                expectedBeforePdjb939Message("propertyDetails.complianceInformation.notificationBanner.missing.beforePdjb939.mainText"),
                service.getComplianceNotificationMessageKeys(propertyCompliance, isLandlordView = true, beforePdjb939 = true),
            )
        }

        @Test
        fun `landlord flag-off view uses the legacy multipleExpired banner`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            assertEquals(
                expectedBeforePdjb939Message("propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText"),
                service.getComplianceNotificationMessageKeys(propertyCompliance, isLandlordView = true, beforePdjb939 = true),
            )
        }

        @Test
        fun `landlord flag-off view keeps the generic link for a single expired cert`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            assertEquals(
                expectedBeforePdjb939Message("propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText"),
                service.getComplianceNotificationMessageKeys(propertyCompliance, isLandlordView = true, beforePdjb939 = true),
            )
        }
    }

    @Nested
    inner class PropertyDetailsBanner {
        @Test
        fun `surfaces compliance messages when certificates are present and there is no provide-later detail`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val banner =
                service.getPropertyDetailsNotificationBanner(
                    propertyCompliance = propertyCompliance,
                    isLandlordView = true,
                    isOccupied = false,
                    isLicensingProvideLater = false,
                    isTenancyProvideLater = false,
                )

            assertEquals(
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                ),
                banner.messages,
            )
        }
    }

    @Nested
    inner class BeforePdjb939Banner {
        @Test
        fun `landlord view populates the legacy compliance banner`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val messages = service.getBeforePdjb939NotificationBanner(propertyCompliance, isLandlordView = true)

            assertEquals(
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missing.beforePdjb939.mainText",
                        links = expectedLinks,
                    ),
                ),
                messages,
            )
        }

        @Test
        fun `local council view shows no compliance banner`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val messages = service.getBeforePdjb939NotificationBanner(propertyCompliance, isLandlordView = false)

            assertEquals(emptyList(), messages)
        }
    }
}
