package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationBannerLink
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel.NotificationMessage
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyComplianceViewModelFactoryTests {
    private val mockMessageSource: MessageSource = mock()

    init {
        whenever(mockMessageSource.getMessage(any(), any(), any())).thenReturn("")
    }

    private val gasSafetyViewModelFactory = GasSafetyViewModelFactory(mock(), mockMessageSource)
    private val electricalSafetyViewModelFactory = ElectricalSafetyViewModelFactory(mock(), mockMessageSource)
    private val propertyComplianceViewModelFactory =
        PropertyComplianceViewModelFactory(
            gasSafetyViewModelFactory,
            electricalSafetyViewModelFactory,
            EpcViewModelFactory(mockMessageSource),
            NotificationBannerViewModelService(),
        )

    private val propertyOwnershipId = 1L

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

    @Test
    fun `notificationMessages returns correctly populated list when property is compliant`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedNotificationMessages = emptyList<NotificationMessage>()

        val result = propertyComplianceViewModelFactory.create(propertyCompliance, propertyOwnershipId = propertyOwnershipId)

        assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
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
                        "propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc",
                        "${PropertyComplianceBuilder.TEST_EPC_BASE_URL}/0000-0000-0000-0000-0000",
                        opensInNewTab = true,
                    ),
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
            assertEquals(
                listOf(
                    SummaryCardActionViewModel(
                        "propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc",
                        "${PropertyComplianceBuilder.TEST_EPC_BASE_URL}/0000-0000-0000-0000-0000",
                        opensInNewTab = true,
                    ),
                ),
                result.epcSummaryCard.actions,
            )
        }
    }

    abstract inner class NotificationTests {
        abstract val landlordView: Boolean

        abstract val expectedMissingMainText: String

        // The NotificationTests scenarios all exercise the flag-on (provide-later) banner.
        val provideLaterEnabled = true

        @Test
        fun `notificationMessages returns multipleExpired banner when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns multipleExpired banner when gas and electrical safety certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndElectricalSafetyExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns multipleExpired banner when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns multipleExpired banner when electrical safety and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns gasCert expired banner when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        links =
                            expectedCertLinkMessage(
                                "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns electricalCert expired banner when electrical safety cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.electricalCert.expired.mainText",
                        links =
                            expectedCertLinkMessage(
                                "propertyDetails.complianceInformation.notificationBanner.electricalCert.expired.linkText",
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns epc expired banner when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        links =
                            expectedCertLinkMessage(
                                "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                            ),
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns empty list when all certs are missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns empty list when gas and electrical safety certs are missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndElectricalSafetyMissingCerts()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns empty list when gas and epc certs are missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns empty list when electrical safety and epc certs are missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithElectricalSafetyAndEpcMissingCerts()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns empty list when gas cert is missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns empty list when electrical safety cert is missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyElectricalSafetyMissingCert()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns empty list when epc cert is missing on unoccupied property`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns missing banner when occupied property has all certs missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = expectedMissingMainText,
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns missing banner when occupied property has one cert missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert(propertyIsOccupied = true)

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = expectedMissingMainText,
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
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
                    NotificationMessage(
                        mainText = "propertyDetails.complianceInformation.notificationBanner.missingAndExpired.mainText",
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages returns missing banner when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(propertyIsOccupied = true)

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = expectedMissingMainText,
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }

        @Test
        fun `notificationMessages returns missing banner when occupied property has provide-later certs`() {
            val propertyCompliance =
                PropertyComplianceBuilder()
                    .withOccupiedPropertyOwnership(LocalDate.now().minusDays(7))
                    .withGasSafetyCertProvideLater()
                    .withElectricalSafetyCertProvideLater()
                    .withEpcProvideLater()
                    .build()

            val expectedNotificationMessages =
                listOf(
                    NotificationMessage(
                        mainText = expectedMissingMainText,
                        links = expectedLinks,
                    ),
                )

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = landlordView,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = provideLaterEnabled,
                )

            assertEquals(expectedNotificationMessages, result.complianceNotificationMessages)
            assertFalse(result.isAllValid)
        }
    }

    @Nested
    inner class LandlordViewNotifications : NotificationTests() {
        override val landlordView = true
        override val expectedMissingMainText = "propertyDetails.complianceInformation.notificationBanner.missing.landlord.mainText"
    }

    @Nested
    inner class NonLandlordViewNotifications : NotificationTests() {
        override val landlordView = false
        override val expectedMissingMainText = "propertyDetails.complianceInformation.notificationBanner.missing.localCouncil.mainText"
    }

    @Nested
    inner class LocalCouncilViewNotifications {
        @Test
        fun `notificationMessages is empty when occupied property has all certs missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages is empty when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages is empty when occupied property has missing and expired certs`() {
            val propertyCompliance =
                PropertyComplianceBuilder()
                    .withOccupiedPropertyOwnership()
                    .withExpiredGasSafetyCert()
                    .withElectricalCertType()
                    .withEpc()
                    .build()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
        }

        @Test
        fun `notificationMessages is empty when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(propertyIsOccupied = true)

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(emptyList(), result.complianceNotificationMessages)
        }
    }

    // Covers the flag-off (beforePdjb939) compliance banner.
    @Nested
    inner class BeforePdjb939Notifications {
        private fun expectedBeforePdjb939Message(mainTextKey: String) =
            listOf(
                NotificationMessage(
                    mainText = mainTextKey,
                    links =
                        listOf(
                            NotificationBannerLink(
                                linkUrl = "#compliance-information",
                                linkText = "propertyDetails.complianceInformation.notificationBanner.viewComplianceCertificates",
                                afterLinkText = "propertyDetails.complianceInformation.notificationBanner.afterLinkText",
                            ),
                        ),
                ),
            )

        @Test
        fun `landlord flag-off view populates the legacy missing banner`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = false,
                )

            assertEquals(
                expectedBeforePdjb939Message("propertyDetails.complianceInformation.notificationBanner.missing.beforePdjb939.mainText"),
                result.beforePdjb939ComplianceNotificationMessages,
            )
            assertEquals(emptyList(), result.complianceNotificationMessages)
        }

        @Test
        fun `landlord flag-off view populates the legacy multipleExpired banner`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = false,
                )

            assertEquals(
                expectedBeforePdjb939Message("propertyDetails.complianceInformation.notificationBanner.multipleExpired.mainText"),
                result.beforePdjb939ComplianceNotificationMessages,
            )
        }

        @Test
        fun `local council flag-off view populates no legacy banner`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true)

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = false,
                    propertyOwnershipId = propertyOwnershipId,
                    provideLaterEnabled = false,
                )

            assertEquals(emptyList(), result.beforePdjb939ComplianceNotificationMessages)
            assertEquals(emptyList(), result.complianceNotificationMessages)
        }
    }
}
