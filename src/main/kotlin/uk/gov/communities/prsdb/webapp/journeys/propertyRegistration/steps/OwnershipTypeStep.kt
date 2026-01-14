package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class OwnershipTypeStepConfig : AbstractGenericRequestableStepConfig<Complete, OwnershipTypeFormModel, JourneyState>() {
    override val formModelClass = OwnershipTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.ownershipType.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = OwnershipType.FREEHOLD,
                        labelMsgKey = "forms.ownershipType.radios.option.freehold.label",
                        hintMsgKey = "forms.ownershipType.radios.option.freehold.hint",
                    ),
                    RadiosButtonViewModel(
                        value = OwnershipType.LEASEHOLD,
                        labelMsgKey = "forms.ownershipType.radios.option.leasehold.label",
                        hintMsgKey = "forms.ownershipType.radios.option.leasehold.hint",
                    ),
                    RadiosButtonViewModel(
                        value = OwnershipType.SHARE_OF_FREEHOLD,
                        labelMsgKey = "forms.ownershipType.radios.option.shareOfFreehold.label",
                        hintMsgKey = "forms.ownershipType.radios.option.shareOfFreehold.hint",
                    ),
                    RadiosButtonViewModel(
                        value = OwnershipType.COMMONHOLD,
                        labelMsgKey = "forms.ownershipType.radios.option.commonhold.label",
                        hintMsgKey = "forms.ownershipType.radios.option.commonhold.hint",
                    ),
                ),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/ownershipTypeForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OwnershipTypeStep(
    stepConfig: OwnershipTypeStepConfig,
) : RequestableStep<Complete, OwnershipTypeFormModel, JourneyState>(stepConfig)
