# Executando o projeto

## Com Docker

Na raiz do projeto:

```bash
docker compose up --build
```

Serviços:

| Serviço | URL |
| --- | --- |
| Frontend | http://localhost:3000 |
| Backend | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |

## Backend local

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

## Frontend local

```powershell
cd frontend
npm install
npm run dev
```

Por padrão, o frontend usa:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Build e testes

Backend:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend
npm run build
```

## Reset local com Docker

Para remover banco, imagens cacheadas e dados do LibreTranslate:

```powershell
docker compose down -v
docker compose up --build
```

