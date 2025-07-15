package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when all certs are expired`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                    if (withNotificationLinks) {
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText"
                    } else {
                        null
                    },
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText" else null,
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when gas and eicr certs are expired`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrExpiredCerts()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                    if (withNotificationLinks) {
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText"
                    } else {
                        null
                    },
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when gas and epc certs are expired`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                    if (withNotificationLinks) {
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText"
                    } else {
                        null
                    },
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when eicr and epc certs are expired`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcExpiredCerts()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText" else null,
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when gas cert is expired`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.mainText",
                    if (withNotificationLinks) {
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.expired.linkText"
                    } else {
                        null
                    },
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when eicr cert is expired`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithEicrExpiredAfterUpload()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.eicr.expired.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.eicr.expired.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when epc cert is expired`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.expired.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.expired.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when all certs are missing`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                    if (withNotificationLinks) {
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText"
                    } else {
                        null
                    },
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText" else null,
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when gas and eicr certs are missing`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrMissingCerts()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                    if (withNotificationLinks) {
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText"
                    } else {
                        null
                    },
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when gas and epc certs are missing`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                    if (withNotificationLinks) {
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText"
                    } else {
                        null
                    },
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when eicr and epc certs are missing`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcMissingCerts()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText" else null,
                ),
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when gas cert is missing`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.mainText",
                    if (withNotificationLinks) {
                        "propertyDetails.complianceInformation.notificationBanner.gasCert.missing.linkText"
                    } else {
                        null
                    },
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when eicr cert is missing`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEicrMissingCert()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.eicr.missing.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.eicr.missing.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when epc cert is missing`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.missing.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.missing.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @ParameterizedTest(name = "and withNotificationLinks = {0}")
    @ValueSource(booleans = [true, false])
    fun `notificationMessages returns correctly populated list when epc rating is low`(withNotificationLinks: Boolean) {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

        val expectedNotificationMessages =
            listOf(
                PropertyComplianceViewModel.PropertyComplianceNotificationMessage(
                    "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.mainText",
                    if (withNotificationLinks) "propertyDetails.complianceInformation.notificationBanner.epc.lowRating.linkText" else null,
                ),
            )

        val result = PropertyComplianceViewModel(propertyCompliance, withNotificationLinks = withNotificationLinks)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }
}
