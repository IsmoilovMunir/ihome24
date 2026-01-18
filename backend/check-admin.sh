#!/bin/bash

# Скрипт для проверки статуса администратора и тестирования входа
# Использование: ./check-admin.sh

echo "Проверка статуса администратора"
echo "================================"
echo ""

# Проверка количества пользователей
echo "1. Проверка количества пользователей в базе:"
USER_COUNT=$(curl -s http://localhost:8080/api/auth/check-users | python3 -c "import sys, json; print(json.load(sys.stdin)['totalUsers'])" 2>/dev/null || echo "0")
echo "   Всего пользователей: $USER_COUNT"
echo ""

# Тест входа
echo "2. Тестирование входа администратора:"
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"admin"}')

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "   ✓ Вход успешен!"
    USERNAME=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin)['userData']['username'])" 2>/dev/null || echo "N/A")
    EMAIL=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin)['userData']['email'])" 2>/dev/null || echo "N/A")
    ROLE=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin)['userData']['role'])" 2>/dev/null || echo "N/A")
    STATUS=$(echo "$BODY" | python3 -c "import sys, json; print(json.load(sys.stdin)['userData']['status'])" 2>/dev/null || echo "N/A")
    echo "   Username: $USERNAME"
    echo "   Email: $EMAIL"
    echo "   Role: $ROLE"
    echo "   Status: $STATUS"
else
    echo "   ✗ Ошибка входа (HTTP $HTTP_CODE):"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
fi

echo ""
echo "3. Информация:"
echo "   - Если пользователь не существует, используйте: ./register-admin.sh"
echo "   - Если пользователь уже существует, вход должен работать"
echo "   - Учетные данные по умолчанию:"
echo "     Email: admin@demo.com"
echo "     Password: admin"
