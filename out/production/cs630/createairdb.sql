--
-- 	Database Table Creation for hw4 database
-- use modified tablenames to avoid collision with old queries
-- use timestamp for time so we can order by time, etc.
-- add is_processed to flights for second version
-- flights departs: timestamp 2005-04-12 8:15:00 for example
create table flights1(
	flno int primary key,
	origin varchar(20) not null,
	destination varchar(20) not null,
	distance int,
	departs timestamp,
	arrives timestamp,
	price decimal(7,2),
	is_processed int
	);
create table aircraft1(
	aid int primary key,
	aname varchar(30) not null unique,
	cruisingrange int
	);
create table employees1(
	eid int primary key,
	ename varchar(30) not null,
	salary decimal(10,2)
	);

-- using named constaints for better error messages
create table certified1(
	eid int,
	aid int,
	primary key(eid,aid),
	foreign key(eid) references employees1(eid),
	foreign key(aid) references aircraft1(aid)
	);
create table start_location(
	eid int,
	city varchar(20) not null,
	constraint start_location_pk primary key(eid)
	);


create table flight_assignments(
        flno int,
        aid int,
        eid int,
	constraint flight_assignment_pk primary key(flno)
        );

create table delayed_flights(
        flno int,
	constraint delayed_flights_pk primary key(flno)
        );

create table new_location(
        eid int,
        city varchar(20) not null,
        arrival_time timestamp,
	constraint new_location_pk primary key(eid)
        );


insert into flights1 values (99,'Los Angeles','Washington D.C.',2308,timestamp '2005-04-12 09:30:00',timestamp '2005-04-12 21:40:00',235.98,0);
insert into flights1 values (13,'Los Angeles','Chicago',1749,timestamp '2005-04-12 08:45:00',timestamp '2005-04-12 20:45:00',220.98,0);
insert into flights1 values (346,'Los Angeles','Dallas',1251,timestamp '2005-04-12 11:50:00',timestamp '2005-04-12 19:05:00',225.43,0);
insert into flights1 values (387,'Los Angeles','Boston',2606,timestamp '2005-04-12 07:03:00',timestamp '2005-04-12 17:03:00',261.56,0);
insert into flights1 values (7,'Los Angeles','Sydney',7487,timestamp '2005-04-12 22:30:00',timestamp '2005-04-14 6:10:00',1278.56,0);
insert into flights1 values (2,'Los Angeles','Tokyo',5478,timestamp '2005-04-12 12:30:00',timestamp '2005-04-13 15:55:00',780.99,0);
insert into flights1 values (33,'Los Angeles','Honolulu',2551,timestamp '2005-04-12 09:15:00',timestamp '2005-04-12 11:15:00',375.23,0);
insert into flights1 values (34,'Los Angeles','Honolulu',2551,timestamp '2005-04-12 12:45:00',timestamp '2005-04-12 15:18:00',425.98,0);
insert into flights1 values (76,'Chicago','Los Angeles',1749,timestamp '2005-04-12 08:32:00',timestamp '2005-04-12 10:03:00',220.98,0);
insert into flights1 values (68,'Chicago','New York',802,timestamp '2005-04-12 09:00:00',timestamp '2005-04-12 12:02:00',202.45,0);
insert into flights1 values (7789,'Madison','Detroit',319,timestamp '2005-04-12 06:15:00',timestamp '2005-04-12 08:19:00',120.33,0);
insert into flights1 values (701,'Detroit','New York',470,timestamp '2005-04-12 08:55:00',timestamp '2005-04-12 10:26:00',180.56,0);
insert into flights1 values (702,'Madison','New York',789,timestamp '2005-04-12 07:05:00',timestamp '2005-04-12 10:12:00',202.34,0);
insert into flights1 values (4884,'Madison','Chicago',84,timestamp '2005-04-12 22:12:00',timestamp '2005-04-12 23:02:00',112.45,0);
insert into flights1 values (2223,'Madison','Pittsburgh',517,timestamp '2005-04-12 08:02:00',timestamp '2005-04-12 10:01:00',189.98,0);
insert into flights1 values (5694,'Madison','Minneapolis',247,timestamp '2005-04-12 08:32:00',timestamp '2005-04-12 09:33:00',120.11,0);
insert into flights1 values (304,'Minneapolis','New York',991,timestamp '2005-04-12 10:00:00',timestamp '2005-04-12 1:39:00',101.56,0);
insert into flights1 values (149,'Pittsburgh','New York',303,timestamp '2005-04-12 09:42:00',timestamp '2005-04-12 12:09:00',116.50,0);
insert into aircraft1 values (1,'Boeing 747-400',8430);
insert into aircraft1 values (2,'Boeing 737-800',3383);
insert into aircraft1 values (3,'Airbus A340-300',7120);
insert into aircraft1 values (4,'British Aerospace Jetstream 41',1502);
insert into aircraft1 values (5,'Embraer ERJ-145',1530);
insert into aircraft1 values (6,'SAAB 340',2128);
insert into aircraft1 values (7,'Piper Archer III',520);
insert into aircraft1 values (8,'Tupolev 154',4103);
insert into aircraft1 values (16,'Schwitzer 2-33',30);
insert into aircraft1 values (9,'Lockheed L1011',6900);
insert into aircraft1 values (10,'Boeing 757-300',4010);
insert into aircraft1 values (11,'Boeing 777-300',6441);
insert into aircraft1 values (12,'Boeing 767-400ER',6475);
insert into aircraft1 values (13,'Airbus A320',2605);
insert into aircraft1 values (14,'Airbus A319',1805);
insert into aircraft1 values (15,'Boeing 727',1504);

insert into employees1 values (242518965,'James Smith',20433);
insert into employees1 values (141582651,'Mary Johnson',178345);
insert into employees1 values (011564812,'John Williams',153972);
insert into employees1 values (567354612,'Lisa Walker',256481);
insert into employees1 values (552455318,'Larry West',101745);
insert into employees1 values (550156548,'Karen Scott',205187);
insert into employees1 values (390487451,'Lawrence Sperry',212156);
insert into employees1 values (274878974,'Michael Miller',99890);
insert into employees1 values (254099823,'Patricia Jones',24450);
insert into employees1 values (356187925,'Robert Brown',44740);
insert into employees1 values (355548984,'Angela Martinez',212156 );
insert into employees1 values (310454876,'Joseph Thompson',212156);
insert into employees1 values (489456522,'Linda Davis',27984);
insert into employees1 values (489221823,'Richard Jackson',23980);
insert into employees1 values (548977562,'William Ward',84476);
insert into employees1 values (310454877,'Chad Stewart',33546);
insert into employees1 values (142519864,'Betty Adams',227489);
insert into employees1 values (269734834,'George Wright',289950);
insert into employees1 values (287321212,'Michael Miller',48090);
insert into employees1 values (552455348,'Dorthy Lewis',152013);
insert into employees1 values (248965255,'Barbara Wilson',43723);
insert into employees1 values (159542516,'William Moore',48250);
insert into employees1 values (348121549,'Haywood Kelly',32899);
insert into employees1 values (090873519,'Elizabeth Taylor',32021);
insert into employees1 values (486512566,'David Anderson',43001);
insert into employees1 values (619023588,'Jennifer Thomas',54921);
insert into employees1 values (015645489,'Donald King',18050);
insert into employees1 values (556784565,'Mark Young',205187);
insert into employees1 values (573284895,'Eric Cooper',114323);
insert into employees1 values (574489456,'William Jones',105743);
insert into employees1 values (574489457,'Milo Brooks',20);
insert into certified1 values (567354612,2);
insert into certified1 values (567354612,10);
insert into certified1 values (567354612,11);
insert into certified1 values (567354612,12);
insert into certified1 values (567354612,15);
insert into certified1 values (567354612,7);
insert into certified1 values (567354612,9);
insert into certified1 values (567354612,3);
insert into certified1 values (567354612,4);
insert into certified1 values (567354612,5);
insert into certified1 values (552455318,2);
insert into certified1 values (552455318,14);
insert into certified1 values (550156548,1);
insert into certified1 values (550156548,12);
insert into certified1 values (390487451,3);
insert into certified1 values (390487451,13);
insert into certified1 values (390487451,14);
insert into certified1 values (274878974,10);
insert into certified1 values (274878974,12);
insert into certified1 values (355548984,8);
insert into certified1 values (355548984,9);
insert into certified1 values (310454876,8);
insert into certified1 values (310454876,9);
insert into certified1 values (548977562,7);
insert into certified1 values (142519864,1);
insert into certified1 values (142519864,11);
insert into certified1 values (142519864,12);
insert into certified1 values (142519864,10);
insert into certified1 values (142519864,3);
insert into certified1 values (142519864,2);
insert into certified1 values (142519864,13);
insert into certified1 values (142519864,7);
insert into certified1 values (269734834,1);
insert into certified1 values (269734834,2);
insert into certified1 values (269734834,3);
insert into certified1 values (269734834,4);
insert into certified1 values (269734834,5);
insert into certified1 values (269734834,6);
insert into certified1 values (269734834,7);
insert into certified1 values (269734834,8);
insert into certified1 values (269734834,9);
insert into certified1 values (269734834,10);
insert into certified1 values (269734834,11);
insert into certified1 values (269734834,12);
insert into certified1 values (269734834,13);
insert into certified1 values (269734834,14);
insert into certified1 values (269734834,15);
insert into certified1 values (552455318,7);
insert into certified1 values (556784565,5);
insert into certified1 values (556784565,2);
insert into certified1 values (556784565,3);
insert into certified1 values (573284895,3);
insert into certified1 values (573284895,4);
insert into certified1 values (573284895,5);
insert into certified1 values (574489456,8);
insert into certified1 values (574489456,6);
insert into certified1 values (574489457,7);
insert into certified1 values (242518965,2);
insert into certified1 values (242518965,10);
insert into certified1 values (141582651,2);
insert into certified1 values (141582651,10);
insert into certified1 values (141582651,12);
insert into certified1 values (011564812,2);
insert into certified1 values (011564812,10);
insert into certified1 values (356187925,6);
insert into certified1 values (159542516,5);
insert into certified1 values (159542516,7);
insert into certified1 values (090873519,6);
insert into start_location values (242518965,'Los Angeles');
insert into start_location values (141582651,'Chicago');
insert into start_location values (011564812,'Los Angeles');
insert into start_location values (567354612,'Chicago');
insert into start_location values (552455318,'Los Angeles');
insert into start_location values (550156548,'Chicago');
insert into start_location values (390487451,'Los Angeles');
insert into start_location values (274878974,'Madison');
insert into start_location values (254099823,'Los Angeles');
insert into start_location values (356187925,'Madison');
insert into start_location values (355548984,'Los Angeles');
insert into start_location values (310454876,'Madison');
insert into start_location values (489456522,'Los Angeles');
insert into start_location values (489221823,'Madison');
insert into start_location values (548977562,'Los Angeles');
insert into start_location values (310454877,'Madison');
insert into start_location values (142519864,'Los Angeles');
insert into start_location values (269734834,'Madison');
insert into start_location values (287321212,'Los Angeles');
insert into start_location values (552455348,'Madison');
insert into start_location values (248965255,'Los Angeles');
insert into start_location values (159542516,'Madison');
insert into start_location values (348121549,'Los Angeles');
insert into start_location values (090873519,'Madison');
insert into start_location values (486512566,'Detroit');
insert into start_location values (619023588,'Madison');
insert into start_location values (015645489,'Detroit');
insert into start_location values (556784565,'Minneapolis');
insert into start_location values (573284895,'Boston');
insert into start_location values (574489456,'San Francisco');
insert into start_location values (574489457,'Minneapolis');
 exit;