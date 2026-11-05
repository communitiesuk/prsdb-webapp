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
    fun `Joint freeholders use joint intro content`() {
        whenever(mockState.isJointFreeholder).thenReturn(true)

        val content = FreeholdersInfoStepConfig().getStepSpecificContent(mockState)

        assertEquals("registerProperty.freeholders.intro.heading.joint", content["heading"])
        assertEquals(false, content["showHelpDetails"])
    }

    @Test
    fun `Non-joint freeholders use non-joint intro content`() {
        whenever(mockState.isJointFreeholder).thenReturn(false)

        val content = FreeholdersInfoStepConfig().getStepSpecificContent(mockState)

        assertEquals("registerProperty.freeholders.intro.heading.nonJoint", content["heading"])
        assertEquals(true, content["showHelpDetails"])
    }
}
