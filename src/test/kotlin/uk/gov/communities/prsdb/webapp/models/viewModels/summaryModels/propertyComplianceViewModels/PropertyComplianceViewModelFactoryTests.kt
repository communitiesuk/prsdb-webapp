package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PropertyComplianceViewModelFactoryTests {
    private val gasSafetyViewModelFactory = GasSafetyViewModelFactory(mock())
    private val electricalSafetyViewModelFactory = ElectricalSafetyViewModelFactory(mock())
    private val propertyComplianceViewModelFactory =
        PropertyComplianceViewModelFactory(gasSafetyViewModelFactory, electricalSafetyViewModelFactory)

    private val propertyOwnershipId = 1L

    @Test
    fun `notificationMessages returns correctly populated list when property is compliant`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

        val result = propertyComplianceViewModelFactory.create(propertyCompliance, propertyOwnershipId = propertyOwnershipId)

        assertEquals(expectedNotificationMessages, result.notificationMessages)
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
            val expectedOtherActions = listOf(SummaryCardActionViewModel("forms.links.change", "#"))
            assertEquals(expectedGasSafetyActions, result.gasSafetySummaryCard.actions)
            assertEquals(expectedOtherActions, result.electricalSafetySummaryCard.actions)
            assertEquals(expectedOtherActions, result.epcSummaryCard.actions)
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

    // TODO PDJB-764, PDJB-765, PDJB-766: Reinstate expected notification messages with change links when notifications are re-enabled
    @Nested
    inner class WithNotificationLinks {
        @Test
        fun `notificationMessages returns correctly populated list when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result =
                propertyComplianceViewModelFactory.create(
                    propertyCompliance,
                    landlordView = true,
                    propertyOwnershipId = propertyOwnershipId,
                )

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }
    }

    // TODO PDJB-764, PDJB-765, PDJB-766: Reinstate expected notification messages (without change links) when notifications are re-enabled
    @Nested
    inner class WithoutNotificationLinks {
        @Test
        fun `notificationMessages returns correctly populated list when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

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
