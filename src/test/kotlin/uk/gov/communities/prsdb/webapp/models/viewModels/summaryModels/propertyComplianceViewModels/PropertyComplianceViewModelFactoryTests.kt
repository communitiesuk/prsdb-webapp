package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyComplianceViewModelFactoryTests {
    private val gasSafetyViewModelFactory = GasSafetyViewModelFactory(mock())
    private val eicrViewModelFactory = EicrViewModelFactory(mock())
    private val propertyComplianceViewModelFactory = PropertyComplianceViewModelFactory(gasSafetyViewModelFactory, eicrViewModelFactory)

    @Test
    fun `notificationMessages returns correctly populated list when property is compliant`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

        val result = propertyComplianceViewModelFactory.create(propertyCompliance)

        assertEquals(expectedNotificationMessages, result.notificationMessages)
    }

    @Test
    fun `landlordResponsibilitiesHintText returns correct message when landlord view is true`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedMessage = "propertyDetails.complianceInformation.landlordResponsibilities.landlord.hintText"

        val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

        assertEquals(expectedMessage, result.landlordResponsibilitiesHintText)
    }

    @Test
    fun `landlordResponsibilitiesHintText returns  returns correct message when landlord view is false`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedMessage = "propertyDetails.complianceInformation.landlordResponsibilities.localCouncil.hintText"

        val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

        assertEquals(expectedMessage, result.landlordResponsibilitiesHintText)
    }

    // TODO PDJB-80: Reinstate expected notification messages with change links when notifications are re-enabled
    @Nested
    inner class WithNotificationLinks {
        @Test
        fun `notificationMessages returns correctly populated list when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrExpiredAfterUpload()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when all certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEicrMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = true)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }
    }

    // TODO PDJB-80: Reinstate expected notification messages (without change links) when notifications are re-enabled
    @Nested
    inner class WithoutNotificationLinks {
        @Test
        fun `notificationMessages returns correctly populated list when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcExpiredCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrExpiredAfterUpload()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when all certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcMissingCerts()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEicrMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

            val result = propertyComplianceViewModelFactory.create(propertyCompliance, landlordView = false)

            assertEquals(expectedNotificationMessages, result.notificationMessages)
        }
    }
}
