package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.WhoProvidesRentalDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class WhoProvidesRentalDetailsStepConfig :
    AbstractRequestableStepConfig<WhoProvidesRentalDetailsMode, WhoProvidesRentalDetailsFormModel, JourneyState>() {
    override val formModelClass = WhoProvidesRentalDetailsFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerProperty.whoProvidesRentalDetails.fieldSetHeading",
            "fieldSetHint" to "registerProperty.whoProvidesRentalDetails.fieldSetHint",
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

    override fun chooseTemplate(state: JourneyState): String = "forms/whoProvidesRentalDetailsForm"

    override fun mode(state: JourneyState): WhoProvidesRentalDetailsMode? =
        getFormModelFromStateOrNull(state)?.whoProvides?.let {
            when (it) {
                WhoProvidesRentalDetails.LANDLORD -> WhoProvidesRentalDetailsMode.LANDLORD_PROVIDES
                WhoProvidesRentalDetails.LETTING_AGENT -> WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES
            }
        }

    override fun afterStepDataIsAdded(state: WhoProvidesDetailsState) {
        state.cachedWhoProvidesRentalDetails = getFormModelFromState(state).whoProvides
    }
}

@JourneyFrameworkComponent
final class WhoProvidesRentalDetailsStep(
    stepConfig: WhoProvidesRentalDetailsStepConfig,
) : RequestableStep<WhoProvidesRentalDetailsMode, WhoProvidesRentalDetailsFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "who-provides-rental-details"
    }
}

enum class WhoProvidesRentalDetailsMode {
    LANDLORD_PROVIDES,
    LETTING_AGENT_PROVIDES,
}
