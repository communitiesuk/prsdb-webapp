CREATE FUNCTION gin_landlord_details(VARIADIC details TEXT[])
RETURNS TEXT
IMMUTABLE
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN array_to_string(details, ' ');
END;
$$;

CREATE FUNCTION gist_landlord_details(VARIADIC details TEXT[])
RETURNS TEXT
IMMUTABLE
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN array_to_string(details, ' ');
END;
$$;

CREATE INDEX landlord_details_gin_idx ON landlord USING gin (gin_landlord_details(phone_number, email, name) gin_trgm_ops);

CREATE INDEX landlord_details_gist_idx ON landlord USING gist (gist_landlord_details(phone_number, email, name) gist_trgm_ops(siglen=256));

CREATE INDEX ON property_ownership USING hash (primary_landlord_id) WHERE is_active;
