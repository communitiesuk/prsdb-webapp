package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.WhoProvidesDetailsState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.WhoProvidesRentalDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-1388: this is a skeleton page. Replace the placeholder heading/labels
// ("Me" / "Letting agent") and wire the real content when the page is built.
@JourneyFrameworkComponent
class WhoProvidesRentalDetailsStepConfig :
    AbstractRequestableStepConfig<WhoProvidesRentalDetailsMode, WhoProvidesRentalDetailsFormModel, WhoProvidesDetailsState>() {
    override val formModelClass = WhoProvidesRentalDetailsFormModel::class

    override fun getStepSpecificContent(state: WhoProvidesDetailsState) =
        mapOf(
            "fieldSetHeading" to "registerProperty.whoProvidesRentalDetails.fieldSetHeading",
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = WhoProvidesRentalDetails.LANDLORD,
                        labelMsgKey = "registerProperty.whoProvidesRentalDetails.radios.option.landlord.label",
                    ),
                    RadiosButtonViewModel(
                        value = WhoProvidesRentalDetails.LETTING_AGENT,
                        labelMsgKey = "registerProperty.whoProvidesRentalDetails.radios.option.lettingAgent.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: WhoProvidesDetailsState): String = "forms/whoProvidesRentalDetailsForm"

    override fun mode(state: WhoProvidesDetailsState): WhoProvidesRentalDetailsMode? =
        getFormModelFromStateOrNull(state)?.whoProvides?.let {
            when (it) {
                WhoProvidesRentalDetails.LANDLORD -> WhoProvidesRentalDetailsMode.LANDLORD_PROVIDES
                WhoProvidesRentalDetails.LETTING_AGENT -> WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES
            }
        }
}

@JourneyFrameworkComponent
final class WhoProvidesRentalDetailsStep(
    stepConfig: WhoProvidesRentalDetailsStepConfig,
) : RequestableStep<WhoProvidesRentalDetailsMode, WhoProvidesRentalDetailsFormModel, WhoProvidesDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "who-provides-rental-details"
    }
}

enum class WhoProvidesRentalDetailsMode {
    LANDLORD_PROVIDES,
    LETTING_AGENT_PROVIDES,
}
