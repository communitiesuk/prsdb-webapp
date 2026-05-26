package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import java.time.LocalDate

object EpcDetailCardBuilder {
    private const val TITLE_KEY = "propertyCompliance.epcTask.checkEpcAnswers.epc.yourEpc"
    private const val VIEW_FULL_EPC_KEY = "propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc"
    private const val ADDRESS_KEY = "propertyCompliance.epcTask.checkEpcAnswers.epc.address"
    private const val ENERGY_RATING_KEY = "propertyCompliance.epcTask.checkEpcAnswers.epc.energyRating"
    private const val EXPIRY_DATE_KEY = "propertyCompliance.epcTask.checkEpcAnswers.epc.expiryDate"
    private const val CERTIFICATE_NUMBER_KEY = "propertyCompliance.epcTask.checkEpcAnswers.epc.certificateNumber"

    fun build(
        epcUrl: String,
        address: String? = null,
        energyRating: String,
        expiryDate: LocalDate,
        certificateNumber: String,
        additionalActions: List<SummaryCardActionViewModel> = emptyList(),
    ): SummaryCardViewModel {
        val rows =
            buildList {
                if (address != null) {
                    add(SummaryListRowViewModel(ADDRESS_KEY, address))
                }
                add(SummaryListRowViewModel(ENERGY_RATING_KEY, energyRating))
                add(SummaryListRowViewModel(EXPIRY_DATE_KEY, expiryDate))
                add(SummaryListRowViewModel(CERTIFICATE_NUMBER_KEY, certificateNumber))
            }

        val actions =
            buildList {
                add(SummaryCardActionViewModel(VIEW_FULL_EPC_KEY, epcUrl, opensInNewTab = true))
                addAll(additionalActions)
            }

        return SummaryCardViewModel(
            title = TITLE_KEY,
            summaryList = rows,
            actions = actions,
        )
    }
}
