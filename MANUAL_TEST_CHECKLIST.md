# Manual end-to-end test checklist

Start with a clean database (`docker compose down -v`, then `docker compose up --build`) when repeatable IDs and empty lists matter.

## Workflow A — Meal Delivery

- [ ] As senior, create a future meal request and confirm `REQUESTED` plus notification.
- [ ] As admin, approve and assign an active volunteer.
- [ ] As volunteer, advance `ASSIGNED → PREPARING → OUT_FOR_DELIVERY → DELIVERED` with a note.
- [ ] As senior, confirm final status, volunteer, note, and notifications.

## Workflow B — Companion Visit

- [ ] As senior, create a future Companion Visit request.
- [ ] As admin, approve, schedule an exact time, and assign a volunteer.
- [ ] As volunteer, confirm required service details, then advance `ASSIGNED → IN_PROGRESS → COMPLETED` with a note.
- [ ] As senior, confirm schedule, completion, and notifications.

## Workflow C — Administration and protection

- [ ] Confirm dashboard counts and status filtering.
- [ ] Create, edit, deactivate, and reactivate a user.
- [ ] Confirm inactive accounts cannot sign in and existing tokens stop working.
- [ ] Confirm senior/admin/volunteer API protections and ownership enforcement.
- [ ] Confirm invalid status jumps return readable errors and started/completed work cannot be cancelled.

## Workflow D — RoboCompanion Visit

- [ ] As admin, confirm `RC-01` or `RC-02` is `AVAILABLE`.
- [ ] As senior, create a future RoboCompanion Visit with reason and assistance needs.
- [ ] Confirm `REQUESTED` and a submission notification.
- [ ] As admin, approve, then schedule an exact future date/time.
- [ ] Assign an `AVAILABLE` RoboCompanion and confirm both visit and robot are `ASSIGNED`.
- [ ] As senior, confirm scheduled time and assigned robot name/model.
- [ ] As admin, start service; confirm visit `IN_PROGRESS` and robot `IN_SERVICE`.
- [ ] Complete with a note; confirm visit `COMPLETED` and robot `AVAILABLE`.
- [ ] As senior, confirm final status, robot, completion note, and notifications.

### Workflow D protection checks

- [ ] `MAINTENANCE` and `INACTIVE` robots are absent from assignment choices and rejected by direct API assignment.
- [ ] An assigned/in-service robot cannot be assigned to another active visit.
- [ ] `REQUESTED → ASSIGNED` and other invalid jumps are rejected.
- [ ] Cancelling an assigned visit releases the robot.
- [ ] Senior cancellation of `IN_PROGRESS` or `COMPLETED` is rejected.
- [ ] A senior cannot read another senior's visit or robot inventory; volunteers cannot access RoboCompanion operations.
