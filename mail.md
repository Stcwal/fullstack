# Mail Production Checklist

Use this before going live with invite and notification email flows.

## Security
- Never hardcode SMTP passwords or API keys in source code.
- Store secrets in environment variables or a secret manager.
- Rotate credentials regularly (and immediately after demos if shared).
- Use a dedicated sender account for system mail (not a personal mailbox).

## Sender Domain and Deliverability
- Send from a domain you control (for example: noreply@yourdomain.com).
- Configure and verify SPF, DKIM, and DMARC for the sender domain.
- Keep a stable From address and friendly sender name.
- Warm up new sending domains/accounts gradually.

## Provider and Setup
- Prefer a transactional provider for production (SendGrid, Postmark, SES, Mailgun, etc.).
- Keep separate credentials for dev, staging, and production.
- Keep sandbox/test mode for non-production environments.
- Set sane SMTP/API timeouts.

## App Behavior
- Treat email send as a recoverable operation (retry strategy/backoff).
- Consider async sending (queue/outbox) to avoid blocking API requests.
- Log message IDs and delivery attempts (without logging secrets).
- Decide failure behavior clearly: rollback vs continue with warning.

## Invite Flow Specific
- One-time token only, short expiry (24h is fine).
- Invalidate old pending invites on resend.
- Add resend cooldown/rate limit to prevent abuse.
- Add audit events: invite created, resent, accepted, expired.

## Observability and Ops
- Add alerts for high mail failure rate.
- Track bounce, block, and complaint metrics.
- Create a simple runbook for SMTP/provider outages.
- Test full invite flow regularly in staging.

## Compliance and Content
- Keep email templates clear and minimal.
- Avoid sensitive data in email body.
- Include support contact information.
- Ensure legal/compliance requirements are met for your region.
