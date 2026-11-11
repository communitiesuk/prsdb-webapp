package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.database.repository.SuperiorLandlordRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@PrsdbWebService
class SuperiorLandlordService(
    private val superiorLandlordRepository: SuperiorLandlordRepository,
    private val addressService: AddressService,
) {
    // Modifies the list of existing superior landlords to match the list the user submitted.
    // Returns the newly added superior landlords so they can be tracked by the journey and notified.
    // TODO PDJB-1994: use this to update superior landlords.
    // TODO PDJB-2171: also return updated and removed superior landlords so they can be tracked by
    //  the journey and notified.
    @Transactional
    fun updateSuperiorLandlords(
        propertyOwnership: PropertyOwnership,
        superiorLandlords: List<SuperiorLandlordDataModel>,
    ): List<SuperiorLandlordDataModel> {
        val existingSuperiorLandlordsKeyedById =
            superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyOwnership.id).associateBy { it.id }
        val submittedIds = superiorLandlords.mapNotNull { it.databaseId }.toSet()

        existingSuperiorLandlordsKeyedById.values.filter { it.id !in submittedIds }.forEach { removeSuperiorLandlord(it) }

        val added = mutableListOf<SuperiorLandlordDataModel>()

        superiorLandlords.forEach { submitted ->
            // An id that no longer matches a saved row means it was deleted elsewhere, so treat it as
            // a new superior landlord rather than losing the user's answers.
            val existingSuperiorLandlord = submitted.databaseId?.let { existingSuperiorLandlordsKeyedById[it] }
            when {
                existingSuperiorLandlord == null -> {
                    createSuperiorLandlord(propertyOwnership, submitted)
                    added.add(submitted)
                }
                // One the user never started editing can't have changed. Skipping the comparison
                // matters for a manual address, which has no UPRN and so would look changed - and be
                // rewritten, saving a duplicate address row - on every submission.
                !submitted.editedInJourney -> Unit
                detailsHaveChanged(existingSuperiorLandlord, submitted) ->
                    updateSuperiorLandlord(existingSuperiorLandlord, submitted)
                // One edited but left unchanged matches no branch, so their row is left alone.
            }
        }

        return added
    }

    private fun detailsHaveChanged(
        existing: SuperiorLandlord,
        submitted: SuperiorLandlordDataModel,
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
    fun createSuperiorLandlords(
        propertyOwnership: PropertyOwnership,
        superiorLandlords: List<SuperiorLandlordDataModel>,
    ) {
        superiorLandlords.forEach { createSuperiorLandlord(propertyOwnership, it) }
    }

    @Transactional
    fun createSuperiorLandlord(
        propertyOwnership: PropertyOwnership,
        superiorLandlord: SuperiorLandlordDataModel,
    ) {
        superiorLandlordRepository.save(
            SuperiorLandlord(
                propertyOwnership = propertyOwnership,
                name = superiorLandlord.name,
                email = superiorLandlord.email,
                address = superiorLandlord.address?.let { addressService.findOrCreateAddress(it) },
            ),
        )
    }

    @Transactional
    fun updateSuperiorLandlord(
        superiorLandlord: SuperiorLandlord,
        updatedDetails: SuperiorLandlordDataModel,
    ) {
        superiorLandlord.name = updatedDetails.name
        superiorLandlord.email = updatedDetails.email
        superiorLandlord.address = updatedDetails.address?.let { addressService.findOrCreateAddress(it) }
        superiorLandlordRepository.save(superiorLandlord)
    }

    @Transactional
    fun removeSuperiorLandlord(superiorLandlord: SuperiorLandlord) {
        superiorLandlordRepository.delete(superiorLandlord)
    }

    @Transactional
    fun clearSuperiorLandlords(propertyOwnership: PropertyOwnership) {
        superiorLandlordRepository.deleteByPropertyOwnership(propertyOwnership)
    }
}
