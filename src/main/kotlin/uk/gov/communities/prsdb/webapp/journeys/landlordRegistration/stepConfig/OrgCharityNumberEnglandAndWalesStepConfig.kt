package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_AND_WALES_CHARITY_REGISTER_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberEnglandAndWalesFormModel

@JourneyFrameworkComponent
class OrgCharityNumberEnglandAndWalesStepConfig :
    AbstractRequestableStepConfig<Complete, OrgCharityNumberEnglandAndWalesFormModel, JourneyState>() {
    override val formModelClass = OrgCharityNumberEnglandAndWalesFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "charityUrl" to ENGLAND_AND_WALES_CHARITY_REGISTER_URL,
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgCharityNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgCharityNumberEnglandAndWalesStep(
    stepConfig: OrgCharityNumberEnglandAndWalesStepConfig,
) : RequestableStep<Complete, OrgCharityNumberEnglandAndWalesFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-charity-number"
    }
}
