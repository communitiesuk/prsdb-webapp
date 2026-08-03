package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.IdentityVerificationStatus
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class CheckLandlordIdentityVerifiedStepConfig(
    private val userToLandlordService: UserToLandlordService,
) : AbstractInternalStepConfig<IdentityVerificationStatus, JourneyState>() {
    override fun mode(state: JourneyState): IdentityVerificationStatus {
        val landlord = userToLandlordService.getCurrentLandlordForUser()
        check(landlord is IndividualLandlord)
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
