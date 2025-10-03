CREATE TABLE jpaforbeginners.public.tbl_persons (ID BIGINT NOT NULL, col_birthday DATE NOT NULL, col_dateinsert timestamp with time zone, col_dateupdate timestamp with time zone, col_deathdate DATE, col_firstname VARCHAR(150) NOT NULL, col_gender CHAR(1) NOT NULL, col_height FLOAT, col_weight FLOAT, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.tbl_addresses (ID BIGINT NOT NULL, col_city VARCHAR(255), col_country VARCHAR(255), col_isprincipal BOOLEAN, col_number VARCHAR(255), col_state VARCHAR(255), col_street VARCHAR(255), col_unit VARCHAR(255), col_zipcode VARCHAR(255), person_id BIGINT NOT NULL, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.tbl_persons_emails (person_id BIGINT, col_email VARCHAR(150) NOT NULL)
CREATE TABLE jpaforbeginners.public.tbl_persons_phones (person_id BIGINT, col_number VARCHAR(20) NOT NULL, col_type char(1))
ALTER TABLE jpaforbeginners.public.tbl_addresses ADD CONSTRAINT FK_tbl_addresses_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.tbl_persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.tbl_persons_emails ADD CONSTRAINT FK_tbl_persons_emails_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.tbl_persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.tbl_persons_phones ADD CONSTRAINT FK_tbl_persons_phones_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.tbl_persons (ID) ON DELETE CASCADE
CREATE SEQUENCE jpaforbeginners.public.persons_id_seq INCREMENT BY 50 START WITH 149
CREATE SEQUENCE jpaforbeginners.public.addresses_id_seq INCREMENT BY 50 START WITH 149
