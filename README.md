# Ecommerce Platform

## Overview
This monorepo contains three Spring Boot services: **Backend** (core API and data layer), **Gateway** (edge routing), and **FrontEnd** (Thymeleaf UI), all targeting **Java 24** and packaged with **Maven**.

The backend seeds an **admin user** (`e@mail.com` / password: `s`), product categories, and two sample laptops at startup so the application has meaningful demo data out of the box.

- **Authentication**: Endpoints issue HTTP-only JWT cookies.  
- **Security**: Stateless filter chain protects user/order routes.  
- **Catalog APIs**: Comprehensive product endpoints for search, carts, reviews, and CRUD.  
- **Checkout**: Redis Lua scripts ensure consistent stock holds, reservations, and order placement.  
- **Gateway/UI**: The gateway forwards browser traffic, while the front-end renders Tailwind/Swiper-based templates and dynamic search/carts.  

---

## Repository structure

```
Backend/   – Spring Boot 3.4.3 service with JPA, Redis, Security/OAuth, JWT, MapStruct, tests, and Docker Compose helpers for Postgres/Redis
Gateway/   – Spring Cloud Gateway config routing UI (http://localhost:8082) and API (http://localhost:8081)
FrontEnd/  – Spring Boot 3.4.5 web app using Thymeleaf templates, static assets, and security resource server
```

---

## Backend (Ecommerce)

### Key features
- **Data seeding** – hierarchical categories, product lines, media, specs, and an admin user.  
- **Auth endpoints** – `/api/auth/register`, `/authenticate`, `/sign-out`.  
- **Security** – JWT-based stateless filter chain, role-based auth, and cookie validation.  
- **Catalog APIs** – search/filter/sort, cart snapshots, review slices, and admin CRUD.  
- **Checkout & reservation** – Redis Lua scripts manage stock holds & releases.  

### Configuration & environment
- Runs on **port 8081**, expects gateway at [http://localhost:8080](http://localhost:8080).  
- Preconfigures: Redis host/port, Hibernate `create-drop`, multipart limits, batch inserts, and RSA key locations.  
- **Docker Compose**: Postgres & Redis services (pgAdmin optional, commented).  
- RSA keys bound via properties; JWK `kid` auto-derived.  


---

## Gateway

- **Framework**: Spring Cloud Gateway.  
- Default **port 8080**, property `url.front-end` for redirects.

---

## FrontEnd

### Highlights
- Controllers map **user/admin/home/product** routes → Thymeleaf templates.  
- `GlobalModelAttributes` injects gateway media/API URLs into every view.  
- Security config consumes gateway JWKS endpoint & handles Auth cookie.  
- Tailwind + Swiper power UI layouts.  
- JavaScript search page syncs filters, history, and cart via gateway APIs.  

### Config & Run
- Port **8082**, reads gateway base URL from config.

---

## Development tips
- Replace bundled **RSA keys** for production.  
- Switch JPA ddl-auto from `create-drop` → `update` / `validate` before persistent DB use.  
- Use seeded admin (`e@mail.com` / `s`) for admin dashboards.  
- Expose gateway port (default at 8080) only so all external will have to pass through gateway.
---

## Future Update
- Add payment
- Extend reservation for user in front-end
- Add order processing UI for admin
- Add product category management for admin
- QoS for user to see error message from backend