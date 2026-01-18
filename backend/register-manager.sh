#!/bin/bash

# Скрипт для регистрации менеджера
# Использование: ./register-manager.sh

API_URL="http://localhost:8080/api/publicapi/auth/register-admin"

echo "Регистрация менеджера"
echo "====================="
echo ""

# Данные по умолчанию
USERNAME="${1:-manager}"
EMAIL="${2:-manager@demo.com}"
PASSWORD="${3:-manager}"
FULL_NAME="${4:-Менеджер}"

echo "Username: $USERNAME"
echo "Email: $EMAIL"
echo "Full Name: $FULL_NAME"
echo ""

# JSON данные для регистрации
JSON_DATA=$(cat <<EOF
{
  "username": "$USERNAME",
  "password": "$PASSWORD",
  "email": "$EMAIL",
  "fullName": "$FULL_NAME",
  "roleName": "manager",
  "currentPlan": "company",
  "billing": "Автоматическое списание"
}
EOF
)

# Отправка запроса
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d "$JSON_DATA")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 201 ]; then
    echo "✓ Менеджер успешно зарегистрирован!"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
else
    echo "✗ Ошибка регистрации (HTTP $HTTP_CODE):"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
fi
