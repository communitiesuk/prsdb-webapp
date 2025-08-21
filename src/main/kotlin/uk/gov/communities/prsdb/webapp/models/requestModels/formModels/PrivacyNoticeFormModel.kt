package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.TrueConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class LandlordPrivacyNoticeFormModel : PrivacyNoticeFormModel() {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerAsALandlord.privacyNotice.checkbox.error.missing",
                validatorType = TrueConstraintValidator::class,
            ),
        ],
    )
    override var agreesToPrivacyNotice: Boolean = false
}

@IsValidPrioritised
class LocalAuthorityPrivacyNoticeFormModel : PrivacyNoticeFormModel() {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerLAUser.privacyNotice.checkbox.error.missing",
                validatorType = TrueConstraintValidator::class,
            ),
        ],
    )
    override var agreesToPrivacyNotice: Boolean = false
}

abstract class PrivacyNoticeFormModel : FormModel {
    abstract var agreesToPrivacyNotice: Boolean
}
