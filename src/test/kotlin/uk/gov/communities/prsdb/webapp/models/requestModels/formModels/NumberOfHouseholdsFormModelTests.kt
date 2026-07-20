package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME

class NumberOfHouseholdsFormModelTests {
    @Test
    fun `Number of households is valid when provideThisLater action is submitted`() {
        val form = NumberOfHouseholdsFormModel().apply {
            action = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
            numberOfHouseholds = ""
        }

        assertTrue(form.numberOfHouseholdsIsValidForAction())
    }
}
