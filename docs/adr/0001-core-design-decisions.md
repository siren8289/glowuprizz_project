# ADR 0001: Core design decisions

## Status
Accepted

## Decisions

| Topic | Decision |
| --- | --- |
| Administrator authentication | A time-limited HMAC bearer token is issued after BCrypt password verification. All `/api/**` routes other than login require it. |
| HTML storage | The single uploaded HTML document is stored as `FormTemplate.htmlContent` in PostgreSQL. |
| Custom form | Each campaign creates one `CustomForm` record that references its chosen `FormTemplate`. |
| HTML isolation | The public shell displays the uploaded document in an iframe with `sandbox="allow-forms"`, no script permission, a restrictive CSP, and a server-injected submission target. |
| Visitor identity | A random UUID is retained as an HttpOnly, SameSite=Lax cookie. A `Visit` is retained for every distribution-link access. |
| Submission data | Variable form fields are stored as JSON text in `Lead.data`; campaign, distribution link, and visitor remain relational foreign keys. |
| Duplicate submissions | One visitor can submit once per distribution link. |
| Attribution | A cryptographically random distribution token identifies the campaign and one of Instagram, X, YouTube, or Threads. |
| Conversion rate | `lead count / distinct visitor count × 100`; zero visitors yields `0`. |
