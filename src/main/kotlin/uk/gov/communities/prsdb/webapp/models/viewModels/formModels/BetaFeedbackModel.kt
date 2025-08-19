package uk.gov.communities.prsdb.webapp.models.viewModels.formModels
import uk.gov.communities.prsdb.webapp.constants.BETA_FEEDBACK_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class BetaFeedbackModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.betaFeedback.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.betaFeedback.error.tooLong",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = arrayOf("0", BETA_FEEDBACK_MAX_LENGTH.toString()),
            ),
        ],
    )
    var feedback: String = ""
}
