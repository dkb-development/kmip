# KMIP DB Operations Cheat Sheet

This guide summarizes the essential database operations for key KMIP operations, mapped to your schema. Text diagrams are included for clarity.

---

## 1. Create Symmetric Key

- **Insert** new key into `managed_objects`
- **Insert** attributes into `object_attributes`

```
[Client Request]
     |
     v
+-------------------+
| managed_objects   |  <--- INSERT (new key)
+-------------------+
     |
     v
+-------------------+
| object_attributes |  <--- INSERT (attributes)
+-------------------+
```

---

## 2. Get Symmetric Key

- **Select** key from `managed_objects` by key ID
- **Select** attributes from `object_attributes` by key ID

```
[Get Request]
     |
     v
+-------------------+
| managed_objects   |  <--- SELECT (by id)
+-------------------+
     |
     v
+-------------------+
| object_attributes |  <--- SELECT (by object_id)
+-------------------+
```

---

## 3. Destroy Symmetric Key

- **Update** `managed_objects`:
  - Set `state = 'DESTROYED'`
  - Set `destruction_date`
  - Optionally, set `deleted = TRUE`

```
[Destroy Request]
     |
     v
+-------------------+
| managed_objects   |  <--- UPDATE (state, destruction_date, deleted)
+-------------------+
```

---

## 4. Re-Key

- **Insert** new key into `managed_objects`
- **Insert** attributes for new key into `object_attributes`
- **Insert** into `object_links` with `link_type = 'rekey'`
- **Update** old key's state if required (e.g., set to `DEACTIVATED`)

```
[Re-Key Request]
     |
     v
+-------------------+        +-------------------+
| managed_objects   |<------>| object_links      |
| (old key)         |        | link_type='rekey' |
+-------------------+        +-------------------+
     |                             ^
     |                             |
     v                             |
+-------------------+              |
| managed_objects   |--------------+
| (new key)         |
+-------------------+
     |
     v
+-------------------+
| object_attributes |  <--- INSERT (new key attrs)
+-------------------+
```

---

## 5. Add Attribute

- **Insert** new attribute row in `object_attributes` for the key

```
[Add Attribute]
     |
     v
+-------------------+
| object_attributes |  <--- INSERT (new attribute)
+-------------------+
```

---

## 6. Activate Key

- **Update** `managed_objects`:
  - Set `activation_date`
  - Set `state = 'ACTIVE'`

```
[Activate]
     |
     v
+-------------------+
| managed_objects   |  <--- UPDATE (activation_date, state)
+-------------------+
```

---

## 7. Deactivate Key

- **Update** `managed_objects`:
  - Set `deactivation_date`
  - Set `state = 'DEACTIVATED'`

```
[Deactivate]
     |
     v
+-------------------+
| managed_objects   |  <--- UPDATE (deactivation_date, state)
+-------------------+
```

---

## 8. Protect Stop (Set Protect Stop Date)

- **Update** `managed_objects`:
  - Set `protect_stop_date`

```
[Protect Stop]
     |
     v
+-------------------+
| managed_objects   |  <--- UPDATE (protect_stop_date)
+-------------------+
```

---

## 9. Set Process Start Date

- **Update** `managed_objects`:
  - Set `process_start_date`

```
[Set Process Start]
     |
     v
+-------------------+
| managed_objects   |  <--- UPDATE (process_start_date)
+-------------------+
```

---

## 10. Revoke Key

- **Update** `managed_objects`:
  - Set `state = 'COMPROMISED'` (or similar)
  - Set `compromise_occurrence_date`

```
[Revoke]
     |
     v
+-------------------+
| managed_objects   |  <--- UPDATE (state, compromise_occurrence_date)
+-------------------+
```

---

## 11. Get Attributes / Get Attribute List

- **Select** from `object_attributes` by key ID

```
[Get Attributes]
     |
     v
+-------------------+
| object_attributes |  <--- SELECT (by object_id)
+-------------------+
```

---

## 12. Modify Attribute

- **Update** row in `object_attributes` for the key

```
[Modify Attribute]
     |
     v
+-------------------+
| object_attributes |  <--- UPDATE (attribute)
+-------------------+
```

---

## 13. Delete Attribute

- **Delete** row in `object_attributes` for the key

```
[Delete Attribute]
     |
     v
+-------------------+
| object_attributes |  <--- DELETE (attribute)
+-------------------+
```

---

## 14. Example: Object Relationship (Re-Key, Split, Join, etc.)

- **Insert** into `object_links` with appropriate `link_type` (e.g., 'rekey', 'split', 'join')

```
[Relationship]
     |
     v
+-------------------+
| object_links      |  <--- INSERT (source_object_id, target_object_id, link_type)
+-------------------+
```

---

> **Note:** For more advanced operations (e.g., Split, Join, DeriveKey, Grouping), extend the use of `object_links` and consider additional tables as needed. 