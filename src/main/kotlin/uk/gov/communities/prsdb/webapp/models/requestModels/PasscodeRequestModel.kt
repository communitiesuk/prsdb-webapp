package uk.gov.communities.prsdb.webapp.models.requestModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO: PRSD-1094: Use built-in validation annotations instead of custom ones
@IsValidPrioritised
data class PasscodeRequestModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "passcodeEntry.error.missingPasscode",
                validatorType = NotBlankConstraintValidator::class,
            ),
        ],
    )
    var passcode: String = "",
)
