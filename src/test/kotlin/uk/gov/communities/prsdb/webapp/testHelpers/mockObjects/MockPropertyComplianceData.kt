package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.LocalDate

class MockPropertyComplianceData {
    companion object {
        fun createPropertyCompliance(
            propertyOwnership: PropertyOwnership = MockLandlordData.createPropertyOwnership(),
            gasSafetyCertUpload: FileUpload? = FileUpload(FileUploadStatus.QUARANTINED, "gas-safety", "pdf", "etag", "versionId"),
            gasSafetyCertIssueDate: LocalDate? = defaultGasAndEicrIssueDate,
            gasSafetyCertEngineerNum: String? = defaultGasEngineerNumber,
            gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null,
            gasSafetyCertExemptionOtherReason: String? = null,
            eicrFileUpload: FileUpload? = FileUpload(FileUploadStatus.QUARANTINED, "eicr", "pdf", "etag", "versionId"),
            eicrIssueDate: LocalDate? = defaultGasAndEicrIssueDate,
            eicrExemptionReason: EicrExemptionReason? = null,
            eicrExemptionOtherReason: String? = null,
            epcUrl: String? = "epc.url/0000-0000-0000-0000-0000",
            epcExpiryDate: LocalDate? = defaultEpcExpiryDate,
            tenancyStartedBeforeEpcExpiry: Boolean? = null,
            epcEnergyRating: String? = defaultGoodEpcEnergyRating,
            epcExemptionReason: EpcExemptionReason? = null,
            epcMeesExemptionReason: MeesExemptionReason? = null,
        ) = PropertyCompliance(
            propertyOwnership = propertyOwnership,
            gasSafetyCertUpload = gasSafetyCertUpload,
            gasSafetyCertIssueDate = gasSafetyCertIssueDate,
            gasSafetyCertEngineerNum = gasSafetyCertEngineerNum,
            gasSafetyCertExemptionReason = gasSafetyCertExemptionReason,
            gasSafetyCertExemptionOtherReason = gasSafetyCertExemptionOtherReason,
            eicrUpload = eicrFileUpload,
            eicrIssueDate = eicrIssueDate,
            eicrExemptionReason = eicrExemptionReason,
            eicrExemptionOtherReason = eicrExemptionOtherReason,
            epcUrl = epcUrl,
            epcExpiryDate = epcExpiryDate,
            tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry,
            epcEnergyRating = epcEnergyRating,
            epcExemptionReason = epcExemptionReason,
            epcMeesExemptionReason = epcMeesExemptionReason,
        )

        fun createFileUpload(uploadId: Long = 123L): FileUpload {
            val fileUpload =
                FileUpload(
                    FileUploadStatus.SCANNED,
                    "objectKey-$uploadId",
                    "pdf",
                    "etag-$uploadId",
                    "versionId-$uploadId",
                )
            ReflectionTestUtils.setField(fileUpload, "id", uploadId)

            return fileUpload
        }

        val defaultGasEngineerNumber = "1234567"
        val defaultGasAndEicrIssueDate = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate()
        val defaultEpcExpiryDate = DateTimeHelper().getCurrentDateInUK().plus(5, DAY).toJavaLocalDate()
        val defaultGoodEpcEnergyRating = "C"
    }
}
