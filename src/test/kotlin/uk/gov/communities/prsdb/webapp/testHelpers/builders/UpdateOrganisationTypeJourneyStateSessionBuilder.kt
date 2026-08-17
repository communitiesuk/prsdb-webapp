package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.OrgType

class UpdateOrganisationTypeJourneyStateSessionBuilder :
    JourneyStateSessionBuilder<UpdateOrganisationTypeJourneyStateSessionBuilder>(),
    OrgTypeStateBuilder<UpdateOrganisationTypeJourneyStateSessionBuilder>,
    LeadTrusteeStateBuilder<UpdateOrganisationTypeJourneyStateSessionBuilder> {
    companion object {
        fun beforeTrustInterruption(orgTypes: List<OrgType>) = UpdateOrganisationTypeJourneyStateSessionBuilder().withOrgType(orgTypes)

        fun beforeCyaTrustUnchanged(orgTypes: List<OrgType>) = UpdateOrganisationTypeJourneyStateSessionBuilder().withOrgType(orgTypes)

        fun beforeCyaAddingTrust(
            orgTypes: List<OrgType> = listOf(OrgType.COMPANY, OrgType.TRUST),
            trusteeName: String = "Lead Trustee",
            trusteeEmail: String = "trustee@test.com",
            trusteePhone: String = "07123456789",
        ) = UpdateOrganisationTypeJourneyStateSessionBuilder()
            .withOrgType(orgTypes)
            .withLeadTrusteeDetails(trusteeName, trusteeEmail, trusteePhone)
    }
}
