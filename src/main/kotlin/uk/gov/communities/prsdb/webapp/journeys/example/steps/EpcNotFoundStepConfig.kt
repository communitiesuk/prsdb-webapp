package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.CONTACT_EPC_ASSESSOR_URL
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@Scope("prototype")
@PrsdbWebComponent
class EpcNotFoundStepConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, EpcJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "contactAssessorUrl" to CONTACT_EPC_ASSESSOR_URL,
            "getNewEpcUrl" to GET_NEW_EPC_URL,
            "searchAgainUrl" to state.searchForEpc?.routeSegment,
            "certificateNumber" to state.searchForEpc?.formModel?.certificateNumber,
            "submitButtonText" to "forms.buttons.saveAndContinueToLandlordResponsibilities",
        )

    override fun chooseTemplate(state: EpcJourneyState): String = "forms/epcNotFoundForm"

    override fun mode(state: EpcJourneyState): Complete? = getFormModelFromState(state)?.let { Complete.COMPLETE }
}
