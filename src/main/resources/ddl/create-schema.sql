CREATE TABLE jpaforbeginners.public.persons (ID BIGINT NOT NULL, birthday DATE NOT NULL, dateinsert TIMESTAMP WITH TIME ZONE, dateupdate TIMESTAMP WITH TIME ZONE, deathdate DATE, firstname VARCHAR(150) NOT NULL, gender CHAR(1) NOT NULL, height NUMERIC(3,2), weight NUMERIC(4,2), partner_id BIGINT, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.products (ID BIGINT NOT NULL, dateinsert TIMESTAMP WITH TIME ZONE, dateupdate TIMESTAMP WITH TIME ZONE, description VARCHAR(255) NOT NULL, discount NUMERIC(3,2), title VARCHAR(150) NOT NULL, unit SMALLINT NOT NULL, unitPrice DECIMAL(11,2) NOT NULL, validity DATE, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.addresses (ID BIGINT NOT NULL, city VARCHAR(255), country VARCHAR(255), isprincipal BOOLEAN, number VARCHAR(255), state VARCHAR(255), street VARCHAR(255), unit VARCHAR(255), zipcode VARCHAR(255), person_id BIGINT NOT NULL, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.wishlists (ID BIGINT NOT NULL, dateinsert TIMESTAMP WITH TIME ZONE, dateupdate TIMESTAMP WITH TIME ZONE, description VARCHAR(255), title VARCHAR(255), person_id BIGINT NOT NULL, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.persons_emails (person_id BIGINT, email VARCHAR(150) NOT NULL)
CREATE TABLE jpaforbeginners.public.persons_phones (person_id BIGINT NOT NULL, number VARCHAR(20) NOT NULL, type char(1))
CREATE TABLE jpaforbeginners.public.wishlists_products (product_id BIGINT NOT NULL, wishlist_id BIGINT NOT NULL, PRIMARY KEY (product_id, wishlist_id))
ALTER TABLE jpaforbeginners.public.persons ADD CONSTRAINT FK_persons_partner_id FOREIGN KEY (partner_id) REFERENCES public.persons(ID) ON DELETE SET NULL
ALTER TABLE jpaforbeginners.public.addresses ADD CONSTRAINT FK_addresses_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.wishlists ADD CONSTRAINT FK_wishlists_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.persons_emails ADD CONSTRAINT FK_persons_emails_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.persons_phones ADD CONSTRAINT FK_persons_phones_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.wishlists_products ADD CONSTRAINT FK_wishlists_products_product_id FOREIGN KEY (product_id) REFERENCES jpaforbeginners.public.products (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.wishlists_products ADD CONSTRAINT FK_wishlists_products_wishlist_id FOREIGN KEY (wishlist_id) REFERENCES jpaforbeginners.public.wishlists (ID) ON DELETE CASCADE
CREATE SEQUENCE jpaforbeginners.public.wishlists_id_seq INCREMENT BY 50 START WITH 149
CREATE SEQUENCE jpaforbeginners.public.products_id_seq INCREMENT BY 50 START WITH 149
CREATE SEQUENCE jpaforbeginners.public.persons_id_seq INCREMENT BY 50 START WITH 149
CREATE SEQUENCE jpaforbeginners.public.addresses_id_seq INCREMENT BY 50 START WITH 149
