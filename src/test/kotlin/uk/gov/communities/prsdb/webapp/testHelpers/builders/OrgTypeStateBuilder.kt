package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgTypeFormModel

interface OrgTypeStateBuilder<SelfType : OrgTypeStateBuilder<SelfType>> {
    val submittedValueMap: MutableMap<String, FormModel>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withOrgType(orgTypes: List<OrgType> = listOf(OrgType.COMPANY)): SelfType {
        val formModel = OrgTypeFormModel().apply { this.orgTypes = orgTypes.map { it.name }.toMutableList() }
        withSubmittedValue(OrgTypeStep.ROUTE_SEGMENT, formModel)
        return self()
    }
}
