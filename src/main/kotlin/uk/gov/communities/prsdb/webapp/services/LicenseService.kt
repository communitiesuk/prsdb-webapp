package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.repository.LicenseRepository

@PrsdbWebService
class LicenseService(
    private val licenseRepository: LicenseRepository,
) {
    fun createLicense(
        licenseType: LicensingType,
        licenceNumber: String,
    ): License =
        licenseRepository.save(
            License(
                licenseType,
                licenceNumber,
            ),
        )

    fun updateLicence(
        license: License?,
        updateLicenceType: LicensingType,
        updateLicenceNumber: String?,
    ): License? =
        if (!licenceShouldBeStored(updateLicenceType)) {
            license?.let { licenseRepository.delete(license) }
            null
        } else if (license == null) {
            createLicense(updateLicenceType, updateLicenceNumber!!)
        } else {
            license.licenseType = updateLicenceType
            updateLicenceNumber?.let { license.licenseNumber = it }
            licenseRepository.save(license)
            license
        }

    // A licence is only stored when the landlord selects a licence type other than NO_LICENSING or PROVIDE_LATER.
    // NO_LICENSING and PROVIDE_LATER are both represented by a null licence, but are distinguished by the
    // licenseProvideLater flag on the property ownership.
    companion object {
        fun licenceShouldBeStored(licenceType: LicensingType): Boolean =
            licenceType != LicensingType.NO_LICENSING && licenceType != LicensingType.PROVIDE_LATER
    }
}
