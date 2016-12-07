select * from flight_assignments order by flno;

select * from delayed_flights;

SET SERVEROUTPUT ON;

DECLARE
CURSOR c1 IS select  flight_assignments.FLNO,FLIGHTS1.ORIGIN ,AID ,EID,'n' as is_Delayed  from flight_assignments ,FLIGHTS1 where FLIGHTS1.FLNO = flight_assignments.FLNO
union
select delayed_flights.FLNO, flights1.ORIGIN,null,null, 'y' as is_Delayed from delayed_flights ,flights1 where flights1.FLNO = delayed_flights.FLNO
order by FLNO;
c1_rec c1%ROWTYPE;

BEGIN
dbms_output.put_line( 'FLNO' ||'   '|| 'ORIGIN'||'     '||'AID'||'   '||'EID');
  FOR c1_rec IN c1
  LOOP
  if c1_rec.is_Delayed = 'n' Then
    dbms_output.put_line( c1_rec.FLNO ||'   '|| c1_rec.ORIGIN||'     '||c1_rec.AID||'   '||c1_rec.EID);
  ELSE
        dbms_output.put_line( c1_rec.FLNO ||'   '|| c1_rec.ORIGIN||'     '||'(delayed)');

    END IF;

  END LOOP;
END;
/
exit;