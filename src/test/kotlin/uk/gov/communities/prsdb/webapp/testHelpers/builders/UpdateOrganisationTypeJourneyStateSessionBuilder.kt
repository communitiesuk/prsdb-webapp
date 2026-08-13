package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.OrgType

class UpdateOrganisationTypeJourneyStateSessionBuilder :
    JourneyStateSessionBuilder<UpdateOrganisationTypeJourneyStateSessionBuilder>(),
    OrgTypeStateBuilder<UpdateOrganisationTypeJourneyStateSessionBuilder> {
    companion object {
        fun beforeTrustInterruption(orgTypes: List<OrgType>) = UpdateOrganisationTypeJourneyStateSessionBuilder().withOrgType(orgTypes)
    }
}
