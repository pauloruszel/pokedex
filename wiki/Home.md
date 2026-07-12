# Pokédex Clean Architecture

Bem-vindo à Wiki do projeto **Pokédex Clean Architecture**.

Esta aplicação é uma Pokédex full stack com backend Java/Spring Boot e frontend React/Vite. O sistema consulta a PokeAPI, mantém cache local, expõe uma API própria, serve imagens pelo backend e oferece uma interface para consulta, favoritos, comparação e modo Super Trunfo.

## Links principais

- [[Funcionalidades]]
- [[Arquitetura]]
- [[Executando-o-projeto]]
- [[API-e-Swagger]]
- [[Docker-e-configuracoes]]
- [[Graphify-e-manutencao]]

## Stack

| Camada | Tecnologias |
| --- | --- |
| Backend | Java 17, Spring Boot, WebFlux, JDBC, H2, Actuator, Lombok |
| Frontend | React, TypeScript, Vite, lucide-react |
| Infra local | Docker Compose, volumes Docker, Nginx |
| Documentação | Swagger/OpenAPI, Graphify |

## Status atual

- Backend dividido em controllers, use cases, portas e adapters.
- Frontend organizado por features.
- Swagger disponível em pt-BR.
- Trunfo com modo automático e escolha manual de deck.
- Graphify usado para acompanhar acoplamento e coesão dos módulos.

