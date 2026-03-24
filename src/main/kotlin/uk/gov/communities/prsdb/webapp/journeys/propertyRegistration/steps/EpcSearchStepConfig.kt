package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchMode.CURRENT_EPC_FOUND
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchMode.NOT_FOUND
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchMode.SUPERSEDED_EPC_FOUND
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcSearchFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-662: Implement this step, this radios version is a placeholder
@JourneyFrameworkComponent
class EpcSearchStepConfig : AbstractRequestableStepConfig<EpcSearchMode, EpcSearchFormModel, EpcState>() {
    override val formModelClass = EpcSearchFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "fieldSetHeading" to "forms.epcSearch.fieldSetHeading",
            "fieldName" to "epcSearchMode",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = CURRENT_EPC_FOUND.name,
                        labelMsgKey = "forms.epcSearch.mode.currentEpcFound",
                    ),
                    RadiosButtonViewModel(
                        value = SUPERSEDED_EPC_FOUND.name,
                        labelMsgKey = "forms.epcSearch.mode.supersededEpcFound",
                    ),
                    RadiosButtonViewModel(
                        value = NOT_FOUND.name,
                        labelMsgKey = "forms.epcSearch.mode.notFound",
                    ),
                ),
        )

    override fun chooseTemplate(state: EpcState) = "forms/todoWithRadios"

    override fun mode(state: EpcState): EpcSearchMode? =
        getFormModelFromStateOrNull(state)?.epcSearchMode?.let { EpcSearchMode.valueOf(it) }
}

@JourneyFrameworkComponent
final class EpcSearchStep(
    stepConfig: EpcSearchStepConfig,
) : RequestableStep<EpcSearchMode, EpcSearchFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "search-for-epc"
    }
}

enum class EpcSearchMode {
    CURRENT_EPC_FOUND,
    SUPERSEDED_EPC_FOUND,
    NOT_FOUND,
}
