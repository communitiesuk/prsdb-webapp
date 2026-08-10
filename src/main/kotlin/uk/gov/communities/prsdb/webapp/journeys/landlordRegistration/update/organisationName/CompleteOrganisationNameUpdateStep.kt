package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationName

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgNameFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteOrganisationNameUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateOrganisationNameJourneyState>() {
    override fun mode(state: UpdateOrganisationNameJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOrganisationNameJourneyState) {
        val newOrgName = state.orgNameStep.formModel.notNullValue(OrgNameFormModel::orgName)
        landlordService.updateOrganisationLandlordName(newOrgName)
    }

    override fun resolveNextDestination(
        state: UpdateOrganisationNameJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOrganisationNameUpdateStep(
    stepConfig: CompleteOrganisationNameUpdateStepConfig,
) : InternalStep<Complete, UpdateOrganisationNameJourneyState>(stepConfig)
