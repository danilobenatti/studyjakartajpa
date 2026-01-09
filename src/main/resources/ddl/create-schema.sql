CREATE TABLE jpaforbeginners.public.persons (ID BIGINT NOT NULL, birthdate DATE NOT NULL, dateinsert TIMESTAMP WITH TIME ZONE, dateupdate TIMESTAMP WITH TIME ZONE, deathdate DATE, firstname VARCHAR(150) NOT NULL, gender CHAR(1) NOT NULL, height NUMERIC(3,2), weight NUMERIC(4,2), partner_id BIGINT, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.products (ID BIGINT NOT NULL, active BOOLEAN, dateinsert TIMESTAMP WITH TIME ZONE, dateupdate TIMESTAMP WITH TIME ZONE, description VARCHAR(255) NOT NULL, discount NUMERIC(4,2), title VARCHAR(150) NOT NULL, unit SMALLINT NOT NULL, unitPrice DECIMAL(18,2) NOT NULL, validity DATE, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.addresses (ID BIGINT NOT NULL, city VARCHAR(255), country VARCHAR(255), isprincipal BOOLEAN, number VARCHAR(255), state VARCHAR(255), street VARCHAR(255), unit VARCHAR(255), zipcode VARCHAR(255), person_id BIGINT NOT NULL, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.wishlists (ID BIGINT NOT NULL, dateinsert TIMESTAMP WITH TIME ZONE, dateupdate TIMESTAMP WITH TIME ZONE, description VARCHAR(255), title VARCHAR(255), person_id BIGINT NOT NULL, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.orders (ID BIGINT NOT NULL, billingdate DATE NOT NULL, dateinsert TIMESTAMP WITH TIME ZONE, dateupdate TIMESTAMP WITH TIME ZONE, discount NUMERIC(4,2), status SMALLINT NOT NULL, total DECIMAL(18,2) NOT NULL, person_id BIGINT NOT NULL, PRIMARY KEY (ID))
CREATE TABLE jpaforbeginners.public.orderitems (quantity FLOAT NOT NULL, subtotal DECIMAL(18,2) NOT NULL, order_id BIGINT NOT NULL, product_id BIGINT NOT NULL, PRIMARY KEY (order_id, product_id))
CREATE TABLE jpaforbeginners.public.persons_emails (person_id BIGINT, email VARCHAR(150) NOT NULL)
CREATE TABLE jpaforbeginners.public.persons_phones (person_id BIGINT NOT NULL, number VARCHAR(20) NOT NULL, type char(1))
CREATE TABLE jpaforbeginners.public.wishlists_products (product_id BIGINT NOT NULL, wishlist_id BIGINT NOT NULL, PRIMARY KEY (product_id, wishlist_id))
ALTER TABLE jpaforbeginners.public.persons ADD CONSTRAINT FK_persons_partner_id FOREIGN KEY (partner_id) REFERENCES public.persons(ID) ON DELETE SET NULL
ALTER TABLE jpaforbeginners.public.addresses ADD CONSTRAINT FK_addresses_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.wishlists ADD CONSTRAINT FK_wishlists_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.orders ADD CONSTRAINT FK_orders_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID)
ALTER TABLE jpaforbeginners.public.orderitems ADD CONSTRAINT FK_orderitems_product_id FOREIGN KEY (product_id) REFERENCES jpaforbeginners.public.products (ID)
ALTER TABLE jpaforbeginners.public.orderitems ADD CONSTRAINT FK_orderitems_order_id FOREIGN KEY (order_id) REFERENCES jpaforbeginners.public.orders (ID)
ALTER TABLE jpaforbeginners.public.persons_emails ADD CONSTRAINT FK_persons_emails_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.persons_phones ADD CONSTRAINT FK_persons_phones_person_id FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.persons (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.wishlists_products ADD CONSTRAINT FK_wishlists_products_product_id FOREIGN KEY (product_id) REFERENCES jpaforbeginners.public.products (ID) ON DELETE CASCADE
ALTER TABLE jpaforbeginners.public.wishlists_products ADD CONSTRAINT FK_wishlists_products_wishlist_id FOREIGN KEY (wishlist_id) REFERENCES jpaforbeginners.public.wishlists (ID) ON DELETE CASCADE
CREATE SEQUENCE jpaforbeginners.public.orders_id_seq INCREMENT BY 50 START WITH 149
CREATE SEQUENCE jpaforbeginners.public.products_id_seq INCREMENT BY 50 START WITH 149
CREATE SEQUENCE jpaforbeginners.public.persons_id_seq INCREMENT BY 50 START WITH 149
CREATE SEQUENCE jpaforbeginners.public.addresses_id_seq INCREMENT BY 50 START WITH 149
CREATE SEQUENCE jpaforbeginners.public.wishlists_id_seq INCREMENT BY 50 START WITH 149
