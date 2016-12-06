create or replace procedure assign_pilot (param_flno int, param_distance int, param_origin char) as
  is_found_rec boolean := false;
CURSOR flightAssignCursor IS SELECT param_flno, A.aid, E.eid
                                    FROM aircraft1 A, certified1 E, start_location sl
                                    WHERE (E.aid = A.aid and sl.eid = e.eid) AND A.cruisingrange > param_distance
                                          and E.eid not in (select fa.eid from flight_assignments fa)  and sl.city = param_origin and rownum =1
                                          order by A.CRUISINGRANGE;
begin
FOR flightRecord IN flightAssignCursor
  LOOP
    is_found_rec := true;
    insert into pavithra.FLIGHT_ASSIGNMENTS values(flightRecord.param_flno,flightRecord.aid,flightRecord.eid);
  END LOOP;
if not is_found_rec then
  INSERT INTO PAVITHRA.delayed_flights (flno) VALUES (param_flno);
END IF;

    --dbms_output.put_line('hi from SP assign_pilot, param_flno = '||param_flno);
-- Note: You don't need to *handle* exceptions here
-- Just let them throw up to the Java program
COMMIT;
end;
/
show errors;
exit;