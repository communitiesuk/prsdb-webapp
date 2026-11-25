package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AddressInUkFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class AddressInUkStepConfig : AbstractRequestableStepConfig<YesOrNo, AddressInUkFormModel, JourneyState>() {
    override val formModelClass = AddressInUkFormModel::class

    private var missingAnswerErrorKey: String? = null

    fun withMissingAnswerError(messageKey: String): AddressInUkStepConfig {
        missingAnswerErrorKey = messageKey
        return this
    }

    // No default heading, journey must add to addressInUkContentProperties a fieldSetHeading
    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/addressInUkForm"

    // Checked here rather than on the form model because the message differs per journey
    override fun afterPrimaryValidation(
        state: JourneyState,
        bindingResult: BindingResult,
    ) {
        if (bindingResult.getFormModel().addressInUk == null) {
            val messageKey =
                checkNotNull(missingAnswerErrorKey) { "AddressInUkStepConfig requires withMissingAnswerError to be called" }
            bindingResult.rejectValueWithMessageKey(AddressInUkFormModel::addressInUk.name, messageKey)
        }
    }

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.addressInUk?.let {
            if (it) YesOrNo.YES else YesOrNo.NO
        }
}

@JourneyFrameworkComponent
final class AddressInUkStep(
    stepConfig: AddressInUkStepConfig,
) : RequestableStep<YesOrNo, AddressInUkFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "address-in-uk"
    }
}
