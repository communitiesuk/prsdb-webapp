package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails

class WhoProvidesRentalDetailsFormModelTests {
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.validator
    }

    @AfterEach
    fun tearDown() {
        validatorFactory.close()
    }

    @Test
    fun `validation fails when whoProvides is null`() {
        val formModel = WhoProvidesRentalDetailsFormModel(whoProvides = null)

        val violations = validator.validate(formModel)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("registerProperty.whoProvidesRentalDetails.radios.error.missing", violation.messageTemplate)
        assertEquals("whoProvides", violation.propertyPath.toString())
    }

    @Test
    fun `validation passes when whoProvides is landlord`() {
        val formModel = WhoProvidesRentalDetailsFormModel(whoProvides = WhoProvidesRentalDetails.LANDLORD)

        val violations = validator.validate(formModel)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation passes when whoProvides is letting agent`() {
        val formModel = WhoProvidesRentalDetailsFormModel(whoProvides = WhoProvidesRentalDetails.LETTING_AGENT)

        val violations = validator.validate(formModel)

        assertTrue(violations.isEmpty())
    }
}
