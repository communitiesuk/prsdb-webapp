package uk.gov.communities.prsdb.webapp.validation.validators

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.models.dataModels.ConfirmedEmailDataModel

class MatchingPropertiesValidatorTest {
    @Test
    fun `ConfirmEmail is invalid if the emails do not match`() {
        val validator = MatchingPropertiesValidator()
        val confirmedEmail = ConfirmedEmailDataModel("test", "test2")
        Assertions.assertFalse(validator.isValid(confirmedEmail, null))
    }

    @Test
    fun `ConfirmEmail is valid if the emails do match`() {
        val validator = MatchingPropertiesValidator()
        val confirmedEmail = ConfirmedEmailDataModel("test", "test")
        Assertions.assertTrue(validator.isValid(confirmedEmail, null))
    }
}
