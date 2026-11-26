package uk.gov.communities.prsdb.webapp.performance

internal class WorkloadReservations {
    private val subjects = mutableSetOf<String>()
    private val properties = mutableSetOf<Long>()
    private val organisations = mutableSetOf<Long>()
    private val addresses = mutableSetOf<String>()
    private val uprns = mutableSetOf<Long>()
    private val invitations = mutableSetOf<String>()
    private val observedSubjects = mutableSetOf<String>()
    private val observedProperties = mutableSetOf<Long>()
    private val observedAddresses = mutableSetOf<String>()
    private val observedUprns = mutableSetOf<Long>()

    fun reserve(
        subjects: List<String> = emptyList(),
        properties: List<Long> = emptyList(),
        organisations: List<Long> = emptyList(),
        addresses: List<String> = emptyList(),
        uprns: List<Long> = emptyList(),
        invitations: List<String?> = emptyList(),
    ) {
        val normalizedAddresses = addresses.map(::normalizeAddress)
        require(subjects.none { it in observedSubjects }) { "Council read owners must not be mutated concurrently" }
        require(properties.none { it in observedProperties }) { "Council read properties must not be mutated concurrently" }
        require(normalizedAddresses.none { it in observedAddresses } && uprns.none { it in observedUprns }) {
            "Council read addresses must not be reused"
        }
        requireExclusive(subjects, this.subjects, "actor")
        requireExclusive(properties, this.properties, "property")
        requireExclusive(organisations, this.organisations, "organisation")
        requireExclusive(normalizedAddresses, this.addresses, "address")
        requireExclusive(uprns, this.uprns, "UPRN")
        require(invitations.all { !it.isNullOrBlank() }) { "Invitations must still have pending tokens" }
        requireExclusive(invitations.filterNotNull(), this.invitations, "invitation")
        this.subjects.addAll(subjects)
        this.properties.addAll(properties)
        this.organisations.addAll(organisations)
        this.addresses.addAll(normalizedAddresses)
        this.uprns.addAll(uprns)
        this.invitations.addAll(invitations.filterNotNull())
    }

    fun observe(
        subjects: List<String> = emptyList(),
        properties: List<Long> = emptyList(),
        addresses: List<String> = emptyList(),
        uprns: List<Long> = emptyList(),
    ) {
        val normalizedAddresses = addresses.map(::normalizeAddress)
        require(subjects.none { it in this.subjects }) { "Council read owners must not be mutated concurrently" }
        require(properties.none { it in this.properties }) { "Council read properties must not be mutated concurrently" }
        require(normalizedAddresses.none { it in this.addresses } && uprns.none { it in this.uprns }) {
            "Council read addresses must not be reused"
        }
        observedSubjects.addAll(subjects)
        observedProperties.addAll(properties)
        observedAddresses.addAll(normalizedAddresses)
        observedUprns.addAll(uprns)
    }

    private fun <T> requireExclusive(
        values: List<T>,
        reserved: Set<T>,
        kind: String,
    ) {
        require(values.toSet().size == values.size && values.none { it in reserved }) {
            "Concurrent workload families share a mutable $kind"
        }
    }

    companion object {
        fun <T> window(
            pool: List<T>,
            offset: Int,
            count: Int,
        ): List<T> {
            require(offset >= 0 && count > 0 && offset.toLong() + count <= pool.size.toLong()) {
                "Fixture pool is exhausted after warmup and preparation allocations"
            }
            return pool.subList(offset, offset + count)
        }

        private fun normalizeAddress(value: String): String = value.trim().lowercase().replace(Regex("\\s+"), " ")
    }
}
