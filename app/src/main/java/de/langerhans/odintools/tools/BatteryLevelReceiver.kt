package de.langerhans.odintools.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.AndroidEntryPoint
import de.langerhans.odintools.data.SharedPrefsRepo
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLevelReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settings: SettingsRepo

    @Inject
    lateinit var prefs: SharedPrefsRepo

    override fun onReceive(context: Context, intent: Intent) {
        if (!prefs.chargeLimitEnabled || intent.action !in ALLOWED_INTENTS) {
            return
        }

        // Read the sticky battery intent so screen and power events use current values.
        // BATTERY_PROPERTY_STATUS / isCharging() cannot reliably indicate a cable
        // connection while native charging separation is active.
        val batteryState = if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            intent
        } else {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } ?: return

        val batteryLevel = batteryState.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        if (batteryLevel !in 0..100) return

        val isPluggedIn = intent.action != Intent.ACTION_POWER_DISCONNECTED &&
            batteryState.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        val separationEnabled = settings.chargingSeparationEnabled()

        // Recovery comes first, including when unplugged. Never turn on bypass
        // while portable: wait until power is connected and the cap is reached.
        if (batteryLevel <= prefs.minBatteryLevel && separationEnabled) {
            settings.disableChargingSeparation()
        } else if (isPluggedIn && batteryLevel >= prefs.maxBatteryLevel && !separationEnabled) {
            settings.enableChargingSeparation()
        }
    }

    companion object {
        val ALLOWED_INTENTS = listOf(
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_SCREEN_OFF,
            Intent.ACTION_SCREEN_ON,
        )
    }
}
