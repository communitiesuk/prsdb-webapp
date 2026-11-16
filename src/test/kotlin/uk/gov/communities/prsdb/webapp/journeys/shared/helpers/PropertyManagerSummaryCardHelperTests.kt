package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel

class PropertyManagerSummaryCardHelperTests {
    private val helper = PropertyManagerSummaryCardHelper()

    @Test
    fun `getSummaryCard is titled with the property manager's name and has an email address row`() {
        val card = helper.getSummaryCard(detailsFormModel())

        assertEquals(NAME, card.title)
        assertEquals("registerProperty.propertyManager.check.emailAddress", card.summaryList.single().fieldHeading)
        assertEquals(EMAIL, card.summaryList.single().fieldValue)
    }

    @Test
    fun `getSummaryCard has no actions by default`() {
        val card = helper.getSummaryCard(detailsFormModel())

        assertEquals(emptyList<SummaryCardActionViewModel>(), card.actions)
    }

    @Test
    fun `getSummaryCard uses the actions provided`() {
        val actions = listOf(SummaryCardActionViewModel(text = "forms.links.change", url = "/change"))

        val card = helper.getSummaryCard(detailsFormModel(), actions)

        assertEquals(actions, card.actions)
    }

    @Test
    fun `getSummaryCard throws when the name is missing`() {
        assertThrows<NotNullFormModelValueIsNullException> { helper.getSummaryCard(detailsFormModel(name = null)) }
    }

    @Test
    fun `getSummaryCard throws when the email address is missing`() {
        assertThrows<NotNullFormModelValueIsNullException> { helper.getSummaryCard(detailsFormModel(email = null)) }
    }

    private fun detailsFormModel(
        name: String? = NAME,
        email: String? = EMAIL,
    ) = PropertyManagerDetailsFormModel().apply {
        this.name = name
        this.emailAddress = email
    }

    companion object {
        private const val NAME = "Best Property Managers Ltd"
        private const val EMAIL = "manager@example.com"
    }
}
