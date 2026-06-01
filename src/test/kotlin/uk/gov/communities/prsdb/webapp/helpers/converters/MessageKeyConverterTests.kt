package uk.gov.communities.prsdb.webapp.helpers.converters

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.config.YamlMessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import java.util.Locale

class MessageKeyConverterTests {
    private val messageSource = YamlMessageSource("classpath:messages")

    private fun assertMessageKeyResolves(messageKey: String) {
        val resolvedMessage = messageSource.getMessage(messageKey, null, messageKey, Locale.getDefault())
        assertNotEquals(messageKey, resolvedMessage) {
            "Message key '$messageKey' does not resolve — it would display as the raw key on the page"
        }
    }

    private fun assertMessageKeyDoesNotResolve(messageKey: String) {
        val resolvedMessage = messageSource.getMessage(messageKey, null, messageKey, Locale.getDefault())
        assertEquals(messageKey, resolvedMessage) {
            "Message key '$messageKey' resolves. This test will likely need flipping to that it now resolves"
        }
    }

    @Test
    fun `convert returns resolvable message keys for booleans`() {
        assertMessageKeyResolves(MessageKeyConverter.convert(true))
        assertMessageKeyResolves(MessageKeyConverter.convert(false))
    }

    @ParameterizedTest
    @EnumSource(PropertyType::class)
    fun `convert returns a resolvable message key for every PropertyType`(value: PropertyType) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(OwnershipType::class)
    fun `convert returns a resolvable message key for every OwnershipType`(value: OwnershipType) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(LicensingType::class)
    fun `convert returns a resolvable message key for every LicensingType`(value: LicensingType) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(FurnishedStatus::class)
    fun `convert returns a resolvable message key for every FurnishedStatus`(value: FurnishedStatus) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(RentFrequency::class, mode = EnumSource.Mode.EXCLUDE, names = ["OTHER"])
    fun `convert returns a resolvable message key for every RentFrequency`(value: RentFrequency) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(BillsIncluded::class, mode = EnumSource.Mode.EXCLUDE, names = ["SOMETHING_ELSE"])
    fun `convert returns a resolvable message key for every BillsIncluded`(value: BillsIncluded) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(EpcExemptionReason::class)
    fun `convert returns a resolvable message key for every EpcExemptionReason`(value: EpcExemptionReason) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(MeesExemptionReason::class)
    fun `convert returns a resolvable message key for every MeesExemptionReason`(value: MeesExemptionReason) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    // If this test is failing, this will likely be as work was taken to remove the compliance-actions-page-may26-redesign feature flag.
    // With it, the old & redesign sub keys should be removed,
    // and so all keys in the enum can now be resolved.
    // Remove this test and remove "PROVIDE_LATER" and "EXPIRED" from the exclude of the below test
    @ParameterizedTest
    @EnumSource(ComplianceCertStatus::class, names = ["NOT_REQUIRED", "ADDED"], mode = EnumSource.Mode.EXCLUDE)
    fun `convert does not return a resolvable message key ComplianceCertStatus changed under the feature flag`(
        value: ComplianceCertStatus,
    ) {
        assertMessageKeyDoesNotResolve(MessageKeyConverter.convert(value))
    }

    @Test
    fun `convert throws IllegalStateException for NOT_REQUIRED ComplianceCertStatus`() {
        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            MessageKeyConverter.convert(ComplianceCertStatus.NOT_REQUIRED)
        }
    }

    @Test
    fun `convert throws IllegalStateException for ADDED ComplianceCertStatus`() {
        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            MessageKeyConverter.convert(ComplianceCertStatus.ADDED)
        }
    }

    @ParameterizedTest
    @EnumSource(FileUploadStatus::class)
    fun `convert returns a resolvable message key for every FileUploadStatus`(value: FileUploadStatus) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }
}
