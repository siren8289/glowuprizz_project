# API summary

All `/api` endpoints except login require `Authorization: Bearer <token>`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/auth/login` | Admin authentication |
| POST, GET | `/api/forms` | Upload and list HTML templates |
| GET | `/api/forms/{formId}` | Get template metadata |
| POST, GET | `/api/campaigns` | Create and list campaigns with a CustomForm |
| GET | `/api/campaigns/{campaignId}` | Get campaign |
| POST | `/api/campaigns/{campaignId}/distributions` | Create channel distribution link |
| GET | `/r/{token}` | Track visitor and redirect to public form |
| GET | `/public/forms/{formId}` | Render isolated public form |
| POST | `/public/forms/{formId}/submissions` | Store public application |
| GET | `/api/campaigns/{campaignId}/leads` | List CRM leads |
| GET | `/api/campaigns/{campaignId}/analytics` | Campaign metrics |
| GET | `/api/campaigns/{campaignId}/analytics/channels` | Channel metrics |

The legacy `distribution-links` and `statistics` endpoint names remain as aliases.
