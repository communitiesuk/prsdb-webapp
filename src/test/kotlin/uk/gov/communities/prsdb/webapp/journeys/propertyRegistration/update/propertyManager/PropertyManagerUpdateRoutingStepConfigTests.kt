package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyManagerTask
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerFormModel
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class PropertyManagerUpdateRoutingStepConfigTests {
    @Mock
    private lateinit var mockState: UpdatePropertyManagerJourneyState

    @Mock
    private lateinit var mockPropertyManagerTask: PropertyManagerTask

    @Mock
    private lateinit var mockPropertyManagerStep: PropertyManagerStep

    @Mock
    private lateinit var mockPropertyManagerDetailsStep: PropertyManagerDetailsStep

    private val stepConfig = PropertyManagerUpdateRoutingStepConfig()

    @BeforeEach
    fun setUp() {
        whenever(mockState.propertyManagerTask).thenReturn(mockPropertyManagerTask)
        whenever(mockPropertyManagerTask.propertyManagerStep).thenReturn(mockPropertyManagerStep)
        whenever(mockPropertyManagerTask.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
    }

    @Test
    fun `mode is HAS_PROPERTY_MANAGER when a property manager is being used and their details are known`() {
        whenever(mockPropertyManagerStep.formModelOrNull).thenReturn(PropertyManagerFormModel().apply { usingPropertyManager = true })
        whenever(mockPropertyManagerDetailsStep.formModelOrNull).thenReturn(PropertyManagerDetailsFormModel())

        assertEquals(PropertyManagerUpdateRouteMode.HAS_PROPERTY_MANAGER, stepConfig.mode(mockState))
    }

    @Test
    fun `mode is NO_PROPERTY_MANAGER when the property manager question has not been answered`() {
        whenever(mockPropertyManagerStep.formModelOrNull).thenReturn(null)
        whenever(mockPropertyManagerDetailsStep.formModelOrNull).thenReturn(null)

        assertEquals(PropertyManagerUpdateRouteMode.NO_PROPERTY_MANAGER, stepConfig.mode(mockState))
    }

    @Test
    fun `mode is NO_PROPERTY_MANAGER when no property manager is being used even if details remain`() {
        whenever(mockPropertyManagerStep.formModelOrNull).thenReturn(PropertyManagerFormModel().apply { usingPropertyManager = false })
        whenever(mockPropertyManagerDetailsStep.formModelOrNull).thenReturn(PropertyManagerDetailsFormModel())

        assertEquals(PropertyManagerUpdateRouteMode.NO_PROPERTY_MANAGER, stepConfig.mode(mockState))
    }
}
