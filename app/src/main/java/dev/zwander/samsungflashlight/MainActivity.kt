package dev.zwander.samsungflashlight

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.zwander.samsungflashlight.activities.BaseActivity
import dev.zwander.samsungflashlight.ui.theme.SamsungFlashlightTheme
import dev.zwander.samsungflashlight.util.PrefManager
import dev.zwander.samsungflashlight.util.prefManager
import kotlin.math.roundToInt

class MainActivity : BaseActivity() {
    override fun onPermissionsGranted() {
        setContent {
            MainContent()
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current

    var strength by remember {
        mutableStateOf(context.prefManager.lightStrength)
    }

    LaunchedEffect(key1 = strength) {
        if (context.prefManager.lightStrength != strength) {
            context.prefManager.lightStrength = strength
        }
    }

    DisposableEffect(key1 = null) {
        val listener = OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                PrefManager.Keys.KEY_LIGHT_STRENGTH -> {
                    strength = context.prefManager.lightStrength
                }
            }
        }

        context.prefManager.registerOnSharedPreferenceChangedListener(listener)

        onDispose {
            context.prefManager.unregisterOnSharedPreferencesChangedListener(listener)
        }
    }

    SamsungFlashlightTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(id = R.string.strength)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "1")

                        Slider(
                            value = strength.toFloat(),
                            onValueChange = { strength = it.roundToInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.weight(1f)
                        )

                        Text(text = "5")
                    }
                }
            }
        }
    }
}