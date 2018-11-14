#!/usr/bin/env bash

#get backup by copy command (fast and smal)
pg_dump -h localhost -p 5432 -U jeus -n invoicing -b -v -f staging.sql

#get backup by insert command (slower and larger)
pg_dump -h localhost -p 5432 -U jeus -n invoicing --column-inserts -b -v -f staging1.sql
