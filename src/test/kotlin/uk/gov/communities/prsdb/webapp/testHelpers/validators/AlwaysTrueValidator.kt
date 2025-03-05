package uk.gov.communities.prsdb.webapp.testHelpers.validators

import org.springframework.validation.Errors
import org.springframework.validation.Validator

class AlwaysTrueValidator : Validator {
    override fun supports(clazz: Class<*>): Boolean = true

    override fun validate(
        target: Any,
        errors: Errors,
    ) {}
}
