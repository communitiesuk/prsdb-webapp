package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationDataModel

class MockPropertyRegistrationData {
    companion object {
        fun createPropertyRegistrationDataModel() =
            PropertyRegistrationDataModel(
                addressModel = AddressDataModel("1 Example Road, EG1 2AB"),
                propertyType = PropertyType.DETACHED_HOUSE,
                licenseType = LicensingType.NO_LICENSING,
                licenceNumber = "",
                ownershipType = OwnershipType.FREEHOLD,
                isOccupied = false,
                numberOfHouseholds = 0,
                numberOfPeople = 0,
                numBedrooms = null,
                billsIncludedList = null,
                customBillsIncluded = null,
                furnishedStatus = null,
                rentFrequency = null,
                customRentFrequency = null,
                rentAmount = null,
                customPropertyType = null,
            )
    }
}
