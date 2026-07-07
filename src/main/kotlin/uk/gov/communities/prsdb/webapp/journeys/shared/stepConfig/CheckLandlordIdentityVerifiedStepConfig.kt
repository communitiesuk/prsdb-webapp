package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.REMOVE_ID_VERIFICATION
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.IdentityVerificationStatus
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CheckLandlordIdentityVerifiedStepConfig(
    private val landlordService: LandlordService,
    private val featureFlagManager: FeatureFlagManager,
) : AbstractInternalStepConfig<IdentityVerificationStatus, JourneyState>() {
    override fun mode(state: JourneyState): IdentityVerificationStatus {
        if (featureFlagManager.checkFeature(REMOVE_ID_VERIFICATION)) {
            return IdentityVerificationStatus.NOT_VERIFIED
        }
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(baseUserId)
                ?: throw PrsdbWebException("Landlord not found for baseUserId: $baseUserId")
        return if (landlord.isVerified) {
            IdentityVerificationStatus.VERIFIED
        } else {
            IdentityVerificationStatus.NOT_VERIFIED
        }
    }
}

@JourneyFrameworkComponent
class CheckLandlordIdentityVerifiedStep(
    stepConfig: CheckLandlordIdentityVerifiedStepConfig,
) : InternalStep<IdentityVerificationStatus, JourneyState>(stepConfig)
