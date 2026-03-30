package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json.Default.encodeToString
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TemporaryCheckMatchedEpcFormModel
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

    fun withPropertyHasEpc(): SelfType {
        withSubmittedValue(
            HasEpcStep.ROUTE_SEGMENT,
            HasEpcFormModel().apply { hasCert = true },
        )
        return self()
    }

    // TODO PDJB-656: Update to use actual logic
    fun withNoEpc(): SelfType {
        withSubmittedValue(
            CheckMatchedEpcStep.MATCHED_ROUTE_SEGMENT,
            TemporaryCheckMatchedEpcFormModel().apply { checkMatchedEpcMode = CheckMatchedEpcMode.EPC_COMPLIANT.name },
        )
        withSubmittedValue(CheckEpcAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEpcLowEnergyRating(epcDataModel: EpcDataModel = MockEpcData.createEpcDataModel()): SelfType {
        withSubmittedValue(
            CheckMatchedEpcStep.MATCHED_ROUTE_SEGMENT,
            TemporaryCheckMatchedEpcFormModel().apply { checkMatchedEpcMode = CheckMatchedEpcMode.EPC_LOW_ENERGY_RATING.name },
        )
        withAdditionalData("epcRetrievedByUprn", encodeToString(serializer(), epcDataModel))
        return self()
    }
}
