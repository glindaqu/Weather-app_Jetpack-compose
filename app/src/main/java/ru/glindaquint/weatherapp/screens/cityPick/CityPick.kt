package ru.glindaquint.weatherapp.screens.cityPick

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import ru.glindaquint.weatherapp.DEBOUNCE_DELAY
import ru.glindaquint.weatherapp.PADDING
import ru.glindaquint.weatherapp.ui.theme.Typography
import ru.glindaquint.weatherapp.viewModels.implementation.CityPickViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Suppress("ktlint:standard:function-naming")
@Composable
fun CityPick(
    state: CityPickState,
    onCityPick: (String, String, String) -> Unit,
) {
    val cityPickViewModel =
        ViewModelProvider(LocalContext.current as ComponentActivity)[CityPickViewModel::class.java]
    val cities by cityPickViewModel.cities.collectAsState(initial = null)
    if (cities != null && state.show) {
        Dialog(
            onDismissRequest = { state.show = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Поиск города",
                                style = Typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.onBackground),
                        actions = {
                            IconButton(onClick = { state.show = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        },
                    )
                },
            ) { padding ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    CitySearchField(onInput = {
                        cityPickViewModel.refreshCities(it)
                    })
                    Spacer(modifier = Modifier.weight(0.01f))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(0.9f),
                        verticalArrangement = Arrangement.spacedBy(0.5.dp),
                    ) {
                        items(cities ?: listOf()) {
                            CityItem(
                                name = it.localNames?.ru ?: it.name!!,
                                country = it.country!!,
                                state = it.state ?: "",
                                onClick = { city, country, st ->
                                    onCityPick(city, country, st)
                                    state.show = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun CityItem(
    name: String,
    country: String,
    state: String,
    onClick: (String, String, String) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onPrimary)
                .clickable { onClick(name, country, state) },
    ) {
        Text(
            text = "$name ($country, $state)",
            style = Typography.bodyLarge,
            modifier = Modifier.padding(PADDING),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun CitySearchField(onInput: (String) -> Unit) {
    var textFieldState by remember { mutableStateOf(TextFieldValue("")) }
    TextField(
        value = textFieldState,
        onValueChange = { textFieldState = it },
        modifier = Modifier.fillMaxWidth().padding(PADDING),
        trailingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
    )
    LaunchedEffect(key1 = textFieldState, block = {
        if (textFieldState.text.isNotBlank()) {
            delay(DEBOUNCE_DELAY)
            onInput(textFieldState.text)
        }
    })
}
