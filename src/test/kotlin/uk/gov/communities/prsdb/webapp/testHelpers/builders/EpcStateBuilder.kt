package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json.Default.encodeToString
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FindEpcByCertificateNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

interface EpcStateBuilder<SelfType : EpcStateBuilder<SelfType>> {
    val additionalDataMap: MutableMap<String, String>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun withAdditionalData(
        key: String,
        value: String,
    ): SelfType

    fun self(): SelfType

    fun withEpcNotFoundByUprn(): SelfType {
        additionalDataMap.remove("epcRetrievedByUprn")
        return self()
    }

    fun withEpcRetrievedByUprn(epcDataModel: EpcDataModel = MockEpcData.createEpcDataModel()): SelfType {
        withAdditionalData("epcRetrievedByUprn", encodeToString(serializer(), epcDataModel))
        return self()
    }

    fun withEpcProvideLater(): SelfType {
        withSubmittedValue(
            HasEpcStep.ROUTE_SEGMENT,
            HasEpcFormModel().apply { action = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME },
        )
        return self()
    }

    fun withPropertyHasEpc(): SelfType {
        withSubmittedValue(
            HasEpcStep.ROUTE_SEGMENT,
            HasEpcFormModel().apply { hasCert = true },
        )
        return self()
    }

    // TODO PDJB-656: Update to use actual logic
    fun withNoEpc(epcDataModel: EpcDataModel = MockEpcData.createEpcDataModel()): SelfType {
        withEpcFoundByUprn(epcDataModel)
        withSubmittedValue(CheckEpcAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEpcFoundByUprn(epcDataModel: EpcDataModel = MockEpcData.createEpcDataModel()): SelfType {
        withEpcRetrievedByUprn(epcDataModel)
        withSubmittedValue(
            ConfirmEpcDetailsRetrievedByUprnStep.ROUTE_SEGMENT,
            CheckMatchedEpcFormModel().apply { matchedEpcIsCorrect = true },
        )
        withAdditionalData("acceptedEpc", encodeToString(serializer(), epcDataModel))
        return self()
    }

    fun withEpcLowEnergyRating(epcDataModel: EpcDataModel = MockEpcData.createEpcDataModel(energyRating = "F")): SelfType {
        withEpcFoundByUprn(epcDataModel)
        return self()
    }

    fun withHasMeesExemption(hasExemption: Boolean): SelfType {
        val formModel = MeesExemptionCheckFormModel().apply { propertyHasExemption = hasExemption }
        withSubmittedValue(HasMeesExemptionStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withFindYourEpc(epcDataModel: EpcDataModel = MockEpcData.createEpcDataModel()): SelfType {
        withSubmittedValue(
            FindYourEpcStep.ROUTE_SEGMENT,
            FindEpcByCertificateNumberFormModel().apply { certificateNumber = epcDataModel.certificateNumber },
        )
        withAdditionalData("epcRetrievedByCertificateNumber", encodeToString(serializer(), epcDataModel))
        return self()
    }

    fun withMeesExemptionReason(exemptionReason: MeesExemptionReason): SelfType {
        val formModel = MeesExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        withSubmittedValue(MeesExemptionStep.ROUTE_SEGMENT, formModel)
        return self()
    }
}
