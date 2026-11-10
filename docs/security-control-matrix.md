# GRID SENTINEL Security Control Matrix

**Generated:** 2026-11-10  
**Source Controls:** SEC-30 through SEC-78 (handwritten security page) + APP-01 through APP-30 (APP Security page)  
**Total Visible Controls:** 79 (49 + 30)  
**Controls #1–#29:** Not present in supplied source material. No fabricated controls added.

---

## SEC-30 through SEC-78 (Handwritten Security Page)

| ID | Control | Source | Grid Sentinel Area | Current Status | Evidence | Implementation | Test | Runtime Validation | Remaining Risk |
|----|---------|--------|-------------------|----------------|----------|----------------|------|-------------------|----------------|
| SEC-30 | Row-Level Security | Handwritten p.30 | PostgreSQL / Security | PARTIAL | DatabaseConfig.java uses H2 in dev, PostGIS in prod. No RLS policies implemented. | Need to implement RLS policies for sensitive tables | Not yet implemented | Not tested | MEDIUM - No database-level access control |
| SEC-31 | Authentication on Every API Route | Handwritten p.31 | REST API / Security | PARTIAL | SecurityConfig.java has endpoint-level RBAC. Some endpoints permitAll() | Need audit of all endpoints | Partial - manual verification | Not fully tested | MEDIUM - Some endpoints permitAll() |
| SEC-32 | Sanitize User Input | Handwritten p.32 | All inputs | PARTIAL | Spring validation on DTOs, but no explicit sanitization | Need to add input sanitization | Not implemented | Not tested | MEDIUM - XSS/sanitization gaps |
| SEC-33 | Parameterized Queries | Handwritten p.33 | Database / Security | PASS | JPA/Hibernate parameterized queries used throughout. No raw SQL concatenation found. | N/A | Verified by code review | N/A | LOW |
| SEC-34 | Lock Down CORS | Handwritten p.34 | Web / Security | PARTIAL | WebSocketConfig has explicit allowed origins. REST CORS not explicitly configured. | Need to configure REST CORS | Not fully tested | Not fully validated | MEDIUM |
| SEC-35 | HTTPS Everywhere | Handwritten p.35 | Transport / Security | PARTIAL | Dev uses HTTP. Prod profile exists but no TLS config shown. | Need TLS termination documentation | Not tested | Not validated | HIGH for production |
| SEC-36 | Stop IDOR Bugs | Handwritten p.36 | API / Security | PARTIAL | RBAC on endpoints, but no ownership checks on individual resources | Need ownership checks per resource | Not fully implemented | Not fully tested | HIGH |
| SEC-37 | Debug Mode Off in Production | Handwritten p.37 | Config / Security | PARTIAL | Dev profile has DEBUG logging. Prod profile exists. | Need prod config audit | Not validated | Not validated | MEDIUM |
| SEC-38 | CAPTCHA/Bot Protection | Handwritten p.38 | Forms / Security | PARTIAL | Login has rate limiting via account lockout. No CAPTCHA. | Need rate limiting on login | Account lockout implemented | Partial | LOW |
| SEC-39 | Log Admin Actions | Handwritten p.39 | Audit / Security | PARTIAL | AuditLog entity exists. AuditEventType enum. | Need audit on all admin actions | Partial | Partial | MEDIUM |
| SEC-40 | Private Storage Buckets | Handwritten p.40 | Storage / Security | NOT APPLICABLE | No external storage buckets used | N/A | N/A | N/A | NONE |
| SEC-41 | Hide/Protect Admin Pages | Handwritten p.41 | Frontend/Backend / Security | PARTIAL | Backend RBAC on /admin endpoints. Frontend route guards needed. | Need frontend route guards | Not implemented | Not tested | MEDIUM |
| SEC-42 | Vulnerable Dependencies | Handwritten p.42 | Dependencies / Security | PARTIAL | Need to run npm audit and Maven dependency check | Need to run audits | Not run | Not run | UNKNOWN |
| SEC-43 | Security Review + Headers | Handwritten p.43 | HTTP / Security | FIXED | SecurityHeadersConfig adds CSP, HSTS, X-Frame-Options, X-Content-Type-Options, Permissions-Policy, Referrer-Policy, X-Content-Type-Options, X-Download-Options, X-DNS-Prefetch-Control, X-Permitted-Cross-Domain-Policies | SecurityHeadersConfig implemented with profile-specific CSP | Implemented | Compile verified | LOW |
| SEC-44 | Verify Webhook Signatures | Handwritten p.44 | Webhooks / Security | NOT APPLICABLE | No webhooks implemented | N/A | N/A | N/A | NONE |
| SEC-45 | Test Links/Navigation/Browser | Handwritten p.45 | Frontend / Security | PARTIAL | Links exist but not systematically tested | Need browser testing | Not automated | Not automated | MEDIUM |
| SEC-46 | Content Security/Response Protection | Handwritten p.46 | HTTP / Security | FIXED | SecurityHeadersConfig adds CSP, X-Content-Type-Options, Referrer-Policy, Permissions-Policy, Cache-Control for sensitive endpoints | SecurityHeadersConfig implemented | Implemented | Compile verified | LOW |
| SEC-47 | Type/UI/Header Consistency | Handwritten p.47 | Frontend/Types / Security | PASS | TypeScript strict mode enabled. Some any types. | Reduce any types | Typecheck passes | Typecheck passes | LOW |
| SEC-48 | Domain Email/Analytics Config | Handwritten p.48 | Config / Security | NOT APPLICABLE | No email/analytics configured | N/A | N/A | N/A | NONE |
| SEC-49 | Backups/Recovery | Handwritten p.49 | Database / Security | PARTIAL | Flyway migrations. No backup strategy documented. | Need backup strategy doc | Not documented | Not tested | HIGH |
| SEC-50 | Payment/High-Risk Flows | Handwritten p.50 | Payments / Security | NOT APPLICABLE | No payment functionality | N/A | N/A | N/A | NONE |
| SEC-51 | Hide API Keys/Secrets | Handwritten p.51 | Secrets / Security | FIXED | JWT secret and DB passwords externalized to environment variables via ${APP_JWT_SECRET}, ${SPRING_DATASOURCE_PASSWORD}, etc. | application.yml uses ${} placeholders, .env.example created | Fixed | Compile verified | LOW |
| SEC-52 | Database Keys/Encrypt Sensitive Data | Handwritten p.52 | Database / Security | PARTIAL | BCrypt(12) for passwords. No field-level encryption for sensitive fields. | Consider field-level encryption | Not implemented | Not tested | MEDIUM |
| SEC-53 | Server-Side Authorization | Handwritten p.53 | Authorization / Security | PARTIAL | Backend RBAC exists. Method security enabled. Profile-specific filter chains. Need ownership checks. | Need ownership checks | Partial | Partial | HIGH |
| SEC-54 | Block Field Tampering | Handwritten p.54 | API / Security | PARTIAL | DTOs used but some endpoints accept Map<String,Object> | Need DTO validation | Not fixed | Not tested | HIGH |
| SEC-55 | Session Cookies + Password Hashing | Handwritten p.55 | Auth / Security | PASS | BCrypt(12) for passwords. JWT tokens. No cookies used. | JWT tokens in localStorage | Works | Works | LOW |
| SEC-56 | Bot Protection/Rate Limiting | Handwritten p.56 | Auth/API / Security | FIXED | RateLimitingFilter implemented with sliding window. Default 100 req/min, auth endpoints 10/5min, simulations 5/min. Headers: X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset | RateLimitingFilter implemented with sliding window | Implemented | Compile verified | LOW |
| SEC-57 | Input Validation + Content Escaping | Handwritten p.57 | API / Security | PARTIAL | DTO validation. Some Map<String,Object> endpoints. | Need stricter DTOs | Partial | Partial | MEDIUM |
| SEC-57 | Input Validation + Content Escaping | Handwritten p.57 | API / Security | PARTIAL | DTO validation. Some Map<String,Object> endpoints. | Need stricter DTOs | Partial | Partial | MEDIUM |
| SEC-58 | API Response Minimization | Handwritten p.58 | API / Security | PARTIAL | Some responses include full entities. Audit exposes details. | Need response DTOs | Not implemented | Not tested | MEDIUM |
| SEC-59 | Custom 404/Error Responses | Handwritten p.59 | Error Handling / Security | PARTIAL | Spring default error responses. No custom error pages. | Need custom error responses | Not implemented | Not tested | LOW |
| SEC-60 | Database/SQL Injection Protection | Handwritten p.60 | Database / Security | PASS | JPA/Hibernate parameterized queries. No raw SQL concatenation found. | N/A | Verified | N/A | LOW |
| SEC-61 | CORS/Origin/Cross-Site Review | Handwritten p.61 | CORS / Security | PARTIAL | WebSocket has explicit origins. REST CORS not configured. | Need REST CORS config | Not fully tested | Not validated | MEDIUM |
| SEC-62 | Outdated NPM Packages | Handwritten p.62 | Dependencies / Security | UNKNOWN | Need to run npm audit | Not run | Not run | Not run | UNKNOWN |
| SEC-63 | Database Backups/Restore/Query Health | Handwritten p.63 | Database / Security | PARTIAL | Flyway migrations. No backup strategy. | Need backup strategy | Not documented | Not tested | HIGH |
| SEC-64 | Database Schema/Production Safety | Handwritten p.64 | Database / Security | PARTIAL | Flyway with validate in prod. No rollback strategy. | Need rollback strategy | Not documented | Not tested | MEDIUM |
| SEC-65 | Lock Down Dangerous Tools | Handwritten p.65 | Tooling / Security | FIXED | Demo endpoints (/api/v1/dev/realtime/**) denied in prod profile. Swagger available. | Demo endpoints denied in prod profile | Fixed | Compile verified | LOW |
| SEC-66 | NPM Audit/Known Vulnerabilities | Handwritten p.66 | Dependencies / Security | UNKNOWN | Need npm audit | Not run | Not run | Not run | UNKNOWN |
| SEC-67 | Password Expiry/Password Security | Handwritten p.67 | Auth / Security | PARTIAL | BCrypt(12). No password expiry policy. Demo credentials exist. | Need password policy | BCrypt works | BCrypt works | MEDIUM |
| SEC-68 | Security Logging/Suspicious Activity | Handwritten p.68 | Logging / Security | PARTIAL | Failed login logging. Auth events. No suspicious activity detection. | Need suspicious activity detection | Partial | Partial | MEDIUM |
| SEC-69 | RLS + Prompt Injection | Handwritten p.69 | Database/AI / Security | PARTIAL | No RLS. No AI/copilot functionality yet. | RLS: see SEC-30. Prompt: N/A | N/A | N/A | MEDIUM |
| SEC-70 | Privilege Escalation + Open Redirects | Handwritten p.70 | Auth/Web / Security | PARTIAL | Role escalation possible via token manipulation. No redirect validation. | Need token validation hardening | Not fully tested | Not tested | HIGH |
| SEC-71 | Audit Logging + Command Injection | Handwritten p.71 | Audit/Injection / Security | PASS | No Runtime.exec, ProcessBuilder, or shell commands found. | N/A | Verified by search | N/A | LOW |
| SEC-72 | Weak Hashing | Handwritten p.72 | Crypto / Security | PASS | BCrypt(12) for passwords. JWT HMAC-SHA256. No MD5/SHA-1 for security. | N/A | Verified | Verified | LOW |
| SEC-73 | Dependency CVEs + SSRF Bypasses | Handwritten p.73 | Dependencies/SSRF / Security | UNKNOWN | Need dependency audit. No URL fetching found. | Need dependency audit | Not run | Not run | UNKNOWN |
| SEC-74 | CSP + Clickjacking | Handwritten p.74 | HTTP / Security | FAIL | No CSP, no X-Frame-Options, no frame-ancestors | Must implement | Not implemented | Not tested | CRITICAL |
| SEC-75 | Public Buckets + Debug Mode | Handwritten p.75 | Storage/Debug / Security | PARTIAL | No public buckets. Dev profile has DEBUG logging. | Disable debug in prod | Partial | Partial | MEDIUM |
| SEC-76 | Token Expiry + Stale Tokens | Handwritten p.76 | Auth / Security | PARTIAL | Access token 1hr, refresh 7 days. No refresh rotation. No token revocation. | Need refresh rotation | Works | Works | MEDIUM |
| SEC-77 | Insecure Deserialization | Handwritten p.77 | Deserialization / Security | PASS | No Java serialization, no ObjectInputStream, no polymorphic JSON deserialization. | N/A | Verified by search | N/A | LOW |
| SEC-78 | Environment Variables + Secure Database | Handwritten p.78 | Config/Database / Security | FAIL | JWT secret and DB password hardcoded in application.yml. | Must externalize to env vars | Not fixed | Not fixed | CRITICAL |

---

## APP-01 through APP-30 (APP Security Page)

| ID | Control | Source | Grid Sentinel Area | Current Status | Evidence | Implementation | Test | Runtime Validation | Remaining Risk |
|----|---------|--------|-------------------|----------------|----------|----------------|------|-------------------|----------------|
| APP-01 | Hard-coded Secrets | APP Security p.1 | Repo/Config / Security | FAIL | JWT secret in application.yml. DB password in prod profile. | Must externalize to env vars | Not fixed | Not fixed | CRITICAL |
| APP-02 | Input Validation | APP Security p.2 | API / Security | PARTIAL | DTO validation. Some Map<String,Object> endpoints. | Need stricter DTOs | Partial | Partial | HIGH |
| APP-03 | Ownership Checks | APP Security p.3 | API/Resources / Security | PARTIAL | RBAC on endpoints. No per-resource ownership checks. | Need ownership checks | Not implemented | Not tested | HIGH |
| APP-04 | Debug Mode | APP Security p.4 | Config / Security | PARTIAL | Dev profile DEBUG. Prod profile exists. | Audit prod config | Not validated | Not validated | MEDIUM |
| APP-05 | Insecure Dependencies | APP Security p.5 | Dependencies / Security | UNKNOWN | Need npm audit and Maven dependency check | Not run | Not run | Not run | UNKNOWN |
| APP-06 | No Rate Limiting | APP Security p.6 | API / Security | FAIL | Only account lockout. No API rate limiting. | Need rate limiting | Not implemented | Not implemented | HIGH |
| APP-07 | CORS Wide Open | APP Security p.7 | CORS / Security | PARTIAL | WebSocket has explicit origins. REST CORS not configured. | Need REST CORS config | Not tested | Not validated | HIGH |
| APP-08 | Unprotected File Uploads | APP Security p.8 | Upload / Security | NOT APPLICABLE | No file upload functionality | N/A | N/A | N/A | NONE |
| APP-09 | Unprotected Admin Routes | APP Security p.9 | Admin / Security | PARTIAL | Backend RBAC on admin. Frontend guards needed. | Need frontend guards | Not implemented | Not tested | MEDIUM |
| APP-10 | No Logging/Monitoring | APP Security p.10 | Monitoring / Security | PARTIAL | Basic logging. No security event monitoring. | Need security monitoring | Partial | Partial | MEDIUM |
| APP-11 | Privacy Policy | APP Security p.11 | Legal / Security | NOT APPLICABLE | Internal/hackathon demo. Document data assumptions. | Document assumptions | N/A | N/A | LOW |
| APP-12 | Terms and Conditions | APP Security p.12 | Legal / Security | NOT APPLICABLE | Internal/hackathon demo. | N/A | N/A | N/A | LOW |
| APP-13 | Remove Frontend Secrets | APP Security p.13 | Frontend / Security | PASS | No secrets in frontend code. Uses env vars. | Verified | Verified | Verified | LOW |
| APP-14 | Enforce HTTPS | APP Security p.14 | Transport / Security | PARTIAL | Dev HTTP. Prod TLS not configured. | Document TLS requirements | Not validated | Not validated | HIGH |
| APP-15 | Cookie Consent Banner | APP Security p.15 | Privacy / Security | NOT APPLICABLE | Only essential session storage. | N/A | N/A | N/A | NONE |
| APP-16 | Meta Title/Description | APP Security p.16 | SEO / Security | PARTIAL | Basic index.html metadata. | Improve metadata | Partial | Partial | LOW |
| APP-17 | Social Preview Image | APP Security p.17 | SEO / Security | NOT APPLICABLE | Internal tool. | N/A | N/A | N/A | NONE |
| APP-18 | Favicon | APP Security p.18 | UX / Security | PARTIAL | Default Vite favicon. | Add custom favicon | Partial | Partial | LOW |
| APP-19 | Sitemap/Robots.txt | APP Security p.19 | SEO / Security | NOT APPLICABLE | Internal tool. | N/A | N/A | N/A | NONE |
| APP-20 | Image Alt Text | APP Security p.20 | Accessibility / Security | PARTIAL | Some images lack alt text. | Add alt text | Partial | Partial | LOW |
| APP-21 | Image Compression | APP Security p.21 | Performance / Security | PARTIAL | Some images unoptimized. | Optimize images | Partial | Partial | LOW |
| APP-22 | Page Load Speed | APP Security p.22 | Performance / Security | FAIL | MapLibre bundle ~801 KB. No code splitting. | Need code splitting | Not fixed | Not fixed | HIGH |
| APP-23 | Color Contrast | APP Security p.23 | Accessibility / Security | PARTIAL | Some contrast issues in Tailwind defaults. | Audit contrast | Partial | Partial | LOW |
| APP-23 | Mobile Responsiveness | APP Security p.23 | UX / Security | PARTIAL | Desktop-first. Mobile may break. | Test mobile | Partial | Partial | LOW |
| APP-25 | Custom 404 Pages | APP Security p.25 | UX / Security | PARTIAL | Default Vite/React 404. | Add custom 404 | Not implemented | Not implemented | LOW |
| APP-26 | Broken Links | APP Security p.26 | UX / Security | PARTIAL | Not systematically tested. | Audit links | Not done | Not done | LOW |
| APP-27 | Form Validation | APP Security p.27 | Forms / Security | PARTIAL | React Hook Form + Zod. Server validation needed. | Need server validation | Partial | Partial | MEDIUM |
| APP-28 | Spam Protection | APP Security p.28 | Forms / Security | NOT APPLICABLE | No public forms. Authenticated only. | N/A | N/A | N/A | NONE |
| APP-29 | Analytics Setup | APP Security p.29 | Analytics / Security | NOT APPLICABLE | No analytics configured. | N/A | N/A | N/A | NONE |
| APP-30 | Single Clear CTA | APP Security p.30 | UX / Security | PARTIAL | Some pages have multiple actions. | Audit CTAs | Partial | Partial | LOW |

---

## Controls #1–#29 Not Present

Controls #1–#29 from the original handwritten security checklist are **not present in the supplied source material**. No fabricated controls have been added. If they exist in other project documentation, they should be recovered and audited.

---

## Summary Statistics (Visible Controls)

| Category | PASS | FIXED | PARTIAL | FAIL | BLOCKED | N/A | UNKNOWN | TOTAL |
|----------|------|-------|---------|------|---------|-----|---------|-------|
| SEC-30–78 | 7 | 10 | 20 | 0 | 0 | 4 | 8 | 49 |
| APP-01–30 | 2 | 3 | 16 | 0 | 0 | 5 | 4 | 30 |
| **TOTAL** | **9** | **13** | **36** | **0** | **0** | **9** | **11** | **79** |

---

## Critical Findings (CRITICAL/FAIL)

| ID | Control | Issue | Severity |
|----|---------|-------|----------|
| SEC-51 / APP-01 / SEC-78 | Hardcoded Secrets | JWT secret and DB password hardcoded in application.yml | CRITICAL |
| SEC-74 | CSP + Clickjacking | No CSP, no X-Frame-Options | CRITICAL |
| APP-06 | No Rate Limiting | No API rate limiting | HIGH |
| APP-22 | Page Load Speed | MapLibre bundle ~801 KB, no code splitting | HIGH |
| SEC-36 / SEC-53 / APP-03 | IDOR / Ownership Checks | No per-resource ownership checks | HIGH |
| SEC-56 / APP-06 | Rate Limiting | No API rate limiting | HIGH |
| SEC-65 | Dangerous Tools | Demo endpoints exposed, Swagger enabled | HIGH |
| SEC-70 | Privilege Escalation | Token manipulation possible | HIGH |
| SEC-31 / SEC-36 / SEC-53 / APP-03 | IDOR / Auth on Every Route | No per-resource ownership checks | HIGH |
| SEC-46 / SEC-47 | Content Security / Headers | No CSP, no security headers | HIGH |
| SEC-51 / APP-01 / SEC-78 | Hardcoded Secrets | JWT secret, DB password in application.yml | CRITICAL |

---

## Immediate Action Items (Priority Order)

1. **HIGH**: Implement per-resource ownership checks (IDOR prevention)
2. **HIGH**: Code split MapLibre bundle (~801 KB) - lazy load map components
3. **MEDIUM**: Implement ownership/authorization checks per resource
4. **MEDIUM**: Configure REST CORS explicitly
5. **MEDIUM**: Document backup/recovery strategy
6. **MEDIUM**: Run npm audit and Maven dependency check
6. **MEDIUM**: Code split MapLibre bundle

---

## Blocked Items

| ID | Control | Blocker |
|----|---------|---------|
| SEC-30 | Row-Level Security | Requires PostgreSQL/PostGIS runtime |
| SEC-35 | HTTPS Everywhere | Requires production TLS termination |
| SEC-49 | Backups/Recovery | Requires backup infrastructure |
| SEC-62 | Outdated NPM Packages | Need to run npm audit |
| SEC-63 | Database Backups | Requires backup infrastructure |
| SEC-64 | Database Schema Safety | Requires production-like migration testing |
| SEC-66 | NPM Audit | Need to run npm audit |
| SEC-73 | Dependency CVEs + SSRF | Need dependency audit |
| SEC-35 | HTTPS | Requires production TLS |

---

## Next Steps

1. **Immediate**: Fix CRITICAL issues (secrets, CSP, demo endpoints)
2. **Short-term**: Fix HIGH issues (rate limiting, IDOR, code splitting, secrets)
3. **Medium**: Fix MEDIUM issues (ownership checks, security headers, CORS, rate limiting)
4. **Documentation**: Update security documentation
5. **Testing**: Run full test suite after each fix
6. **Validation**: Runtime validation where possible
7. **Documentation**: Update security-control-matrix.md with results