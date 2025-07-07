# KMIP Key Lifecycle States and Transitions

## State Overview

```
[PRE-ACTIVE] → [ACTIVE] → [DEACTIVATED] → [DESTROYED]
      ↓             ↓            ↓
      └─────────[COMPROMISED]─────┘
```

## Key States Description

1. **PRE-ACTIVE**
   - Initial state after object creation
   - Object exists but not yet valid for cryptographic operations
   - Can transition to ACTIVE based on scheduled activation or immediate use
   - Can be compromised before activation

2. **ACTIVE**
   - Object is valid and available for cryptographic operations
   - Has two sub-states:
     * Protecting: Can be used to protect new data
     * Processing Only: Can only process previously protected data
   - Can transition to DEACTIVATED or COMPROMISED

3. **DEACTIVATED**
   - Object is no longer valid for new cryptographic operations
   - Can still process (decrypt/verify) previously protected data
   - Cannot be used to protect new data
   - Can transition to COMPROMISED or DESTROYED

4. **COMPROMISED**
   - Indicates a security breach has been detected
   - Can occur from any other state except DESTROYED
   - Limited operations possible based on security policy
   - Can only transition to DESTROYED

5. **DESTROYED**
   - Terminal state - no further transitions possible
   - Object metadata retained for audit purposes
   - Cryptographic material permanently erased
   - No operations permitted

## State Transitions and Operations

1. **Creation to PRE-ACTIVE**
   - Triggered by: Create, Register, or Import operations
   - Sets initial attributes and security parameters
   - Can specify future activation time

2. **PRE-ACTIVE to ACTIVE**
   - Triggered by:
     * Scheduled activation time reached
     * Explicit Activate operation
     * First cryptographic operation (if immediate activation allowed)
   - Enables cryptographic operations

3. **ACTIVE State Transitions**
   - Within ACTIVE:
     * Protecting → Processing Only: Based on policy or explicit operation
     * Processing Only remains until deactivation
   - To DEACTIVATED:
     * Scheduled deactivation time reached
     * Explicit Deactivate operation
     * Policy-based triggers (usage count, time period)

4. **DEACTIVATED to DESTROYED**
   - Triggered by Destroy operation
   - Requires proper authorization
   - Permanent and irreversible

5. **COMPROMISED State**
   - Entry from any state via Revoke operation
   - Requires compromise reason
   - Special handling based on security policy
   - Can only exit to DESTROYED

## Implementation Considerations

1. **State Validation**
   - Validate all state transitions
   - Enforce proper operation sequence
   - Check authorization for each transition
   - Maintain audit trail of state changes

2. **Automatic Transitions**
   - Monitor scheduled events
   - Handle time-based transitions
   - Enforce policy-based transitions
   - Update audit logs for automated changes

3. **Security Implications**
   - Enforce access control per state
   - Handle compromised objects securely
   - Maintain cryptographic separation
   - Protect state transition metadata