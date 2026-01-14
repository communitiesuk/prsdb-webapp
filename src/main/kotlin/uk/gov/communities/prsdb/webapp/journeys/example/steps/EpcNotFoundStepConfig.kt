package uk.gov.communities.prsdb.webapp.journeys.example.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTACT_EPC_ASSESSOR_URL
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class EpcNotFoundStepConfig : AbstractGenericRequestableStepConfig<Complete, NoInputFormModel, EpcJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "contactAssessorUrl" to CONTACT_EPC_ASSESSOR_URL,
            "getNewEpcUrl" to GET_NEW_EPC_URL,
            "searchAgainUrl" to state.searchForEpc?.routeSegment,
            "certificateNumber" to state.searchForEpc?.formModelOrNull?.certificateNumber,
            "submitButtonText" to "forms.buttons.saveAndContinueToLandlordResponsibilities",
        )

    override fun chooseTemplate(state: EpcJourneyState): String = "forms/epcNotFoundForm"

    override fun mode(state: EpcJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class EpcNotFoundStep(
    stepConfig: EpcNotFoundStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcJourneyState>(stepConfig)
