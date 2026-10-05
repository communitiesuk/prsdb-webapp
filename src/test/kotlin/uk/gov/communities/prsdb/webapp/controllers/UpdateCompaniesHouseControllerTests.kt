package uk.gov.communities.prsdb.webapp.controllers

import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.UpdateCompaniesHouseJourneyFactory
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@WebMvcTest(UpdateCompaniesHouseController::class)
class UpdateCompaniesHouseControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseOrganisationalLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateCompaniesHouseJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    override lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    override lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    override val updateStepRoute =
        UpdateCompaniesHouseController.UPDATE_COMPANIES_HOUSE_ROUTE +
            "/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}"

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
