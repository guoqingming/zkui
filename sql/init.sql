create table histories (
id  bigint default null auto_increment primary key,
change_user varchar(100),
change_date datetime,
change_summary varchar(500),
change_ip varchar(500)
);