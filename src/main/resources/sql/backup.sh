#!/usr/bin/env bash
pg_dump -h localhost -p 5432 -U jeus -n invoicing -b -v -f staging.sql

