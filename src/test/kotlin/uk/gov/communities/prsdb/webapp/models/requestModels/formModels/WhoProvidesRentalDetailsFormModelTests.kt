package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails

class WhoProvidesRentalDetailsFormModelTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `is invalid when whoProvides is null`() {
        val formModel = WhoProvidesRentalDetailsFormModel()

        val violations = validator.validate(formModel)

        assertEquals(1, violations.size)
        assertEquals("registerProperty.whoProvidesRentalDetails.radios.error.missing", violations.first().message)
    }

    @Test
    fun `is valid when whoProvides is LANDLORD`() {
        val formModel = WhoProvidesRentalDetailsFormModel().apply { whoProvides = WhoProvidesRentalDetails.LANDLORD }

        val violations = validator.validate(formModel)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `is valid when whoProvides is LETTING_AGENT`() {
        val formModel = WhoProvidesRentalDetailsFormModel().apply { whoProvides = WhoProvidesRentalDetails.LETTING_AGENT }

        val violations = validator.validate(formModel)

        assertTrue(violations.isEmpty())
    }
}
