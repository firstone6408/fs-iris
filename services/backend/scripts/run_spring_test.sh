#!/usr/bin/env bash
set -e

# รันทุก test
# ./services/backend/scripts/run_spring_test.sh

# รัน test เฉพาะ class
# ./services/backend/scripts/run_spring_test.sh -Dtest=AuthControllerTest

# รัน test เฉพาะ method
# ./services/backend/scripts/run_spring_test.sh -Dtest=AuthServiceImplTest#login_wrongPassword_throwsInvalidCredentialsException

cd "$(dirname "$0")/.."

mvn test "$@"
