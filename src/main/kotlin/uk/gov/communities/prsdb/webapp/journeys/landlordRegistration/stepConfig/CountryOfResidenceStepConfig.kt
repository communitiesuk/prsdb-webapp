package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class CountryOfResidenceStepConfig : AbstractRequestableStepConfig<CountryOfResidenceMode, CountryOfResidenceFormModel, JourneyState>() {
    override val formModelClass = CountryOfResidenceFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.countryOfResidence.fieldSetHeading",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/countryOfResidenceForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.livesInEnglandOrWales == true) {
                CountryOfResidenceMode.ENGLAND_OR_WALES
            } else {
                CountryOfResidenceMode.NON_ENGLAND_OR_WALES
            }
        }
}

@JourneyFrameworkComponent
final class CountryOfResidenceStep(
    stepConfig: CountryOfResidenceStepConfig,
) : RequestableStep<CountryOfResidenceMode, CountryOfResidenceFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "country-of-residence"
    }
}

enum class CountryOfResidenceMode {
    ENGLAND_OR_WALES,
    NON_ENGLAND_OR_WALES,
}
