# Manual end-to-end test checklist

Start with a clean database (`docker compose down -v`, then `docker compose up --build`) when repeatable IDs and empty request lists matter.

## Workflow A — meal delivery

- [ ] Sign in as the senior and create a meal request for a future date.
- [ ] Confirm it appears as `REQUESTED` and a notification is created.
- [ ] Sign out, sign in as admin, approve the request, choose the active volunteer, and assign it.
- [ ] Sign in as volunteer; confirm only assigned meals appear.
- [ ] Advance `ASSIGNED → PREPARING → OUT_FOR_DELIVERY → DELIVERED`, adding a completion note.
- [ ] Sign in as senior; confirm the final status, volunteer, note, and notifications.

## Workflow B — companion service

- [ ] As senior, create a companion request for a future date/time.
- [ ] As admin, approve it, schedule an exact date/time, then assign a volunteer.
- [ ] As volunteer, confirm the senior's required contact/address/service details are visible.
- [ ] Advance `ASSIGNED → IN_PROGRESS → COMPLETED` and add a completion note.
- [ ] As senior, confirm scheduled time, completed status, and notification.

## Workflow C — administration and protection

- [ ] Dashboard counts change when requests are created and completed.
- [ ] Filter requests by status.
- [ ] Create a new senior or volunteer, edit name/phone/role, deactivate, and reactivate.
- [ ] Confirm an inactive account cannot sign in and an existing token stops working.
- [ ] Confirm a senior cannot open admin URLs/API data.
- [ ] Confirm a volunteer cannot see or update another volunteer's assignment.
- [ ] Confirm invalid status jumps return a readable error.
- [ ] Confirm completed/in-progress services cannot be cancelled by the senior.
