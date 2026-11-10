package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

@PrsdbWebService
class FreeholderService(
    private val freeholderRepository: FreeholderRepository,
    private val addressService: AddressService,
) {
    // Reconciles the list the user submitted against the freeholders already saved, for the update
    // journey in PDJB-1993. Returns the newly added freeholders so the caller can write to them;
    // updated and removed ones are persisted but not reported, as contacting them is PDJB-2171.
    // Freeholders whose details aren't known have nobody to contact, so may have no email or address.
    @Transactional
    fun updateFreeholders(
        propertyOwnership: PropertyOwnership,
        freeholders: List<FreeholderDataModel>,
    ): List<FreeholderDataModel> {
        val existingById = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnership.id).associateBy { it.id }
        val submittedIds = freeholders.mapNotNull { it.databaseId }.toSet()

        existingById.values.filter { it.id !in submittedIds }.forEach { removeFreeholder(it) }

        val added = mutableListOf<FreeholderDataModel>()

        freeholders.forEach { submitted ->
            // An id that no longer matches a saved row means it was deleted elsewhere, so treat it as
            // a new freeholder rather than losing the user's answers.
            val existing = submitted.databaseId?.let { existingById[it] }
            when {
                existing == null -> {
                    createFreeholder(propertyOwnership, submitted)
                    added.add(submitted)
                }
                // One the user never started editing can't have changed. Skipping the comparison
                // matters for a manual address, which has no UPRN and so would look changed - and be
                // rewritten, saving a duplicate address row - on every submission.
                !submitted.editedInJourney -> Unit
                detailsHaveChanged(existing, submitted) -> updateFreeholder(existing, submitted)
                // One edited but left unchanged matches no branch, so their row is left alone.
            }
        }

        return added
    }

    private fun detailsHaveChanged(
        existing: Freeholder,
        submitted: FreeholderDataModel,
    ): Boolean =
        existing.name != submitted.name ||
            existing.email != submitted.email ||
            addressHasChanged(existing.address, submitted.address)

    // Only a UPRN proves two addresses are the same place: findOrCreateAddress saves a new row for
    // every manual address, so without one we assume a change and accept a redundant write.
    private fun addressHasChanged(
        existing: Address?,
        submitted: AddressDataModel?,
    ): Boolean =
        when {
            existing == null && submitted == null -> false
            existing == null || submitted == null -> true
            else -> existing.uprn == null || existing.uprn != submitted.uprn
        }

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
                address = freeholder.address?.let { addressService.findOrCreateAddress(it) },
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
        freeholder.address = updatedDetails.address?.let { addressService.findOrCreateAddress(it) }
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
    //  PDJB-2015 and PDJB-1992 remove the flag-on callers; the flag-off ones remain until the flag goes.
    @Transactional
    fun createPlaceholderFreeholder(propertyOwnership: PropertyOwnership) {
        freeholderRepository.save(Freeholder(propertyOwnership = propertyOwnership))
    }
}
