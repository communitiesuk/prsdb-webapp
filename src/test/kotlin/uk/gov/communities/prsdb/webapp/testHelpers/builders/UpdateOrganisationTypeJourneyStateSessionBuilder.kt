package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgTypeFormModel

class UpdateOrganisationTypeJourneyStateSessionBuilder :
    JourneyStateSessionBuilder<UpdateOrganisationTypeJourneyStateSessionBuilder>() {
    fun withOrgType(orgTypes: List<OrgType>): UpdateOrganisationTypeJourneyStateSessionBuilder {
        val formModel = OrgTypeFormModel().apply { this.orgTypes = orgTypes.map { it.name }.toMutableList() }
        withSubmittedValue(OrgTypeStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    companion object {
        fun beforeTrustInterruption(orgTypes: List<OrgType>) = UpdateOrganisationTypeJourneyStateSessionBuilder().withOrgType(orgTypes)
    }
}
