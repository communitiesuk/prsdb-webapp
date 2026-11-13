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
            "fieldSetHeading" to "tenancyDetails.billsIncluded.fieldSetHeading",
            "fieldSetHint" to "tenancyDetails.billsIncluded.fieldSetHint",
            "checkboxOptions" to
                listOf(
                    CheckboxButtonViewModel(
                        value = BillsIncluded.GAS,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.gas",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.ELECTRICITY,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.electricity",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.WATER,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.water",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.COUNCIL_TAX,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.councilTax",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.LANDLINE,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.landline",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.BROADBAND,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.broadband",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.TV_LICENCE,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.tvLicence",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.CABLE_OR_SATELLITE_TV,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.cableSatelliteTV",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.PARKING,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.parking",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.GARDENING,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.gardening",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.CLEANER_FOR_COMMUNAL_AREAS,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.communalAreasCleaner",
                    ),
                    CheckboxButtonViewModel(
                        value = BillsIncluded.SOMETHING_ELSE,
                        labelMsgKey = "tenancyDetails.billsIncluded.checkbox.somethingElse",
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
