package uk.gov.communities.prsdb.webapp.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class PropertyAlreadyReactivatedException(
    propertyOwnershipId: Long,
) : PrsdbWebException("Property ownership $propertyOwnershipId has already been reactivated")
