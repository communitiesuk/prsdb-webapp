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
                    local_authority_id,
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
                    local_authority_id  = EXCLUDED.local_authority_id,
                    is_active           = EXCLUDED.is_active,
                    last_modified_date  = current_timestamp
            """
        return connection.prepareStatement(query)
    }

    fun deleteUnusedInactiveAddresses() {
        val query =
            """
                -- Function returning every address.id that is referenced by any foreign key
                CREATE OR REPLACE FUNCTION used_address_ids()
                RETURNS TABLE(id BIGINT)
                LANGUAGE plpgsql
                AS $$
                DECLARE
                    r record;
                    sql text;
                BEGIN
                    FOR r IN
                        SELECT c.conrelid::regclass AS tbl, a.attname AS col
                        FROM pg_constraint c
                        JOIN pg_attribute a
                        ON a.attrelid = c.conrelid
                        AND a.attnum = ANY(c.conkey)
                        WHERE c.contype = 'f'
                        AND c.confrelid = 'address'::regclass
                    LOOP
                        sql := format('SELECT DISTINCT %I FROM %s WHERE %I IS NOT NULL', r.col, r.tbl, r.col);
                        RETURN QUERY EXECUTE sql;
                    END LOOP;
                END;
                $$;
                
                DELETE FROM address
                WHERE is_active = false
                AND id NOT IN (SELECT * FROM used_address_ids());
            """
        session.createNativeMutationQuery(query).executeUpdate()
    }
}
