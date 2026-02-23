package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.email

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteEmailUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateEmailJourneyState>() {
    override fun mode(state: UpdateEmailJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateEmailJourneyState) {
        val email = state.emailStep.formModel.notNullValue(EmailFormModel::emailAddress)
        landlordService.updateLandlordForBaseUserId(
            SecurityContextHolder.getContext().authentication.name,
            LandlordUpdateModel(
                email = email,
                name = null,
                phoneNumber = null,
                address = null,
                dateOfBirth = null,
            ),
        ) {}
    }

    override fun resolveNextDestination(
        state: UpdateEmailJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteEmailUpdateStep(
    stepConfig: CompleteEmailUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateEmailJourneyState>(stepConfig)
