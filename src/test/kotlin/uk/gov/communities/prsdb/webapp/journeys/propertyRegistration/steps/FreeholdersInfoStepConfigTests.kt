package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState

@ExtendWith(MockitoExtension::class)
class FreeholdersInfoStepConfigTests {
    @Mock
    lateinit var mockState: FreeholdersState

    @Test
    fun `Joint freeholders use the joint freeholders template`() {
        whenever(mockState.isJointFreeholder).thenReturn(true)

        val template = FreeholdersInfoStepConfig().chooseTemplate(mockState)

        assertEquals("forms/freeholdersInfoFormJoint", template)
    }

    @Test
    fun `Non-joint freeholders use the freeholders template`() {
        whenever(mockState.isJointFreeholder).thenReturn(false)

        val template = FreeholdersInfoStepConfig().chooseTemplate(mockState)

        assertEquals("forms/freeholdersInfoForm", template)
    }
}
