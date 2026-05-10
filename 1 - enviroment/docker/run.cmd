@echo off

set ACTIVE_PROFILE=dev
set SERVER_PORT=8082
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=boilerplate
set DB_SCHEMA=public
set DB_USER=postgres
set DB_PWD=postgres
set SEC_KEY=49bd2f98-8c2f-4f10-bd2f-988c2f2f105f
set CORS_ORIGINS=http://localhost:3000,http://localhost:4200

docker-compose up --build -d