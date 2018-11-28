#!/usr/bin/env bash
psql -h localhost -U jeus -d jeus -c "DROP SCHEMA invoicing CASCADE;"
psql -U jeus -d jeus -n invoicing -1 -f invoicing_batch.sql