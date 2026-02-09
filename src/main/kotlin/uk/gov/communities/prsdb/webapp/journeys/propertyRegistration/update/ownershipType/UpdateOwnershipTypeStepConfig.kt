package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class UpdateOwnershipTypeStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<Complete, OwnershipTypeFormModel, UpdateOwnershipTypeJourneyState>() {
    override val formModelClass = OwnershipTypeFormModel::class

    override fun getStepSpecificContent(state: UpdateOwnershipTypeJourneyState) =
        mapOf(
            "title" to "propertyDetails.update.title",
            "fieldSetHeading" to "forms.update.ownershipType.fieldSetHeading",
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
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "showWarning" to true,
        )

    override fun chooseTemplate(state: UpdateOwnershipTypeJourneyState): String = "forms/ownershipTypeForm"

    override fun mode(state: UpdateOwnershipTypeJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: UpdateOwnershipTypeJourneyState) {
        propertyOwnershipService.updateOwnershipType(
            state.propertyId,
            state.ownershipTypeStep.formModel.notNullValue(OwnershipTypeFormModel::ownershipType),
        )
    }
}

@JourneyFrameworkComponent
final class UpdateOwnershipTypeStep(
    stepConfig: UpdateOwnershipTypeStepConfig,
) : RequestableStep<Complete, OwnershipTypeFormModel, UpdateOwnershipTypeJourneyState>(stepConfig)
