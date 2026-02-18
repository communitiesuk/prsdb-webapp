package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IdentityState
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService

@JourneyFrameworkComponent
class IdentityVerifyingStepConfig(
    val identityService: OneLoginIdentityService,
) : AbstractRequestableStepConfig<IdentityVerifiedMode, Nothing, IdentityState>() {
    override val formModelClass = Nothing::class

    override fun getStepSpecificContent(state: IdentityState) = emptyMap<String, Any?>()

    override fun chooseTemplate(state: IdentityState) = ""

    override fun mode(state: IdentityState) =
        if (state.getIsIdentityVerified()) {
            IdentityVerifiedMode.VERIFIED
        } else {
            IdentityVerifiedMode.NOT_VERIFIED
        }

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) =
        StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun beforeChoosingNextDestination(state: IdentityState) {
        val oidcUser = SecurityContextHolder.getContext().authentication.principal as OidcUser
        state.verifiedIdentity = identityService.getVerifiedIdentityData(oidcUser)
    }
}

@JourneyFrameworkComponent
final class IdentityVerifyingStep(
    stepConfig: IdentityVerifyingStepConfig,
) : RequestableStep<IdentityVerifiedMode, Nothing, IdentityState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "verify-identity"
    }
}

enum class IdentityVerifiedMode {
    VERIFIED,
    NOT_VERIFIED,
}
