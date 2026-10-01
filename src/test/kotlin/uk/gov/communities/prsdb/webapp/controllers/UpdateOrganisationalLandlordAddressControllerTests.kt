package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationAddress.UpdateOrganisationAddressJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@WebMvcTest(UpdateOrganisationalLandlordAddressController::class)
class UpdateOrganisationalLandlordAddressControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseOrganisationalLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateOrganisationAddressJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    override lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    override lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    override val updateStepRoute =
        UpdateOrganisationalLandlordAddressController.UPDATE_ORG_ADDRESS_ROUTE +
            "/${LookupAddressStep.ROUTE_SEGMENT}"

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(LookupAddressStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}