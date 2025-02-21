package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createProperty
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class RegisteredPropertyViewModelTests {
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Returns a corresponding RegisteredPropertyViewModel from a PropertyOwnership when`(isLaView: Boolean) {
        val address = "11 Example Road, EG1 2AB"
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val localAuthority = LocalAuthority(11, "DERBYSHIRE DALES DISTRICT COUNCIL", "1045")

        val property = createProperty(address = createAddress(address, localAuthority))

        val propertyOwnership =
            createPropertyOwnership(
                property = property,
                registrationNumber = registrationNumber,
                license = null,
                currentNumTenants = 0,
            )

        val expectedLocalAuthority = localAuthority.name
        val expectedRegistrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber).toString()
        val expectedPropertyLicence = "forms.checkPropertyAnswers.propertyDetails.noLicensing"
        val expectedIsTenantedMessageKey = "commonText.no"
        val expectedRecordLink = PropertyDetailsController.getPropertyDetailsPath(property.id, isLaView)

        val expectedRegisteredPropertyViewModel =
            RegisteredPropertyViewModel(
                address,
                expectedRegistrationNumber,
                expectedLocalAuthority,
                expectedPropertyLicence,
                expectedIsTenantedMessageKey,
                expectedRecordLink,
            )

        val result = RegisteredPropertyViewModel.fromPropertyOwnership(propertyOwnership, isLaView)

        assertEquals(expectedRegisteredPropertyViewModel, result)
    }

    @ParameterizedTest
    @CsvSource(
        "0,commonText.no",
        "1,commonText.yes",
        "2,commonText.yes",
    )
    fun `Returns correct isTenanted message key`(
        currentNumTenants: Int,
        expectedMessageKey: String,
    ) {
        val propertyOwnership = createPropertyOwnership(currentNumTenants = currentNumTenants)

        val result = RegisteredPropertyViewModel.fromPropertyOwnership(propertyOwnership)

        assertEquals(result.isTenantedMessageKey, expectedMessageKey)
    }

    @ParameterizedTest
    @CsvSource(
        "SELECTIVE_LICENCE,forms.licensingType.radios.option.selectiveLicence.label",
        "HMO_MANDATORY_LICENCE,forms.licensingType.radios.option.hmoMandatory.label",
        "HMO_ADDITIONAL_LICENCE,forms.licensingType.radios.option.hmoAdditional.label",
        "NO_LICENSING,forms.checkPropertyAnswers.propertyDetails.noLicensing",
    )
    fun `Returns correct licensing display name for licence`(
        licensingType: LicensingType,
        expectedDisplayName: String,
    ) {
        val licence = License(licensingType, "testLicenseNumber")

        val propertyOwnership = createPropertyOwnership(license = licence)

        val result = RegisteredPropertyViewModel.fromPropertyOwnership(propertyOwnership)

        assertEquals(result.licenseTypeMessageKey, expectedDisplayName)
    }

    @Test
    fun `Returns correct licensing display name for property with no licence`() {
        val propertyOwnership = createPropertyOwnership(license = null)

        val result = RegisteredPropertyViewModel.fromPropertyOwnership(propertyOwnership)

        assertEquals(result.licenseTypeMessageKey, "forms.checkPropertyAnswers.propertyDetails.noLicensing")
    }
}
