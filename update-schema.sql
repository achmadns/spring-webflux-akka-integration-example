create sequence account_group_sequence start 1 increment 1;
create sequence transaction_sequence start 1 increment 1;
create sequence account_sequence start 1 increment 1;

ALTER TABLE account_group ALTER COLUMN id SET DEFAULT nextval('account_group_sequence');
ALTER TABLE transaction ALTER COLUMN id SET DEFAULT nextval('transaction_sequence');
ALTER TABLE account ALTER COLUMN id SET DEFAULT nextval('account_sequence');

ALTER SEQUENCE account_group_sequence OWNED BY account_group.id;
ALTER SEQUENCE transaction_sequence OWNED BY transaction.id;
ALTER SEQUENCE account_sequence OWNED BY account.id;


ALTER TABLE account ADD COLUMN id int8 not null;
