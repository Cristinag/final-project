create table BUILDS (
project varchar unique,
tests_processed boolean,
changes ARRAY,
primary key (project));

CREATE SEQUENCE line_seq INCREMENT 1 START 2; 

CREATE TABLE LINES
(lineid int NOT NULL DEFAULT nextval('line_seq') PRIMARY KEY,
project varchar not null,
lineno int,
line varchar,
file varchar);

create table CHANGES (
project varchar not null,
test varchar not null,
lineid int not null,
count int, primary key(project, test, lineid));

create table tests(
project varchar not null,
test varchar not null,
runs int default 0,
failures int default 0,
primary key(project, test));

insert into builds (project, tests_processed,changes) values ('project1', false, ());
insert into builds (project, tests_processed,changes) values ('project2', true, (1,2));
insert into builds (project, tests_processed,changes) values ('project3', true, (1,2));
insert into tests (project, test, runs, failures) values ('project2', '1', 1,0);
insert into tests (project, test, runs, failures) values ('project2', '2', 1,0);
insert into tests (project, test, runs, failures) values ('project2', '3', 1,0);
insert into tests (project, test, runs, failures) values ('project2', '4', 1,0);
insert into tests (project, test, runs, failures) values ('project3', 'test5', 4,3);
insert into changes (project, test, lineid, count) values('project3', 'test5', 1, 4);
insert into lines (lineid, project, lineno, line, file) values (1,'project3',1,'line test', 'file');
