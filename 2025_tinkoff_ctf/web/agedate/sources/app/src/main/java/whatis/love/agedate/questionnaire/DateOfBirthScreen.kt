package whatis.love.agedate.questionnaire

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import whatis.love.agedate.api.model.BirthDateVisibility
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateOfBirthScreen(
    day: Int,
    month: Int,
    year: Int,
    dateError: String? = null,
    onUpdate: (Int, Int, Int, BirthDateVisibility) -> Unit,
    onNext: () -> Unit,
) {
    val calendar = remember { Calendar.getInstance() }
    calendar.set(year, month - 1, day)

    var selectedDay by remember { mutableIntStateOf(day) }
    var selectedMonth by remember { mutableIntStateOf(month) }
    var selectedYear by remember { mutableIntStateOf(year) }
    var dateVisibility by remember { mutableStateOf(BirthDateVisibility.HIDE_NONE) }
    var localDateError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(dateError) {
        dateError?.let { localDateError = it }
    }

    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale("ru")) }
    var showDatePicker by remember { mutableStateOf(false) }

    fun poke() {
        onUpdate(selectedDay, selectedMonth, selectedYear, dateVisibility)
    }
    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = calendar.timeInMillis,
            )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newCalendar = Calendar.getInstance()
                            newCalendar.timeInMillis = millis
                            selectedYear = newCalendar.get(Calendar.YEAR)
                            selectedMonth = newCalendar.get(Calendar.MONTH) + 1
                            selectedDay = newCalendar.get(Calendar.DAY_OF_MONTH)
                            poke()
                            localDateError = null
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Выбрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Дата рождения",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Вы можете скрыть свой возраст или дату рождения",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedVisibility(
            visible = localDateError != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Text(
                    text = localDateError ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePicker = true
                        localDateError = null
                    },
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            border =
                if (localDateError != null) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                } else {
                    null
                },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                calendar.set(selectedYear, selectedMonth - 1, selectedDay)
                Text(
                    text = dateFormatter.format(calendar.time),
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showDatePicker = true
                        localDateError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Изменить дату", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Text(
                    text = "Настройки приватности:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                dateVisibility = BirthDateVisibility.HIDE_NONE
                                localDateError = null
                            }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = dateVisibility == BirthDateVisibility.HIDE_NONE,
                        onClick = {
                            dateVisibility = BirthDateVisibility.HIDE_NONE
                            poke()
                            localDateError = null
                        },
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Показать полную дату",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                dateVisibility = BirthDateVisibility.HIDE_AGE
                                localDateError = null
                            }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = dateVisibility == BirthDateVisibility.HIDE_AGE,
                        onClick = {
                            dateVisibility = BirthDateVisibility.HIDE_AGE
                            poke()
                            localDateError = null
                        },
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Скрыть год рождения",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                dateVisibility = BirthDateVisibility.HIDE_BIRTHDAY
                                localDateError = null
                            }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = dateVisibility == BirthDateVisibility.HIDE_BIRTHDAY,
                        onClick = {
                            dateVisibility = BirthDateVisibility.HIDE_BIRTHDAY
                            poke()
                            localDateError = null
                        },
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Скрыть дату рождения",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = {
                    poke()
                    onNext()
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(56.dp),
            ) {
                Text(
                    "Далее",
                    fontSize = 18.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
    LaunchedEffect(dateError) {
        if (dateError != null && !showDatePicker) {
            showDatePicker = true
        }
    }
}
