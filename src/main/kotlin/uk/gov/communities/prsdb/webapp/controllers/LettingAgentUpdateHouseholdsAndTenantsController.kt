package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateHouseholdsAndTenantsController.Companion.LETTING_AGENT_UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.util.UUID

@PrsdbController
@RequestMapping(LETTING_AGENT_UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE)
class LettingAgentUpdateHouseholdsAndTenantsController(
    private val journeyFactory: UpdateHouseholdsAndTenantsJourneyFactory,
    lettingAgentAccessService: LettingAgentAccessService,
    propertyOwnershipService: PropertyOwnershipService,
) : AbstractLettingAgentUpdateController(lettingAgentAccessService, propertyOwnershipService) {
    override fun createJourneySteps(
        propertyOwnershipId: Long,
        returnUrl: String,
    ): Map<String, StepLifecycleOrchestrator> = journeyFactory.createJourneySteps(propertyOwnershipId, returnUrl)

    override fun initialiseJourneyState(propertyOwnershipId: Long): String =
        journeyFactory.initialiseJourneyStateForLettingAgent(propertyOwnershipId)

    companion object {
        const val LETTING_AGENT_UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{token}/update-households-and-tenants"

        fun getUpdateHouseholdsAndTenantsRoute(token: UUID): String =
            UriTemplate(LETTING_AGENT_UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE).expand(token).toASCIIString()
    }
}
