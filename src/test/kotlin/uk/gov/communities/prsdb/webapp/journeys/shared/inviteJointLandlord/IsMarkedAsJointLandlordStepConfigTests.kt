package uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.InviteJointLandlordState

@ExtendWith(MockitoExtension::class)
class IsMarkedAsJointLandlordStepConfigTests {
    @Mock
    private lateinit var mockState: InviteJointLandlordState

    private val stepConfig = IsMarkedAsJointLandlordStepConfig()

    @Test
    fun `mode returns YES when property is marked as joint landlord`() {
        whenever(mockState.propertyMarkedAsJointLandlord).thenReturn(true)

        val result = stepConfig.mode(mockState)

        assertEquals(YesOrNo.YES, result)
    }

    @Test
    fun `mode returns NO when property is not marked as joint landlord`() {
        whenever(mockState.propertyMarkedAsJointLandlord).thenReturn(false)

        val result = stepConfig.mode(mockState)

        assertEquals(YesOrNo.NO, result)
    }
}
