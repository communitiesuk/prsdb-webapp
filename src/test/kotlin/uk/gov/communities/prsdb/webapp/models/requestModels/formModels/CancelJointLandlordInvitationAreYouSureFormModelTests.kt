package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CancelJointLandlordInvitationAreYouSureFormModelTests {
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
    fun `validation fails when wantsToProceed is null`() {
        val formModel = CancelJointLandlordInvitationAreYouSureFormModel(wantsToProceed = null)

        val violations = validator.validate(formModel)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("cancelJointLandlordInvitation.areYouSure.radios.error.missing", violation.messageTemplate)
        assertEquals("wantsToProceed", violation.propertyPath.toString())
    }

    @Test
    fun `validation passes when wantsToProceed is true`() {
        val formModel = CancelJointLandlordInvitationAreYouSureFormModel(wantsToProceed = true)

        val violations = validator.validate(formModel)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `validation passes when wantsToProceed is false`() {
        val formModel = CancelJointLandlordInvitationAreYouSureFormModel(wantsToProceed = false)

        val violations = validator.validate(formModel)

        assertTrue(violations.isEmpty())
    }
}
