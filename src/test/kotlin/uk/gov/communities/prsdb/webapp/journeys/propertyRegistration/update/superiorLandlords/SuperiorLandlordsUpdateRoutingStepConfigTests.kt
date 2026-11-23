package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SuperiorLandlordsUpdateRoutingStepConfigTests {
    @Mock
    private lateinit var mockState: UpdateSuperiorLandlordsJourneyState

    @Mock
    private lateinit var mockSuperiorLandlordsTask: SuperiorLandlordsTask

    @Mock
    private lateinit var mockAnyoneInOwnershipChainStep: AnyoneInOwnershipChainStep

    private val stepConfig = SuperiorLandlordsUpdateRoutingStepConfig()

    @BeforeEach
    fun setUp() {
        whenever(mockState.superiorLandlordsTask).thenReturn(mockSuperiorLandlordsTask)
        whenever(mockSuperiorLandlordsTask.anyoneInOwnershipChainStep).thenReturn(mockAnyoneInOwnershipChainStep)
    }

    @Test
    fun `mode is HAS_SUPERIOR_LANDLORDS when the ownership chain answer is yes and superior landlords are known`() {
        whenever(mockAnyoneInOwnershipChainStep.formModelOrNull).thenReturn(AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = true))
        whenever(mockSuperiorLandlordsTask.superiorLandlordsMap).thenReturn(mapOf(1 to SuperiorLandlordDataModel(detailsKnown = false)))

        assertEquals(SuperiorLandlordsUpdateRouteMode.HAS_SUPERIOR_LANDLORDS, stepConfig.mode(mockState))
    }

    @Test
    fun `mode is NO_SUPERIOR_LANDLORDS when the ownership chain question has not been answered`() {
        whenever(mockAnyoneInOwnershipChainStep.formModelOrNull).thenReturn(null)

        assertEquals(SuperiorLandlordsUpdateRouteMode.NO_SUPERIOR_LANDLORDS_ADDED, stepConfig.mode(mockState))
    }

    @Test
    fun `mode is NO_SUPERIOR_LANDLORDS when the answer is yes but there are no superior landlords`() {
        whenever(mockAnyoneInOwnershipChainStep.formModelOrNull).thenReturn(AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = true))
        whenever(mockSuperiorLandlordsTask.superiorLandlordsMap).thenReturn(emptyMap())

        assertEquals(SuperiorLandlordsUpdateRouteMode.NO_SUPERIOR_LANDLORDS_ADDED, stepConfig.mode(mockState))
    }
}
