package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationMainContact

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgMainContactFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteOrganisationMainContactUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateOrganisationMainContactJourneyState>() {
    override fun mode(state: UpdateOrganisationMainContactJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOrganisationMainContactJourneyState) {
        landlordService.updateOrganisationLandlordMainContact(
            name = state.orgMainContactStep.formModel.notNullValue(OrgMainContactFormModel::name),
            email = state.orgMainContactStep.formModel.notNullValue(OrgMainContactFormModel::emailAddress),
            phone = state.orgMainContactStep.formModel.notNullValue(OrgMainContactFormModel::phoneNumber),
        )
    }

    override fun resolveNextDestination(
        state: UpdateOrganisationMainContactJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOrganisationMainContactUpdateStep(
    stepConfig: CompleteOrganisationMainContactUpdateStepConfig,
) : InternalStep<Complete, UpdateOrganisationMainContactJourneyState>(stepConfig)
