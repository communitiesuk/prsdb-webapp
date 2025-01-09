package uk.gov.communities.prsdb.webapp.mockObjects

import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

class MockLandlordData {
    companion object {
        private fun createAddress() = Address(AddressDataModel("1 Example Road, EG1 2AB"))

        private fun createLandlord() =
            Landlord(
                baseUser = OneLoginUser(),
                name = "name",
                email = "example@email.com",
                phoneNumber = "07123456789",
                address = createAddress(),
                registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 0L),
                internationalAddress = null,
                dateOfBirth = null,
            )

        private fun createProperty() =
            Property(
                status = RegistrationStatus.REGISTERED,
                propertyType = PropertyType.FLAT,
                address = createAddress(),
            )

        fun createPropertyOwnership() =
            PropertyOwnership(
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                landlordType = LandlordType.SOLE,
                ownershipType = OwnershipType.FREEHOLD,
                currentNumHouseholds = 0,
                currentNumTenants = 0,
                registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456),
                primaryLandlord = createLandlord(),
                property = createProperty(),
                license = null,
            )
    }
}
