package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class FreeholderProvideDetailsFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.freeholders.provideDetails.radios.error.missing.joint",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isJointFreeholderAnswerProvided",
            ),
            ConstraintDescriptor(
                messageKey = "registerProperty.freeholders.provideDetails.radios.error.missing.nonJoint",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isNonJointFreeholderAnswerProvided",
            ),
        ],
    )
    var provideDetails: Boolean? = null,
) : FormModel {
    var isJointFreeholder: Boolean = false

    fun isJointFreeholderAnswerProvided(): Boolean = if (isJointFreeholder) provideDetails != null else true

    fun isNonJointFreeholderAnswerProvided(): Boolean = if (!isJointFreeholder) provideDetails != null else true
}
