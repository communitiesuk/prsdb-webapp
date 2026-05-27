package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_COMPLIANCE_TAB_MAY26_REDESIGN
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

interface GasSafetyViewModelService {
    @PrsdbFlip(name = PROPERTY_COMPLIANCE_TAB_MAY26_REDESIGN, alterBean = "gasSafetyViewModelServiceRedesign")
    fun getInsetTextKey(propertyCompliance: PropertyCompliance): String?

    @PrsdbFlip(name = PROPERTY_COMPLIANCE_TAB_MAY26_REDESIGN, alterBean = "gasSafetyViewModelServiceRedesign")
    fun fromEntity(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel>
}
