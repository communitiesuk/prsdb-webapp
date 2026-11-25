package uk.gov.communities.prsdb.webapp.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class PropertyAlreadyReactivatedException(
    propertyOwnershipId: Long,
) : ResponseStatusException(HttpStatus.NOT_FOUND, "Property ownership $propertyOwnershipId has already been reactivated")
