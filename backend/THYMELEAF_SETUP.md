# Настройка Thymeleaf для страницы авторизации

## Что было сделано:

1. **Добавлена зависимость Thymeleaf** в `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-thymeleaf</artifactId>
   </dependency>
   ```

2. **Создан HTML шаблон** `src/main/resources/templates/login.html`:
   - Красивый современный дизайн с градиентным фоном
   - Адаптивная верстка
   - Форма авторизации с полями username и password
   - Автоматическая обработка CSRF токена
   - Отображение ошибок авторизации
   - Информация о тестовых учетных данных

3. **Обновлен контроллер** `LoginController`:
   - Изменен с `@RestController` на `@Controller`
   - Теперь возвращает имя шаблона `"login"` вместо JSON

4. **Обновлена конфигурация Security**:
   - CSRF включен для форм авторизации (безопасность)
   - CSRF отключен для публичных API endpoints
   - Правильная обработка редиректов после авторизации

## Использование:

### Открыть страницу авторизации:
```
http://localhost:8080/api/auth/login
```

### Тестовые учетные данные:
- **Username**: `admin`
- **Password**: `admin`

### После успешной авторизации:
Автоматический редирект на `/api/admin/products` для добавления товаров.

## Особенности:

- ✅ CSRF защита включена для форм
- ✅ Красивый современный UI
- ✅ Адаптивный дизайн
- ✅ Отображение ошибок авторизации
- ✅ Информация о тестовых учетных данных прямо на странице

## Структура файлов:

```
backend/
├── src/main/
│   ├── java/com/ihome24/ihome24/
│   │   └── controller/publicapi/auth/
│   │       └── LoginController.java
│   └── resources/
│       └── templates/
│           └── login.html
└── pom.xml (добавлена зависимость Thymeleaf)
```
