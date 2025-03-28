package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.repository.LicenseRepository

@Service
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

    fun deleteLicence(license: License) {
        licenseRepository.delete(license)
    }

    @Transactional
    fun updateLicence(
        license: License?,
        updateLicenceType: LicensingType?,
        updateLicenceNumber: String?,
    ): License? =
        if (updateLicenceType == LicensingType.NO_LICENSING) {
            license?.let { deleteLicence(license) }
            null
        } else if (license == null) {
            createLicense(updateLicenceType!!, updateLicenceNumber!!)
        } else {
            updateLicenceType?.let { license.licenseType = it }
            updateLicenceNumber?.let { license.licenseNumber = it }
            license
        }
}
