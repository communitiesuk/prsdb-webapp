package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.BillsIncludedFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel

@JourneyFrameworkComponent
class BillsIncludedStepConfig : AbstractGenericStepConfig<Complete, BillsIncludedFormModel, OccupationState>() {
    override val formModelClass = BillsIncludedFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.billsIncluded.fieldSetHeading",
            "fieldSetHint" to "forms.billsIncluded.fieldSetHint",
            "checkboxOptions" to
                listOf(
                    CheckboxViewModel(
                        value = BillsIncluded.GAS,
                        labelMsgKey = "forms.billsIncluded.checkbox.gas",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.ELECTRICITY,
                        labelMsgKey = "forms.billsIncluded.checkbox.electricity",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.WATER,
                        labelMsgKey = "forms.billsIncluded.checkbox.water",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.COUNCIL_TAX,
                        labelMsgKey = "forms.billsIncluded.checkbox.councilTax",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.CONTENTS_INSURANCE,
                        labelMsgKey = "forms.billsIncluded.checkbox.contentsInsurance",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.BROADBAND,
                        labelMsgKey = "forms.billsIncluded.checkbox.broadband",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.TV_LICENCE,
                        labelMsgKey = "forms.billsIncluded.checkbox.tvLicence",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.CABLE_OR_SATELLITE_TV,
                        labelMsgKey = "forms.billsIncluded.checkbox.cableSatelliteTV",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.GARDENING,
                        labelMsgKey = "forms.billsIncluded.checkbox.gardening",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.CLEANER_FOR_COMMUNAL_AREAS,
                        labelMsgKey = "forms.billsIncluded.checkbox.communalAreasCleaner",
                    ),
                    CheckboxViewModel(
                        value = BillsIncluded.SOMETHING_ELSE,
                        labelMsgKey = "forms.billsIncluded.checkbox.somethingElse",
                        conditionalFragment = "customBillsIncludedInput",
                    ),
                ),
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/billsIncludedForm"

    override fun mode(state: OccupationState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class BillsIncludedStep(
    stepConfig: BillsIncludedStepConfig,
) : RequestableStep<Complete, BillsIncludedFormModel, OccupationState>(stepConfig)
