package uk.gov.communities.prsdb.webapp.exceptions

import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole

class NoLandlordEmailRecipientsException(
    landlordId: Long,
    orgRolesToSendTo: List<OrganisationalLandlordUserRole>,
) : PrsdbWebException("No recipients found for landlord $landlordId with roles to send to $orgRolesToSendTo")
