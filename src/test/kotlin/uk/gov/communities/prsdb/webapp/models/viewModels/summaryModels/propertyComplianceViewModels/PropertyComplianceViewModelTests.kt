package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyComplianceViewModelTests {
    @Test
    fun `notificationMessages returns correctly populated list when property is compliant`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedNotificationMessages = emptyList<PropertyComplianceViewModel.PropertyComplianceNotificationMessage>()

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Nested
    inner class WithNotificationLinks {
        @Test
        fun `notificationMessages returns correctly populated list when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when all certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEicrMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.mainText",
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.linkText",
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }
    }

    @Nested
    inner class WithoutNotificationLinks {
        @Test
        fun `notificationMessages returns correctly populated list when all certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when all certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        null,
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEicrMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.mainText",
                        null,
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }
    }
}
