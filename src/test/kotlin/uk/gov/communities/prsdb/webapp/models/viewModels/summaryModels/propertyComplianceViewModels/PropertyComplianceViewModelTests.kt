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

    @Test
    fun `landlordResponsibilitiesHintText returns correct message when landlord view is true`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedMessage = "propertyDetails.complianceInformation.landlordResponsibilities.landlord.hintText"

        val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

        assertEquals(result.landlordResponsibilitiesHintText, expectedMessage)
    }

    @Test
    fun `landlordResponsibilitiesHintText returns  returns correct message when landlord view is false`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedMessage = "propertyDetails.complianceInformation.landlordResponsibilities.localAuthority.hintText"

        val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

        assertEquals(result.landlordResponsibilitiesHintText, expectedMessage)
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
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-gas-safety-certificate",
                            "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-eicr",
                            "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-gas-safety-certificate",
                            "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-eicr",
                            "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-gas-safety-certificate",
                            "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcExpiredCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-eicr",
                            "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-gas-safety-certificate",
                            "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrExpiredAfterUpload()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-eicr",
                            "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is expired`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when all certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-gas-safety-certificate",
                            "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-eicr",
                            "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and eicr certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-gas-safety-certificate",
                            "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-eicr",
                            "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-gas-safety-certificate",
                            "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr and epc certs are missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcMissingCerts()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-eicr",
                            "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when gas cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-gas-safety-certificate",
                            "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when eicr cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEicrMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-eicr",
                            "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc cert is missing`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.asSoonAsPossible",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }

        @Test
        fun `notificationMessages returns correctly populated list when epc rating is low`() {
            val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

            val expectedNotificationMessages =
                listOf(
                    PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                        "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.mainText",
                        PropertyComplianceViewModel.PropertyComplianceLinkMessage(
                            "/landlord/provide-compliance-certificates/1/update/update-epc",
                            "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.linkText",
                            "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.afterLinkText",
                            "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.beforeLinkText",
                        ),
                    ),
                )

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = true)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

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

            val result = PropertyComplianceViewModel(propertyCompliance, landlordView = false)

            assertEquals(result.notificationMessages, expectedNotificationMessages)
        }
    }
}
