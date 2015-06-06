create table if not exists tbl_user
(
  usr_id varchar(100) not null primary key,
  usr_nm varchar(100) not null,
  passwd varchar(100) not null,
  thumbnail blob
);
