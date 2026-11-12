package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.database.repository.SuperiorLandlordRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@PrsdbWebService
class SuperiorLandlordService(
    private val superiorLandlordRepository: SuperiorLandlordRepository,
    private val addressService: AddressService,
) {
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
    fun clearSuperiorLandlords(propertyOwnership: PropertyOwnership) {
        superiorLandlordRepository.deleteByPropertyOwnership(propertyOwnership)
    }
}
