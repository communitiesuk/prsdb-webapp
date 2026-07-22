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
        updateLicenceType: LicensingType?,
        updateLicenceNumber: String?,
    ): License? =
        if (!licenceShouldBeStored(updateLicenceType)) {
            license?.let { licenseRepository.delete(license) }
            null
        } else if (license == null) {
            createLicense(updateLicenceType!!, updateLicenceNumber!!)
        } else {
            updateLicenceType?.let { license.licenseType = it }
            updateLicenceNumber?.let { license.licenseNumber = it }
            licenseRepository.save(license)
            license
        }

    // A licence is only stored when the landlord selects a licence type other than NO_LICENSING and has not opted to
    // provide the licensing details later. NO_LICENSING and "provide this later" are both represented by a null licence.
    companion object {
        fun licenceShouldBeStored(
            licenceType: LicensingType?,
            licenseProvideLater: Boolean = false,
        ): Boolean = licenceType != null && licenceType != LicensingType.NO_LICENSING && !licenseProvideLater
    }
}
