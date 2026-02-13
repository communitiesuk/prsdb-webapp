# ADR-0040: seeding-nft

## Status

Accepted

Date of decision: 13/02/2026

## Context and Problem Statement

For performance testing in our NFT (non-functional test) environment to be an accurate approximation of our real-world usage, we’ll need a
realistic data set of landlords and property registrations on it. We need a repeatable method of generating this data and restoring the NFT
environment to an initial seeded state.

## Considered Options

* Dynamically generating the data via an SQL script
* Dynamically generating the data via an ECS instance
* Initially generating the data as CSVs, then restoring via \copy
* Initially generating the data, then restoring via \copy
* Initially generating the data, then restoring via pg_dump and pg_restore
* Initially generating the data, then restoring via an RDS snapshot

## Decision Outcome

Initially generating the data (via an ECS instance), then restoring via pg_dump and pg_restore, because it provides the best balance of
efficiency, repeatability, and simplicity. Besides this, it also gives us the option of using a developer laptop instead of an ECS instance
for one-off or infrequent data generation.

## Pros and Cons of the Options

### Dynamically generating the data via an SQL script

Run a data-generating SQL script each time the database needs to be seeded.

* Good, because no storage overhead is required.
* Good, because we already have a data generation script (`scripts/generate-load-test-data.sql`) that we can adapt for this purpose.
* Good, because running the script requires no additional infrastructure setup.
* Bad, because generating data dynamically is much slower than restoring it from a data source.
* Bad, because it places heavy CPU load on the database during generation.
* Bad, because generated data is not identical between runs (due to random elements, different UUIDs/IDs, etc.).
* Bad, because procedural SQL has limited capabilities for complex data generation logic.

### Dynamically generating the data via an ECS instance

Each time the database needs to be seeded, spin up an ephemeral copy of the WebApp container that generates the data, then shuts itself down.

* Good, because no storage overhead is required.
* Good, because it's suited to implementing complex data generation logic.
* Good, because it can make use of external libraries like Faker to generate realistic data.
* Good, because the ECS instance will have better compute capability than our database.
* Neutral, because though it requires some infrastructure setup, we can reuse our existing ephemeral task pattern.
* Bad, because generating data dynamically is much slower than restoring it from a data source.
* Bad, because network latency between the application and database can slow down inserts.
* Bad, because generated data is not identical between runs (due to random elements, different UUIDs/IDs, etc.).
* Bad, because we would have to build the data generation logic from scratch in application code.

### Initially generating the data as CSVs, then restoring via \copy

Generate a seed data CSV file for each table, store in S3, then use the psql \copy command to restore the database.

* Good, because it is much faster than generating the data dynamically.
* Good, because data is identical between restorations.
* Good, because \copy is a native psql command.
* Good, because we can use a compressed format to save space and transfer time.
* Good, because we can generate the CSVs via an ECS instance and so gain its benefits (complex logic suitability, external libraries, better compute).
* Neutral, because although there's a storage overhead, S3 storage costs are low ($0.024/GB/month in eu-west-2).
* Neutral, because though it requires some infrastructure setup, we can reuse our existing S3 bucket pattern.
* Bad, because the initial setup is more complex than other options.
* Bad, because we would have to build the CSV generation logic from scratch in application code.
* Bad, because restoring the database schema must be handled separately.
* Bad, because we would have to manage CSV file load order based on table dependencies.

### Initially generating the data, then restoring via \copy

Seed the database once (via one of the dynamic generation methods), export using the psql \copy TO command with binary format, store the
binary files in S3, then restore using \copy FROM.

* Good, because it is faster than generating the data dynamically.
* Good, because binary format is more efficient than CSV text format.
* Good, because data is identical between restorations.
* Good, because \copy is a native psql command.
* Good, because binary files are slightly smaller than pg_dump files.
* Neutral, because although there's a storage overhead, S3 storage costs are low ($0.024/GB/month in eu-west-2).
* Neutral, because though it requires some infrastructure setup, we can reuse our existing S3 bucket pattern.
* Bad, because restoring the database schema must be handled separately.
* Bad, because we would have to manage binary file load order based on table dependencies.
* Bad, because it requires more complex scripting than pg_dump/pg_restore.
* Bad, because the initial setup is more complex than other options.

### Initially generating the data, then restoring via pg_dump and pg_restore

Seed the database once (via one of the dynamic generation methods), create a pg_dump, store the dump file in S3, then restore using
pg_restore to reset the database.

* Good, because it is much faster than generating the data dynamically.
* Good, because data is identical between restorations.
* Good, because it uses native PostgreSQL tools.
* Good, because we can use a compressed format and parallel jobs to save space and transfer time.
* Neutral, because although there's a storage overhead, S3 storage costs are low ($0.024/GB/month in eu-west-2).
* Neutral, because though it requires some infrastructure setup, we can reuse our existing S3 bucket pattern.
* Neutral, because it requires one-time initial seeding.
* Bad, because the initial setup is more complex than other options.

### Initially generating the data, then restoring via an RDS snapshot

Seed the database once (via one of the dynamic generation methods), take an RDS snapshot, then restore from the snapshot to reset the database.

* Good, because it is much faster than generating the data dynamically.
* Good, because data is identical between restorations.
* Good, because it is a native AWS capability with no additional tooling needed.
* Neutral, because although there's a storage overhead, snapshot storage costs are low ($0.095/GB/month in eu-west-2).
* Bad, because it cannot restore to an existing database (it requires creating a new RDS instance or replacing the existing one).
* Bad, because it requires careful instance management and potential downtime.
* Bad, because it requires additional infrastructure set-up.

## More Information

* [PostgreSQL documentation on \copy](https://www.postgresql.org/docs/current/app-psql.html#APP-PSQL-META-COMMANDS-COPY)
* [PostgreSQL documentation on pg_dump](https://www.postgresql.org/docs/current/app-pgdump.html)
* [PostgreSQL documentation on pg_restore](https://www.postgresql.org/docs/current/app-pgrestore.html)
