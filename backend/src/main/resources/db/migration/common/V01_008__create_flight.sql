create table flight (id bigint not null auto_increment, flight_number varchar(255) not null, airline varchar(255) not null not null, airport_departure varchar(255) not null, airport_destination varchar(255) not null, price double not null, departure_time varchar(255) not null, destination_time varchar(255) not null, primary key (id));

alter table flight add constraint unique_flight unique (flight_number);
