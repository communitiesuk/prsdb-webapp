package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel

interface JointLandlordsStateBuilder<SelfType : JointLandlordsStateBuilder<SelfType>> {
    val submittedValueMap: MutableMap<String, FormModel>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withNoJointLandlords(): SelfType = withHasJointLandlordsSetToFalse()

    fun withHasJointLandlordsSetToFalse(): SelfType {
        val hasJointLandlordsFormModel =
            HasJointLandlordsFormModel().apply {
                hasJointLandlords = false
            }
        withSubmittedValue(RegisterPropertyStepId.HasJointLandlords.urlPathSegment, hasJointLandlordsFormModel)
        return self()
    }

    fun withHasJointLandlords(hasJointLandlords: Boolean): SelfType {
        val hasJointLandlordsFormModel =
            HasJointLandlordsFormModel().apply {
                this.hasJointLandlords = hasJointLandlords
            }
        withSubmittedValue(RegisterPropertyStepId.HasJointLandlords.urlPathSegment, hasJointLandlordsFormModel)
        return self()
    }

    fun withJointLandlords(): SelfType {
        withHasJointLandlords(true)
        @Suppress("UNCHECKED_CAST")
        return self()
    }
}
