package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentIncludesBillsController.Companion.LETTING_AGENT_UPDATE_RENT_INCLUDES_BILLS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills.UpdateRentIncludesBillsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.util.UUID

@PrsdbController
@RequestMapping(LETTING_AGENT_UPDATE_RENT_INCLUDES_BILLS_ROUTE)
class LettingAgentUpdateRentIncludesBillsController(
    private val journeyFactory: UpdateRentIncludesBillsJourneyFactory,
    lettingAgentAccessService: LettingAgentAccessService,
    propertyOwnershipService: PropertyOwnershipService,
) : AbstractLettingAgentUpdateController(lettingAgentAccessService, propertyOwnershipService) {
    override fun createJourneySteps(
        propertyOwnershipId: Long,
        returnUrl: String,
    ): Map<String, StepLifecycleOrchestrator> = journeyFactory.createJourneySteps(propertyOwnershipId, returnUrl)

    override fun initialiseJourneyState(
        token: UUID,
        propertyOwnershipId: Long,
    ): String = journeyFactory.initialiseJourneyState(token, propertyOwnershipId)

    companion object {
        const val LETTING_AGENT_UPDATE_RENT_INCLUDES_BILLS_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{token}/update-rent-includes-bills"

        fun getUpdateRentIncludesBillsRoute(token: UUID): String =
            UriTemplate(LETTING_AGENT_UPDATE_RENT_INCLUDES_BILLS_ROUTE).expand(token).toASCIIString()
    }
}
