create table nemoulous.strategy (
    sid serial not null primary key,
    id uuid not null unique,
    name varchar(255) not null
);

create table nemoulous.model (
    sid serial not null primary key,
    id uuid not null unique,
    name varchar(255) not null
);

create table nemoulous.strategy_model (
    sid serial not null primary key,
    id uuid not null unique,
    strategy_id uuid not null references nemoulous.strategy(id),
    model_id uuid not null references nemoulous.model(id)
);

create table nemoulous.security_master (
    sid serial not null primary key,
    id uuid not null unique,
    name varchar(32)
);


create table nemoulous.strategy_model_tradable (
    sid serial not null primary key,
    strategy_model_id uuid not null references nemoulous.strategy_model(id),
    security uuid not null references nemoulous.security_master(id)
);
