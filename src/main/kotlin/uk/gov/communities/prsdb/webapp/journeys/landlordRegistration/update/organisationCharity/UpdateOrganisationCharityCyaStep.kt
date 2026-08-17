package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationCharity

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCharityTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CharityRegisteredWithFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberEnglandAndWalesFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberNorthernIrelandFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberScotlandFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCharityFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class UpdateOrganisationCharityCyaStepConfig(
    private val landlordService: LandlordService,
) : AbstractCheckYourAnswersStepConfig<UpdateOrganisationCharityJourneyState>() {
    override fun getStepSpecificContent(state: UpdateOrganisationCharityJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "landlordDetails.update.title",
            "sectionHeaderInfo" to SectionHeaderViewModel("landlordDetails.title", 0, 0, useNumbering = false),
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "summaryName" to "forms.update.checkCharity.summaryName",
            "summaryListData" to buildSummaryRows(state),
        )

    override fun afterStepDataIsAdded(state: UpdateOrganisationCharityJourneyState) {
        val task = state.charityTask

        if (!task.orgIsRegisteredCharityStep.formModel.notNullValue(OrgIsRegisteredCharityFormModel::charity)) {
            landlordService.updateOrganisationLandlordAsNotARegisteredCharity()
            return
        }

        val charityRegisteredWith = task.getCharityRegisteredWith()
        val charityNumber = task.getCharityNumber(charityRegisteredWith)

        if (charityNumber == null) {
            landlordService.updateOrganisationLandlordAsRegisteredCharityWithNoRegulator()
        } else {
            landlordService.updateOrganisationLandlordCharityRegistration(charityRegisteredWith, charityNumber)
        }
    }

    private fun buildSummaryRows(state: UpdateOrganisationCharityJourneyState): List<SummaryListRowViewModel> =
        buildList {
            val task = state.charityTask

            val isRegisteredCharity =
                task.orgIsRegisteredCharityStep.formModel.notNullValue(OrgIsRegisteredCharityFormModel::charity)
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "landlordDetails.org.registeredCharity",
                    isRegisteredCharity,
                    Destination.VisitableStep(
                        task.orgIsRegisteredCharityStep,
                        state.getCyaJourneyId(task.orgIsRegisteredCharityStep),
                    ),
                ),
            )

            if (isRegisteredCharity) {
                val charityRegulator =
                    task.orgCharityRegisteredWithStep.formModel.notNullValue(
                        CharityRegisteredWithFormModel::charityRegisteredWith,
                    )
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "landlordDetails.org.charityCommission",
                        charityRegulator,
                        Destination.VisitableStep(
                            task.orgCharityRegisteredWithStep,
                            state.getCyaJourneyId(task.orgCharityRegisteredWithStep),
                        ),
                    ),
                )

                if (charityRegulator != CharityRegulator.NONE) {
                    add(charityNumberRow(state, task, charityRegulator))
                }
            }
        }

    private fun charityNumberRow(
        state: UpdateOrganisationCharityJourneyState,
        task: OrgCharityTask,
        regulator: CharityRegulator,
    ): SummaryListRowViewModel {
        val headingKey = "landlordDetails.org.charityNumber"
        return when (regulator) {
            CharityRegulator.ENGLAND_AND_WALES -> {
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    headingKey,
                    task.orgCharityNumberEnglandAndWalesStep.formModel.notNullValue(
                        OrgCharityNumberEnglandAndWalesFormModel::charityNumber,
                    ),
                    Destination.VisitableStep(
                        task.orgCharityNumberEnglandAndWalesStep,
                        state.getCyaJourneyId(task.orgCharityNumberEnglandAndWalesStep),
                    ),
                )
            }

            CharityRegulator.NORTHERN_IRELAND -> {
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    headingKey,
                    task.orgCharityNumberNorthernIrelandStep.formModel.notNullValue(
                        OrgCharityNumberNorthernIrelandFormModel::charityNumber,
                    ),
                    Destination.VisitableStep(
                        task.orgCharityNumberNorthernIrelandStep,
                        state.getCyaJourneyId(task.orgCharityNumberNorthernIrelandStep),
                    ),
                )
            }

            CharityRegulator.SCOTLAND -> {
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    headingKey,
                    task.orgCharityNumberScotlandStep.formModel.notNullValue(OrgCharityNumberScotlandFormModel::charityNumber),
                    Destination.VisitableStep(
                        task.orgCharityNumberScotlandStep,
                        state.getCyaJourneyId(task.orgCharityNumberScotlandStep),
                    ),
                )
            }

            CharityRegulator.NONE -> {
                throw PrsdbWebException("charityNumberRow should only be called for a regulator that issues a charity number")
            }
        }
    }

    private fun OrgCharityTask.getCharityRegisteredWith() =
        orgCharityRegisteredWithStep.formModel.notNullValue(CharityRegisteredWithFormModel::charityRegisteredWith)

    private fun OrgCharityTask.getCharityNumber(charityRegisteredWith: CharityRegulator) =
        when (charityRegisteredWith) {
            CharityRegulator.ENGLAND_AND_WALES -> {
                orgCharityNumberEnglandAndWalesStep.formModel.notNullValue(OrgCharityNumberEnglandAndWalesFormModel::charityNumber)
            }

            CharityRegulator.NORTHERN_IRELAND -> {
                orgCharityNumberNorthernIrelandStep.formModel.notNullValue(OrgCharityNumberNorthernIrelandFormModel::charityNumber)
            }

            CharityRegulator.SCOTLAND -> {
                orgCharityNumberScotlandStep.formModel.notNullValue(OrgCharityNumberScotlandFormModel::charityNumber)
            }

            CharityRegulator.NONE -> {
                null
            }
        }
}

@JourneyFrameworkComponent
final class UpdateOrganisationCharityCyaStep(
    stepConfig: UpdateOrganisationCharityCyaStepConfig,
) : AbstractCheckYourAnswersStep<UpdateOrganisationCharityJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-charity-check-your-answers"
    }
}
