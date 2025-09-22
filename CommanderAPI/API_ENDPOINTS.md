# CommanderAPI Endpoints Documentation

Base URL: `http://localhost:8081/api/v1`

## Bills API

### Get All Bills
```http
GET /bills
```
Query Parameters:
- `status` (optional): Filter by bill status (OPEN, CLOSED, CANCELED)

### Get Bill by ID
```http
GET /bills/{id}
```

### Get Bill by Table ID
```http
GET /bills/table/{tableId}
```

### Get Payment Summary for Table
```http
GET /bills/table/{tableId}/payment-summary
```

### Create Bill
```http
POST /bills
```
Body:
```json
{
  "tableId": 1,
  "tableNumber": "01"
}
```

### Update Bill
```http
PUT /bills/{id}
```

### Process Table Payment
```http
POST /bills/table/{tableId}/payment
```

### Delete Bill
```http
DELETE /bills/{id}
```

## Partial Payments API

### Create Partial Payment
```http
POST /bills/table/{tableId}/partial-payment
```
Body:
```json
{
  "paidBy": "Customer Name",
  "amountInCentavos": 5000,
  "description": "Payment description",
  "paymentMethod": "CASH",
  "receivedBy": "Server Name"
}
```

### Get Partial Payments for Table
```http
GET /bills/table/{tableId}/partial-payments
```

### Get Partial Payment Details
```http
GET /bills/partial-payments/{paymentId}
```
Response:
```json
{
  "id": 1,
  "tableId": 5,
  "paidBy": "Customer Name",
  "amountInCentavos": 5000,
  "amountFormatted": "R$ 50,00",
  "description": "Payment description",
  "paymentMethod": "CASH",
  "receivedBy": "Server Name",
  "status": "ACTIVE",
  "createdAt": "2025-09-22T15:30:00"
}
```

### Cancel Partial Payment
```http
PATCH /bills/partial-payments/{paymentId}/cancel
```
Response:
```json
{
  "message": "Partial payment canceled successfully"
}
```

## Tables API

### Get All Tables
```http
GET /tables
```

### Get Table by ID
```http
GET /tables/{id}
```

### Create Table
```http
POST /tables
```

### Update Table
```http
PUT /tables/{id}
```

### Delete Table
```http
DELETE /tables/{id}
```

## Orders API

### Get All Orders
```http
GET /orders
```

### Get Order by ID
```http
GET /orders/{id}
```

### Create Order
```http
POST /orders
```

### Update Order
```http
PUT /orders/{id}
```

### Delete Order
```http
DELETE /orders/{id}
```

## Users API

### Get Users (Paginated)
```http
GET /users?page=1&size=10
```

### Get User by ID
```http
GET /users/{id}
```

### Create User
```http
POST /users
```
Body:
```json
{
  "name": "User Name"
}
```

### Update User
```http
PUT /users/{id}
```

### Delete User
```http
DELETE /users/{id}
```

## SEFAZ API

### Generate NFe
```http
POST /sefaz/generate-nfe
```

### Get NFe Status
```http
GET /sefaz/nfe/{id}/status
```

## Response Formats

### Success Response
```json
{
  "data": { ... },
  "message": "Success message"
}
```

### Error Response
```json
{
  "error": "Error description"
}
```

## Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success with no content
- `400 Bad Request` - Invalid request
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Recent Additions (2025-09-22)

### New Partial Payment Endpoints
- `GET /bills/partial-payments/{paymentId}` - Get partial payment details
- `PATCH /bills/partial-payments/{paymentId}/cancel` - Cancel partial payment

These endpoints support the new partial payment details and cancellation functionality in the mobile app.