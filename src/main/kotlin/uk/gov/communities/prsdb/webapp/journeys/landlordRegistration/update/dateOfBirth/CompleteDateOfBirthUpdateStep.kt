package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.dateOfBirth

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteDateOfBirthUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateDateOfBirthJourneyState>() {
    override fun mode(state: UpdateDateOfBirthJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateDateOfBirthJourneyState) {
        val dateOfBirth = state.dateOfBirthStep.formModel.toLocalDate()
        landlordService.updateLandlordDateOfBirth(
            SecurityContextHolder.getContext().authentication.name,
            dateOfBirth,
        )
    }

    override fun resolveNextDestination(
        state: UpdateDateOfBirthJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteDateOfBirthUpdateStep(
    stepConfig: CompleteDateOfBirthUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateDateOfBirthJourneyState>(stepConfig)
