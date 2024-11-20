package uk.gov.communities.prsdb.webapp.validation

import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator

class NotNullConstraintValidator : PropertyConstraintValidator {
    override fun isValid(value: Any?): Boolean = NotNullValidator().isValid(value, null)
}
