package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationEmail

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteOrganisationEmailUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateOrganisationEmailJourneyState>() {
    override fun mode(state: UpdateOrganisationEmailJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOrganisationEmailJourneyState) {
        val newOrgEmail = state.orgEmailStep.formModel.notNullValue(EmailFormModel::emailAddress)
        landlordService.updateOrganisationLandlordEmail(newOrgEmail)
    }

    override fun resolveNextDestination(
        state: UpdateOrganisationEmailJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOrganisationEmailUpdateStep(
    stepConfig: CompleteOrganisationEmailUpdateStepConfig,
) : InternalStep<Complete, UpdateOrganisationEmailJourneyState>(stepConfig)
