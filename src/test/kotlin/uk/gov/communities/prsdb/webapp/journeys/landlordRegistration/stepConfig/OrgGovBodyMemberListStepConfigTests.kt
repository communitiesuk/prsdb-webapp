package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyState
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class OrgGovBodyMemberListStepConfigTests {
    @Mock
    lateinit var mockState: OrgGovBodyState

    @Mock
    lateinit var urlParameterService: CollectionKeyParameterService

    @Test
    fun `afterStepIsReached resets editingGovBodyMemberId to null`() {
        val stepConfig = OrgGovBodyMemberListStepConfig(urlParameterService)
        stepConfig.urlPath = OrgGovBodyMemberListStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        stepConfig.afterStepIsReached(mockState)

        verify(mockState).editingGovBodyMemberId = null
    }
}
