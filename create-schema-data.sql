create table account (id int8 not null, address varchar(255), date_of_birth DATE, name varchar(255), social_number int8 not null, primary key (id));
create table account_group (id int8 not null, balance float8, name varchar(255), primary key (id));
create table transaction (id int8 not null, account_name varchar(255) not null, amount float8, code int4 not null, group_id int8 not null, group_name varchar(255) not null, social_number int8 not null, transaction_timestamp TIMESTAMP, primary key (id));
alter table account add constraint UK_pioutyxyyrbkjgnhx4e0l8qhr unique (social_number);
