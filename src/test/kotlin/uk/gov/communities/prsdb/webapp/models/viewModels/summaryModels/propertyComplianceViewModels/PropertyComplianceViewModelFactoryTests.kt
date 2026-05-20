package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyComplianceViewModelFactoryTests {
    private val gasSafetyViewModelFactory = GasSafetyViewModelFactory(mock())
    private val electricalSafetyViewModelFactory = ElectricalSafetyViewModelFactory(mock())
    private val propertyComplianceViewModelFactory =
        PropertyComplianceViewModelFactory(gasSafetyViewModelFactory, electricalSafetyViewModelFactory)

    private val propertyOwnershipId = 1L

    private val expectedLinkMessage =
        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
            linkUrl = "#compliance-information",
            linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
            afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
            isAfterLinkTextFullStop = true,
        )

    @Test
    fun `notificationMessages returns correctly populated list when property is compliant`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

        val result = propertyComplianceViewModelFactory.create(propertyCompliance, propertyOwnershipId = propertyOwnershipId)

        assertEquals(expectedNotificationMessages, result.notificationMessages)
        assertTrue(result.isAllValid)
    }

    @Nested
    inner class CardActions {
        @Test
        fun `cards have change actions when landlordView is true`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()
            val propertyOwnershipId = propertyCompliance.propertyOwnership.id

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            val expectedGasSafetyActions =
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateGasSafetyController.getUpdateGasSafetyFirstStepRoute(propertyOwnershipId),
                    ),
                )

            val expectedElectricalSafetyActions =
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateElectricalSafetyController.getUpdateElectricalSafetyFirstStepRoute(propertyOwnershipId),
                    ),
                )

            val expectedEpcActions =
                listOf(
                    SummaryCardActionViewModel(
                        "forms.links.change",
                        UpdateEpcController.getUpdateEpcRouteFirstStep(propertyOwnershipId),
                    ),
                )
            assertEquals(expectedGasSafetyActions, result.gasSafetySummaryCard.actions)
            assertEquals(expectedElectricalSafetyActions, result.electricalSafetySummaryCard.actions)
            assertEquals(expectedEpcActions, result.epcSummaryCard.actions)
        }

        @Test
        fun `cards have no change actions when landlordView is false`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertNull(result.gasSafetySummaryCard.actions)
            assertNull(result.electricalSafetySummaryCard.actions)
            assertNull(result.epcSummaryCard.actions)
        }
    }

    @Nested
    inner class LandlordViewNotifications {
        @Test
        fun `notificationMessages returns correctly populated list when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and electrical safety certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndElectricalSafetyExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when electrical safety and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when electrical safety cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.electricalCert.expired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when all certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and electrical safety certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndElectricalSafetyMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when electrical safety and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyAndEpcMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when electrical safety cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyElectricalSafetyMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns missing banner when occupied property has all certs missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missing.mainText",
                        linkMessage = expectedLinkMessage,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns missing banner when occupied property has one cert missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert(propertyIsOccupied = true)

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missing.mainText",
                        linkMessage = expectedLinkMessage,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns missingAndExpired banner when occupied property has missing and expired certs`() {
            val propertyCompliance =
                PropertyComplianceBuilder()
                    .withOccupiedPropertyOwnership()
                    .withExpiredGasSafetyCert()
                    .withElectricalCertType()
                    .withEpc()
                    .build()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missingAndExpired.mainText",
                        linkMessage = expectedLinkMessage,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }
    }

    @Nested
    inner class NonLandlordViewNotifications {
        @Test
        fun `notificationMessages returns correctly populated list when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and electrical safety certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndElectricalSafetyExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when electrical safety and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when electrical safety cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.electricalCert.expired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when all certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and electrical safety certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndElectricalSafetyMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when electrical safety and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyAndEpcMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when electrical safety cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyElectricalSafetyMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
            assertTrue(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns missing banner when occupied property has all certs missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missing.mainText",
                        linkMessage = expectedLinkMessage,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns missing banner when occupied property has one cert missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert(propertyIsOccupied = true)

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missing.mainText",
                        linkMessage = expectedLinkMessage,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns missingAndExpired banner when occupied property has missing and expired certs`() {
            val propertyCompliance =
                PropertyComplianceBuilder()
                    .withOccupiedPropertyOwnership()
                    .withExpiredGasSafetyCert()
                    .withElectricalCertType()
                    .withEpc()
                    .build()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missingAndExpired.mainText",
                        linkMessage = expectedLinkMessage,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        linkMessage =
                            PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                                isAfterLinkTextFullStop = true,
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }
    }
}
