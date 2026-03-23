package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel.Companion.yesOrNoRadios

@JourneyFrameworkComponent
class HasMeesExemptionStepConfig : AbstractRequestableStepConfig<HasMeesExemptionMode, MeesExemptionCheckFormModel, JourneyState>() {
    override val formModelClass = MeesExemptionCheckFormModel::class

    // TODO PDJB-667: Provide actual address and MEES exemption guide URL from state
    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "forms.meesExemptionCheck.heading",
            "singleLineAddress" to "",
            "meesExemptionGuideUrl" to "#",
            "radioOptions" to yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/meesExemptionCheckForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.propertyHasExemption) {
                true -> HasMeesExemptionMode.HAS_EXEMPTION
                false -> HasMeesExemptionMode.NO_EXEMPTION
                null -> null
            }
        }
}

@JourneyFrameworkComponent
final class HasMeesExemptionStep(
    stepConfig: HasMeesExemptionStepConfig,
) : RequestableStep<HasMeesExemptionMode, MeesExemptionCheckFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "has-mees-exemption"
    }
}

enum class HasMeesExemptionMode {
    HAS_EXEMPTION,
    NO_EXEMPTION,
}
