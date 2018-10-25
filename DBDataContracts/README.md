

select max(cast(octet_length(column_name)*1.2 as int)/64*64+64) from table_name;