package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode

@JourneyFrameworkComponent
class OrgGovBodyDetailsStepConfig : AbstractRequestableStepConfig<OrgGovBodyDetailsMode, OrgGovBodyDetailsFormModel, JourneyState>() {
    override val formModelClass = OrgGovBodyDetailsFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "forms.orgGovBodyDetails.title",
            "landlordTypeUrl" to JourneyStateService.urlWithJourneyState(LandlordTypeStep.ROUTE_SEGMENT, state.journeyId),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgGovBodyDetailsForm"

    override fun mode(state: JourneyState): OrgGovBodyDetailsMode? =
        getFormModelFromStateOrNull(state)?.orgGovBodyDetailsMode?.let { OrgGovBodyDetailsMode.valueOf(it) }
}

@JourneyFrameworkComponent
final class OrgGovBodyDetailsStep(
    stepConfig: OrgGovBodyDetailsStepConfig,
) : RequestableStep<OrgGovBodyDetailsMode, OrgGovBodyDetailsFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-details"
    }
}
