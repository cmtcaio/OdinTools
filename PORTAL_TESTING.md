# Odin 2 Portal charging separation – experimental test plan

This branch is **unverified**. It has not been compiled or tested on an Odin 2 Portal. Keep charging supervised until all tests pass.

## Why the change?

Observed on an Odin 2 Portal using the **stock** charging separation toggle:

| State | `settings system is_charging_separation` | `/sys/class/qcom-battery/restrict_chg` | `/sys/class/qcom-battery/restrict_cur` |
| --- | --- | --- | --- |
| Off | 0 | 0 | 1000000 |
| On | 1 | 0 | 1000000 |

OdinTools 1.3.1 also writes the two `restrict_*` values. This branch avoids writing them **on Portal only**.

## Supervised checks before trusting unattended charging

1. Verify app launches and recognizes `ro.product.model=Odin2 Portal` and `ro.vendor.retro.name=Q9`.
2. Charge from below the configured upper threshold, watch the native separation toggle switch on at the upper threshold.
3. Confirm neither `restrict_chg` nor `restrict_cur` changed from stock values.
4. Disconnect external power and confirm normal portable use.
5. With external power connected and separation on, observe what happens when screen turns off, and when the charge level falls below the lower threshold.
6. Verify charging **actually resumes**, including battery percentage increasing; do not infer success only from Android settings.
7. Turn off the app's charge limit. Confirm separation is off and subsequent charging works.
8. Reboot and repeat with the device locked and unplugged; verify no wake/sleep loop.

If the device behaves unexpectedly, switch off the app's automatic limit and use the native Odin setting. Do not leave charging unattended during testing.

No hardware-level bypass guarantee is made by this experimental change.