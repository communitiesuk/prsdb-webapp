package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckPropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemovePropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel

class PropertyManagerStateTests {
    @Test
    fun `getPropertyManagerOrNull returns the property manager when the landlord is using one and has given their details`() {
        // Arrange
        val state = buildState(outcome = YesOrNo.YES, detailsFormModel = detailsFormModel())

        // Act
        val result = state.getPropertyManagerOrNull()

        // Assert
        assertEquals(PropertyManagerDataModel("Best Property Managers Ltd", "manager@example.com"), result)
    }

    @Test
    fun `getPropertyManagerOrNull returns null when the landlord is not using a property manager, even if details were given earlier`() {
        // Arrange
        val state = buildState(outcome = YesOrNo.NO, detailsFormModel = detailsFormModel())

        // Act & Assert
        assertNull(state.getPropertyManagerOrNull())
    }

    @Test
    fun `getPropertyManagerOrNull returns null when the landlord is using a property manager but has not given their details`() {
        // Arrange
        val state = buildState(outcome = YesOrNo.YES, detailsFormModel = null)

        // Act & Assert
        assertNull(state.getPropertyManagerOrNull())
    }

    private fun detailsFormModel() =
        PropertyManagerDetailsFormModel().apply {
            name = "Best Property Managers Ltd"
            emailAddress = "manager@example.com"
        }

    private fun buildState(
        outcome: YesOrNo?,
        detailsFormModel: PropertyManagerDetailsFormModel?,
    ): PropertyManagerState =
        object : AbstractJourneyState(journeyStateService = mock()), PropertyManagerState {
            override val propertyManagerStep =
                mock<PropertyManagerStep>().apply { whenever(this.outcome).thenReturn(outcome) }
            override val propertyManagerDetailsStep =
                mock<PropertyManagerDetailsStep>().apply { whenever(this.formModelIfReachableOrNull).thenReturn(detailsFormModel) }
            override val checkPropertyManagerStep = mock<CheckPropertyManagerStep>()
            override val removePropertyManagerStep = mock<RemovePropertyManagerStep>()
        }
}
