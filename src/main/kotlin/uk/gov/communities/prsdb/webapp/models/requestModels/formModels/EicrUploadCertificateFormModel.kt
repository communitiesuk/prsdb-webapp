package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class EicrUploadCertificateFormModel : UploadCertificateFormModel() {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.uploadCertificate.eicr.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNameNotBlank",
            ),
            ConstraintDescriptor(
                messageKey = "forms.uploadCertificate.error.wrongType",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isFileTypeValid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.uploadCertificate.error.tooBig",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isContentLengthValid",
            ),
            ConstraintDescriptor(
                messageKey = "forms.uploadCertificate.error.unsuccessfulUpload",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isUploadSuccessfulOrInvalid",
            ),
        ],
    )
    override val certificate = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance) =
            record.eicrS3Key?.let {
                EicrUploadCertificateFormModel().apply {
                    this.name = it
                    // The following are not stored in the database, and are only required for validation
                    this.isUserSubmittedMetadataOnly = false
                    this.contentType = validMimeTypes.first()
                }
            }
    }
}
