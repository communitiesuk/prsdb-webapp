package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode.EPC_COMPLIANT
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode.EPC_INCORRECT
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode.EPC_LOW_ENERGY_RATING
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode.EPC_OLDER_THAN_10_YEARS
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TemporaryCheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-661: Implement this step
@JourneyFrameworkComponent("propertyRegistrationCheckMatchedEpcStepConfig")
class CheckMatchedEpcStepConfig : AbstractRequestableStepConfig<CheckMatchedEpcMode, TemporaryCheckMatchedEpcFormModel, EpcState>() {
    // TODO PDJB-661: Update form model back to CheckMatchedEpcFormModel once implemented,
    // TemporaryCheckMatchedEpcFormModel is just a placeholder to allow progress on other steps
    override val formModelClass = TemporaryCheckMatchedEpcFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "fieldSetHeading" to "forms.checkMatchedEpc.isThisTheCorrectEpc.heading",
            "fieldName" to "checkMatchedEpcMode",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = EPC_INCORRECT.name,
                        labelMsgKey = "forms.checkMatchedEpc.mode.epcIncorrect",
                    ),
                    RadiosButtonViewModel(
                        value = EPC_COMPLIANT.name,
                        labelMsgKey = "forms.checkMatchedEpc.mode.epcCompliant",
                    ),
                    RadiosButtonViewModel(
                        value = EPC_OLDER_THAN_10_YEARS.name,
                        labelMsgKey = "forms.checkMatchedEpc.mode.epcOlderThan10Years",
                    ),
                    RadiosButtonViewModel(
                        value = EPC_LOW_ENERGY_RATING.name,
                        labelMsgKey = "forms.checkMatchedEpc.mode.epcLowEnergyRating",
                    ),
                ),
        )

    override fun chooseTemplate(state: EpcState) = "forms/todoWithRadios"

    override fun mode(state: EpcState): CheckMatchedEpcMode? =
        getFormModelFromStateOrNull(state)?.checkMatchedEpcMode?.let { CheckMatchedEpcMode.valueOf(it) }
}

@JourneyFrameworkComponent("propertyRegistrationCheckMatchedEpcStep")
final class CheckMatchedEpcStep(
    stepConfig: CheckMatchedEpcStepConfig,
) : RequestableStep<CheckMatchedEpcMode, TemporaryCheckMatchedEpcFormModel, EpcState>(stepConfig) {
    companion object {
        const val SEARCHED_ROUTE_SEGMENT = "check-searched-epc"
        const val MATCHED_ROUTE_SEGMENT = "check-matched-epc"
    }
}

enum class CheckMatchedEpcMode {
    EPC_INCORRECT,
    EPC_COMPLIANT,
    EPC_OLDER_THAN_10_YEARS,
    EPC_LOW_ENERGY_RATING,
}
