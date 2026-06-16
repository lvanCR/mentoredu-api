ALTER TABLE verification_requests
    ADD COLUMN university_id UUID REFERENCES universities(id);
