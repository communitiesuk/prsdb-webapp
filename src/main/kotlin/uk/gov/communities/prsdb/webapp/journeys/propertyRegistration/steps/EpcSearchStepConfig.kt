package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel

// TODO PDJB-662: Update content including "search again" url. Add StepConfig tests
@JourneyFrameworkComponent
class EpcSearchStepConfig : AbstractRequestableStepConfig<EpcSearchMode, EpcLookupFormModel, JourneyState>() {
    override val formModelClass = EpcLookupFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "forms.epcLookup.fieldSetHeading",
            "findEpcUrl" to FIND_EPC_URL,
            "getNewEpcUrl" to "#",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/epcLookupForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { EpcSearchMode.FOUND }
}

@JourneyFrameworkComponent
final class EpcSearchStep(
    stepConfig: EpcSearchStepConfig,
) : RequestableStep<EpcSearchMode, EpcLookupFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "search-for-epc"
    }
}

enum class EpcSearchMode {
    FOUND,
    SUPERSEDED,
    NOT_FOUND,
}
