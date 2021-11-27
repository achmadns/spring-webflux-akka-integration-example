create table account (id int8 not null, address varchar(255), date_of_birth DATE, name varchar(255), social_number int8 not null, primary key (id));
create table account_group (id int8 not null, balance float8, name varchar(255), primary key (id));
create table transaction (id int8 not null, account_name varchar(255) not null, amount float8, code int4 not null, group_id int8 not null, group_name varchar(255) not null, social_number int8 not null, transaction_timestamp TIMESTAMP, primary key (id));
alter table account add constraint UK_pioutyxyyrbkjgnhx4e0l8qhr unique (social_number);

create sequence account_group_sequence start 1 increment 1;
create sequence transaction_sequence start 1 increment 1;
create sequence account_sequence start 1 increment 1;

ALTER TABLE account_group ALTER COLUMN id SET DEFAULT nextval('account_group_sequence');
ALTER TABLE transaction ALTER COLUMN id SET DEFAULT nextval('transaction_sequence');
ALTER TABLE account ALTER COLUMN id SET DEFAULT nextval('account_sequence');

ALTER SEQUENCE account_group_sequence OWNED BY account_group.id;
ALTER SEQUENCE transaction_sequence OWNED BY transaction.id;
ALTER SEQUENCE account_sequence OWNED BY account.id;
