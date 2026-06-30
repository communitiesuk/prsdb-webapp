package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentIncludesBillsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.BillsIncludedFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxButtonViewModel

@JourneyFrameworkComponent
class BillsIncludedStepConfig : AbstractRequestableStepConfig<Complete, BillsIncludedFormModel, RentIncludesBillsState>() {
    override val formModelClass = BillsIncludedFormModel::class

    override fun getStepSpecificContent(state: RentIncludesBillsState) =
        mapOf(
            "fieldSetHeading" to "forms.billsIncluded.fieldSetHeading",
            "fieldSetHint" to "forms.billsIncluded.fieldSetHint",
            "checkboxOptions" to
                listOf(
                    CheckboxButtonViewModel(
                        value = BillsIncluded.GAS,
                        labelMsgKey = "forms.billsIncluded.checkbox.gas",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.ELECTRICITY,
                        labelMsgKey = "forms.billsIncluded.checkbox.electricity",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.WATER,
                        labelMsgKey = "forms.billsIncluded.checkbox.water",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.COUNCIL_TAX,
                        labelMsgKey = "forms.billsIncluded.checkbox.councilTax",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.LANDLINE,
                        labelMsgKey = "forms.billsIncluded.checkbox.landline",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.BROADBAND,
                        labelMsgKey = "forms.billsIncluded.checkbox.broadband",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.TV_LICENCE,
                        labelMsgKey = "forms.billsIncluded.checkbox.tvLicence",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.CABLE_OR_SATELLITE_TV,
                        labelMsgKey = "forms.billsIncluded.checkbox.cableSatelliteTV",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.PARKING,
                        labelMsgKey = "forms.billsIncluded.checkbox.parking",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.GARDENING,
                        labelMsgKey = "forms.billsIncluded.checkbox.gardening",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.CLEANER_FOR_COMMUNAL_AREAS,
                        labelMsgKey = "forms.billsIncluded.checkbox.communalAreasCleaner",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.SOMETHING_ELSE,
                        labelMsgKey = "forms.billsIncluded.checkbox.somethingElse",
                        conditionalFragment = "customBillsIncludedInput",
                    ),
                ),
        )

    override fun chooseTemplate(state: RentIncludesBillsState): String = "forms/billsIncludedForm"

    override fun mode(state: RentIncludesBillsState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class BillsIncludedStep(
    stepConfig: BillsIncludedStepConfig,
) : RequestableStep<Complete, BillsIncludedFormModel, RentIncludesBillsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "bills-included"
    }
}
