package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyComplianceViewModelTests {
    @Test
    fun `notificationMessages returns correctly populated list when property is compliant`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()

        val expectedNotificationMessages = emptyList<String>()

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when all certs are expired`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithExpiredCerts()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.gasCert.expired",
                "propertyDetails.complianceInformation.notificationMessage.eicr.expired",
                "propertyDetails.complianceInformation.notificationMessage.epc.expired",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when gas and eicr certs are expired`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrExpiredCerts()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.gasCert.expired",
                "propertyDetails.complianceInformation.notificationMessage.eicr.expired",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when gas and epc certs are expired`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcExpiredCerts()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.gasCert.expired",
                "propertyDetails.complianceInformation.notificationMessage.epc.expired",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when eicr and epc certs are expired`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcExpiredCerts()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.eicr.expired",
                "propertyDetails.complianceInformation.notificationMessage.epc.expired",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when gas cert is expired`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasCertExpiredAfterUpload()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.gasCert.expired",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when eicr cert is expired`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithEicrExpiredAfterUpload()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.eicr.expired",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when epc cert is expired`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcExpiredCert()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.epc.expired",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when all certs are missing`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithMissingCerts()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.gasCert.missing",
                "propertyDetails.complianceInformation.notificationMessage.eicr.missing",
                "propertyDetails.complianceInformation.notificationMessage.epc.missing",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when gas and eicr certs are missing`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEicrMissingCerts()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.gasCert.missing",
                "propertyDetails.complianceInformation.notificationMessage.eicr.missing",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when gas and epc certs are missing`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithGasAndEpcMissingCerts()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.gasCert.missing",
                "propertyDetails.complianceInformation.notificationMessage.epc.missing",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when eicr and epc certs are missing`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithEicrAndEpcMissingCerts()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.eicr.missing",
                "propertyDetails.complianceInformation.notificationMessage.epc.missing",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when gas cert is missing`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithOnlyGasMissingCert()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.gasCert.missing",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when eicr cert is missing`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEicrMissingCert()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.eicr.missing",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when epc cert is missing`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithOnlyEpcMissingCert()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.epc.missing",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }

    @Test
    fun `notificationMessages returns correctly populated list when epc rating is low`() {
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating()

        val expectedNotificationMessages =
            listOf(
                "propertyDetails.complianceInformation.notificationMessage.epc.lowRating",
            )

        val result = PropertyComplianceViewModel(propertyCompliance)

        assertEquals(result.notificationMessages, expectedNotificationMessages)
    }
}
