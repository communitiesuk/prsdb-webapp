package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

interface ElectricalSafetyStateBuilder<SelfType : ElectricalSafetyStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    // TODO PDJB-646: Update to use actual logic
    fun withNoElectricalSupply(): SelfType {
        withSubmittedValue(HasElectricalCertStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(ElectricalCertIssueDateStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(UploadElectricalCertStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckElectricalCertUploadsStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(RemoveElectricalCertUploadStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(ElectricalCertExpiredStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(ElectricalCertMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(ProvideElectricalCertLaterStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }
}
