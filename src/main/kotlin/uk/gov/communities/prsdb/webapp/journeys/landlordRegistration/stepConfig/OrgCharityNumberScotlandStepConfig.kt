package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberScotlandFormModel

@JourneyFrameworkComponent
class OrgCharityNumberScotlandStepConfig :
    AbstractRequestableStepConfig<Complete, OrgCharityNumberScotlandFormModel, JourneyState>() {
    override val formModelClass = OrgCharityNumberScotlandFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "charityUrl" to CHARITY_REGISTER_URL,
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgCharityNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    companion object {
        private const val CHARITY_REGISTER_URL = "https://www.oscr.org.uk/"
    }
}

@JourneyFrameworkComponent
final class OrgCharityNumberScotlandStep(
    stepConfig: OrgCharityNumberScotlandStepConfig,
) : RequestableStep<Complete, OrgCharityNumberScotlandFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-charity-number-scotland"
    }
}
