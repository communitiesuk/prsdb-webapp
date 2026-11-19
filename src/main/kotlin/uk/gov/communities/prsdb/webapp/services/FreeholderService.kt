package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

@PrsdbWebService
class FreeholderService(
    private val freeholderRepository: FreeholderRepository,
) {
    // Updates the saved freeholders to match the list the user submitted, creating, updating and
    // deleting them one at a time.
    // Returns the newly added freeholders so they can be tracked by the journey and notified.
    // TODO PDJB-2171: also return updated and removed freeholders so they can be tracked by the
    //  journey and notified.
    @Transactional
    fun updateFreeholders(
        propertyOwnership: PropertyOwnership,
        freeholders: List<FreeholderDataModel>,
    ): List<FreeholderDataModel> {
        val existingFreeholdersKeyedById =
            freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id).associateBy { it.id }
        val submittedIds = freeholders.mapNotNull { it.databaseId }.toSet()

        existingFreeholdersKeyedById.values.filter { it.id !in submittedIds }.forEach { removeFreeholder(it) }

        val added = mutableListOf<FreeholderDataModel>()

        freeholders.forEach { submitted ->
            // An id that no longer matches a saved row means it was deleted elsewhere, so treat it as
            // a new freeholder rather than losing the user's answers.
            val existingFreeholder = submitted.databaseId?.let { existingFreeholdersKeyedById[it] }
            when {
                existingFreeholder == null -> {
                    createFreeholder(propertyOwnership, submitted)
                    added.add(submitted)
                }
                detailsHaveChanged(existingFreeholder, submitted) -> updateFreeholder(existingFreeholder, submitted)
                // An unchanged freeholder matches no branch, so their row is left alone.
            }
        }

        return added
    }

    // Postal addresses aren't stored, so an address in the submitted model means the user entered one in this journey
    private fun detailsHaveChanged(
        existing: Freeholder,
        submitted: FreeholderDataModel,
    ): Boolean =
        existing.name != submitted.name ||
            existing.email != submitted.email ||
            submitted.address != null

    @Transactional
    fun createFreeholders(
        propertyOwnership: PropertyOwnership,
        freeholders: List<FreeholderDataModel>,
    ) {
        freeholders.forEach { createFreeholder(propertyOwnership, it) }
    }

    @Transactional
    fun createFreeholder(
        propertyOwnership: PropertyOwnership,
        freeholder: FreeholderDataModel,
    ) {
        freeholderRepository.save(
            Freeholder(
                propertyOwnership = propertyOwnership,
                name = freeholder.name,
                email = freeholder.email,
            ),
        )
    }

    @Transactional
    fun updateFreeholder(
        freeholder: Freeholder,
        updatedDetails: FreeholderDataModel,
    ) {
        freeholder.name = updatedDetails.name
        freeholder.email = updatedDetails.email
        freeholderRepository.save(freeholder)
    }

    @Transactional
    fun removeFreeholder(freeholder: Freeholder) {
        freeholderRepository.delete(freeholder)
    }

    @Transactional
    fun clearFreeholders(propertyOwnership: PropertyOwnership) {
        freeholderRepository.deleteByPropertyOwnership(propertyOwnership)
    }

    // TODO PDJB-2016: remove this with the flag-off code paths, which never ask who the freeholder is.
    //  It keeps the invariant that an ownership type requiring freeholder records has at least one row.
    @Transactional
    fun createPlaceholderFreeholder(propertyOwnership: PropertyOwnership) {
        freeholderRepository.save(Freeholder(propertyOwnership = propertyOwnership))
    }
}
