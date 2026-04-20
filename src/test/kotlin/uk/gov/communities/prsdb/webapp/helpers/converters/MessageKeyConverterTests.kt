package uk.gov.communities.prsdb.webapp.helpers.converters

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.config.YamlMessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
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
    @EnumSource(GasSafetyExemptionReason::class)
    fun `convert returns a resolvable message key for every GasSafetyExemptionReason`(value: GasSafetyExemptionReason) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(EicrExemptionReason::class)
    fun `convert returns a resolvable message key for every EicrExemptionReason`(value: EicrExemptionReason) {
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

    @ParameterizedTest
    @EnumSource(ComplianceCertStatus::class)
    fun `convert returns a resolvable message key for every ComplianceCertStatus`(value: ComplianceCertStatus) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }

    @ParameterizedTest
    @EnumSource(FileUploadStatus::class)
    fun `convert returns a resolvable message key for every FileUploadStatus`(value: FileUploadStatus) {
        assertMessageKeyResolves(MessageKeyConverter.convert(value))
    }
}
