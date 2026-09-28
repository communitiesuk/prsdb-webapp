package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PaymentOutcome
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-993: Delete this form model when PaymentRoutingStep becomes an internal step - the outcome will then be
//  derived from the real payment status rather than a user-submitted radio.
@IsValidPrioritised
class PaymentOutcomeFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerProperty.paymentOutcome.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var paymentOutcome: PaymentOutcome? = null,
) : FormModel
