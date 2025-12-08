package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel

open class JourneyStateSessionBuilder<SelfType : JourneyStateSessionBuilder<SelfType>> {
    val additionalDataMap = mutableMapOf<String, String>()
    val submittedValueMap = mutableMapOf<String, FormModel>()

    @Suppress("UNCHECKED_CAST")
    fun self(): SelfType = this as SelfType

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType {
        submittedValueMap[key] = value
        return self()
    }

    fun build(): Map<String, Any> {
        val sessionData = mutableMapOf<String, Any>()
        sessionData.putAll(additionalDataMap)
        sessionData["journeyData"] = submittedValueMap.mapValues { it.value.toPageData() }
        return sessionData
    }
}
