-- assign_pilot2 procedure (can't be function since it changes the DB)
create or replace procedure assign_pilot2  as
CURSOR findFlightsCursor is SELECT *  FROM flights1  ORDER BY departs;
begin
FOR findFlightRecord IN findFlightsCursor
  LOOP
    Exec assign_pilot  @findFlightRecord.flno, @findFlightRecord.distance, @findFlightRecord.origin;
  END LOOP;


--dbms_output.put_line('hi from SP assign_pilot2');
end;
/
show errors;

