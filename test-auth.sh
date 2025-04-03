#!/bin/bash

echo "Testing JWT Authentication and Authorization in Flight System API"
echo "----------------------------------------------------------"

# Test login with admin user
echo -e "\n1. Testing admin login:"
ADMIN_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}')

echo "Response: $ADMIN_RESPONSE"
ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to extract admin token. Check if the server is running and the login endpoint is working."
  exit 1
fi

echo "Admin token received: ${ADMIN_TOKEN:0:20}... (truncated)"

# Test login with agency user
echo -e "\n2. Testing agency login:"
AGENCY_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "agency", "password": "agency123"}')

echo "Response: $AGENCY_RESPONSE"
AGENCY_TOKEN=$(echo $AGENCY_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$AGENCY_TOKEN" ]; then
  echo "Failed to extract agency token. Check if the server is running and the login endpoint is working."
  exit 1
fi

echo "Agency token received: ${AGENCY_TOKEN:0:20}... (truncated)"

# Test accessing a protected endpoint with admin token
echo -e "\n3. Testing admin access to a protected endpoint (routes):"
ROUTES_RESPONSE=$(curl -s -X GET http://localhost:8080/routes \
  -H "Authorization: Bearer $ADMIN_TOKEN")

echo "Routes endpoint response with admin token: $ROUTES_RESPONSE"

# Test agency GET access to locations (should now SUCCEED)
echo -e "\n4. Testing agency GET access to locations (should SUCCEED):"
LOCATIONS_GET_RESPONSE=$(curl -s -X GET http://localhost:8080/locations \
  -H "Authorization: Bearer $AGENCY_TOKEN")

echo "GET /locations response with agency token: $LOCATIONS_GET_RESPONSE"

# Test agency POST access to locations (should FAIL)
echo -e "\n5. Testing agency POST access to locations (should FAIL):"
LOCATIONS_POST_RESPONSE=$(curl -s -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $AGENCY_TOKEN" \
  -d '{"locationCode": "TST", "name": "Test Location", "city": "Test City", "country": "Test Country"}')

echo "POST /locations response with agency token: $LOCATIONS_POST_RESPONSE"

# Test agency PUT access to locations (should FAIL)
echo -e "\n6. Testing agency PUT access to locations (should FAIL):"
LOCATIONS_PUT_RESPONSE=$(curl -s -X PUT http://localhost:8080/locations/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $AGENCY_TOKEN" \
  -d '{"locationCode": "TST", "name": "Updated Test Location", "city": "Test City", "country": "Test Country"}')

echo "PUT /locations response with agency token: $LOCATIONS_PUT_RESPONSE"

# Test agency DELETE access to locations (should FAIL)
echo -e "\n7. Testing agency DELETE access to locations (should FAIL):"
LOCATIONS_DELETE_RESPONSE=$(curl -s -X DELETE http://localhost:8080/locations/1 \
  -H "Authorization: Bearer $AGENCY_TOKEN")

echo "DELETE /locations response with agency token: $LOCATIONS_DELETE_RESPONSE"

# Test admin access to transportations (should SUCCEED)
echo -e "\n8. Testing admin access to transportations (should SUCCEED):"
TRANSPORTATIONS_RESPONSE=$(curl -s -X GET http://localhost:8080/transportations \
  -H "Authorization: Bearer $ADMIN_TOKEN")

echo "GET /transportations response with admin token: $TRANSPORTATIONS_RESPONSE"

# Test agency access to transportations (should FAIL)
echo -e "\n9. Testing agency access to transportations (should FAIL):"
TRANSPORTATIONS_AGENCY_RESPONSE=$(curl -s -X GET http://localhost:8080/transportations \
  -H "Authorization: Bearer $AGENCY_TOKEN")

echo "GET /transportations response with agency token: $TRANSPORTATIONS_AGENCY_RESPONSE"

echo -e "\nTest completed!"
