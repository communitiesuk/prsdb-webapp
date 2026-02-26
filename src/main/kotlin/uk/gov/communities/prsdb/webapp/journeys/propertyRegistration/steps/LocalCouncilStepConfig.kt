package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_LOCAL_COUNCIL_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectLocalCouncilFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService

@JourneyFrameworkComponent
class LocalCouncilStepConfig(
    private val localCouncilService: LocalCouncilService,
) : AbstractRequestableStepConfig<Complete, SelectLocalCouncilFormModel, JourneyState>() {
    override val formModelClass = SelectLocalCouncilFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> {
        val localCouncilsSelectOptions =
            localCouncilService.retrieveAllLocalCouncils().map {
                SelectViewModel(
                    value = it.id,
                    label = it.name,
                )
            }

        return mapOf(
            "fieldSetHeading" to "forms.selectLocalCouncil.fieldSetHeading",
            "fieldSetHint" to "forms.selectLocalCouncil.fieldSetHint",
            "selectLabel" to "forms.selectLocalCouncil.select.label",
            "findLocalCouncilUrl" to FIND_LOCAL_COUNCIL_URL,
            "selectOptions" to localCouncilsSelectOptions,
        )
    }

    override fun chooseTemplate(state: JourneyState): String = "forms/selectLocalCouncilForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class LocalCouncilStep(
    stepConfig: LocalCouncilStepConfig,
) : RequestableStep<Complete, SelectLocalCouncilFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "local-council"
    }
}
