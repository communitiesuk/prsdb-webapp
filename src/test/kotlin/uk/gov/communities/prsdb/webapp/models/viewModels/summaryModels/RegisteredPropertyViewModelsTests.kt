package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createProperty
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

class RegisteredPropertyViewModelsTests {
    @Nested
    inner class RegisteredPropertyLocalCouncilViewModelTests {
        @Test
        fun `Returns a corresponding RegisteredPropertyViewModel from a PropertyOwnership`() {
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
            val expectedRecordLink =
                PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLaView = true)

            val expectedRegisteredPropertyViewModel =
                RegisteredPropertyLocalCouncilViewModel(
                    address,
                    expectedRegistrationNumber,
                    expectedLocalAuthority,
                    expectedPropertyLicence,
                    expectedIsTenantedMessageKey,
                    expectedRecordLink,
                )

            val result = RegisteredPropertyLocalCouncilViewModel.fromPropertyOwnership(propertyOwnership)

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

            val result = RegisteredPropertyLocalCouncilViewModel.fromPropertyOwnership(propertyOwnership)

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

            val result = RegisteredPropertyLocalCouncilViewModel.fromPropertyOwnership(propertyOwnership)

            assertEquals(result.licenseTypeMessageKey, expectedDisplayName)
        }

        @Test
        fun `Returns correct licensing display name for property with no licence`() {
            val propertyOwnership = createPropertyOwnership(license = null)

            val result = RegisteredPropertyLocalCouncilViewModel.fromPropertyOwnership(propertyOwnership)

            assertEquals(result.licenseTypeMessageKey, "forms.checkPropertyAnswers.propertyDetails.noLicensing")
        }
    }

    @Nested
    inner class RegisteredPropertyLandlordViewModelTests {
        @Test
        fun `Returns a corresponding RegisteredPropertyLandlordViewModel from a PropertyOwnership`() {
            val propertyOwnership = createPropertyOwnership()

            val expectedRegisteredPropertyLandlordViewModel =
                RegisteredPropertyLandlordViewModel(
                    address = propertyOwnership.property.address.singleLineAddress,
                    registrationNumber =
                        RegistrationNumberDataModel
                            .fromRegistrationNumber(
                                propertyOwnership.registrationNumber,
                            ).toString(),
                    recordLink = PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id),
                )

            val result = RegisteredPropertyLandlordViewModel.fromPropertyOwnership(propertyOwnership)

            assertEquals(expectedRegisteredPropertyLandlordViewModel, result)
        }
    }
}
