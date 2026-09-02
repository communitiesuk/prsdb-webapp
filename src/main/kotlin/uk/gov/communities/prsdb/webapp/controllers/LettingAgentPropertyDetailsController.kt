package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentPropertyDetailsController.Companion.LETTING_AGENT_PROPERTY_DETAILS_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.LettingAgentPropertyDetailsViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModelFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.util.UUID

@PrsdbController
@RequestMapping(LETTING_AGENT_PROPERTY_DETAILS_ROUTE)
class LettingAgentPropertyDetailsController(
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val propertyComplianceViewModelFactory: PropertyComplianceViewModelFactory,
    private val messageSource: MessageSource,
) {
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @GetMapping
    fun getLettingAgentPropertyDetails(
        @PathVariable token: UUID,
        model: Model,
    ): String {
        // TODO: PDJB-1659: Authorise that the letting agent stored in the session has access to this property.
        val lettingAgentAccess =
            lettingAgentAccessService.getInvitationByTokenOrNull(token)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No letting agent access found for token $token")

        val propertyOwnershipId = lettingAgentAccess.propertyOwnership.id
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)

        val propertyCompliance =
            propertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)
                ?: throw PrsdbWebException("Property ownership $propertyOwnershipId does not have a compliance record")

        val complianceAllValid = ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).isAllValid

        model.addAttribute(
            "propertyDetails",
            LettingAgentPropertyDetailsViewModel(propertyOwnership, complianceAllValid, messageSource),
        )
        model.addAttribute(
            "complianceDetails",
            propertyComplianceViewModelFactory.create(
                propertyCompliance = propertyCompliance,
                landlordView = false,
                propertyOwnershipId = propertyOwnershipId,
            ),
        )
        // TODO: PDJB-1567: Point the back link at the letting-agent confirmation page once it exists.
        model.addAttribute("backUrl", "/")

        return LETTING_AGENT_PROPERTY_DETAILS_VIEW
    }

    companion object {
        const val LETTING_AGENT_PROPERTY_DETAILS_VIEW = "propertyDetailsLettingAgentView"

        const val LETTING_AGENT_PROPERTY_DETAILS_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{token}"

        fun getLettingAgentPropertyDetailsPath(token: UUID): String =
            UriTemplate(LETTING_AGENT_PROPERTY_DETAILS_ROUTE).expand(token).toASCIIString()
    }
}
