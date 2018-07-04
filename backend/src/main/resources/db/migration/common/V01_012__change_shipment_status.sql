update shipment set status = 'UNDEFINED' where status is null;
alter table shipment modify column status varchar(255) not null;