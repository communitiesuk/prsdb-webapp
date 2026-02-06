package uk.gov.communities.prsdb.webapp.database.repository

import org.hibernate.StatelessSession
import java.sql.Connection
import java.sql.PreparedStatement

class NgdAddressLoaderRepository(
    private val session: StatelessSession,
) {
    fun saveCommentOnAddressTable(comment: String) {
        // Comment cannot be parameterized
        val query = "COMMENT ON TABLE address IS '$comment';"
        session.createNativeMutationQuery(query).executeUpdate()
    }

    fun findCommentOnAddressTable(): String? {
        val query = "SELECT obj_description('address'::regclass, 'pg_class') AS comment;"
        return session.createNativeQuery(query, String::class.java).singleResultOrNull
    }

    fun getLoadAddressPreparedStatement(connection: Connection): PreparedStatement {
        val query =
            """
                INSERT INTO address (
                    uprn,
                    single_line_address,
                    organisation,
                    sub_building,
                    building_name,
                    building_number,
                    street_name,
                    locality,
                    town_name,
                    postcode,
                    local_council_id,
                    is_active,
                    created_date
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)
                ON CONFLICT (uprn) DO UPDATE SET
                    single_line_address = EXCLUDED.single_line_address,
                    organisation        = EXCLUDED.organisation,
                    sub_building        = EXCLUDED.sub_building,
                    building_name       = EXCLUDED.building_name,
                    building_number     = EXCLUDED.building_number,
                    street_name         = EXCLUDED.street_name,
                    locality            = EXCLUDED.locality,
                    town_name           = EXCLUDED.town_name,
                    postcode            = EXCLUDED.postcode,
                    local_council_id    = EXCLUDED.local_council_id,
                    is_active           = EXCLUDED.is_active,
                    last_modified_date  = current_timestamp
            """
        return connection.prepareStatement(query)
    }

    fun updatePropertyOwnershipAddresses(upsertedAddressUprns: Set<Long>) {
        val query =
            """
                UPDATE property_ownership po
                SET
                    single_line_address = a.single_line_address,
                    local_council_id = a.local_council_id
                FROM address a
                WHERE po.address_id = a.id AND a.uprn IN (:upsertedAddressUprns);
            """
        session
            .createNativeMutationQuery(query)
            .setParameter("upsertedAddressUprns", upsertedAddressUprns)
            .executeUpdate()
    }

    fun deleteUnusedInactiveAddresses() {
        val query =
            """
                DO $$
                    DECLARE
                        fk record;
                        sql text;
                        fkIndexName text;
                        fkIndexNames text[];
                    BEGIN
                        CREATE TEMP TABLE tmp_used_inactive_address_ids (id BIGINT) ON COMMIT DROP;
                
                        -- This only supports single-column foreign keys that reference address.id
                        FOR fk IN
                            SELECT c.conrelid::regclass AS tbl, a.attname AS col
                            FROM pg_constraint c
                            JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)
                            WHERE c.contype = 'f' AND c.confrelid = 'address'::regclass
                        LOOP
                            sql := format(
                                'INSERT INTO tmp_used_inactive_address_ids (id)
                                 SELECT DISTINCT %I
                                 FROM %I t
                                 JOIN address a ON t.%I = a.id AND NOT a.is_active;',
                                fk.col, fk.tbl, fk.col
                               );
                            EXECUTE sql;
                
                            -- Create index on each foreign key column to speed up deletes
                            fkIndexName := format('tmp_%s_%s_idx', fk.tbl, fk.col);
                            fkIndexNames := array_append(fkIndexNames, fkIndexName);
                            sql := format('CREATE INDEX %I ON %I (%I);', fkIndexName, fk.tbl, fk.col);
                            EXECUTE sql;
                        END LOOP;
                
                        DELETE FROM address a
                        WHERE a.is_active = false
                        AND a.id NOT IN (SELECT id FROM tmp_used_inactive_address_ids);
                
                        FOREACH fkIndexName IN ARRAY fkIndexNames
                        LOOP
                            sql := format('DROP INDEX %I;', fkIndexName);
                            EXECUTE sql;
                        END LOOP;
                END $$;
            """
        session.createNativeMutationQuery(query).executeUpdate()
    }
}
