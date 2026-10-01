package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationCharity.UpdateOrganisationCharityJourneyFactory
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@WebMvcTest(UpdateOrganisationLandlordCharityController::class)
class UpdateOrganisationLandlordCharityControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseOrganisationalLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateOrganisationCharityJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    override lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    override lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    override val updateStepRoute =
        UpdateOrganisationLandlordCharityController.UPDATE_ORG_CHARITY_ROUTE +
            "/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}"

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(OrgIsRegisteredCharityStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}