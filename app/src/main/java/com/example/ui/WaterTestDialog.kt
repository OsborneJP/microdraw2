package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SampleCounties
import com.example.model.WaterSampleInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTestDialog(
  currentInfo: WaterSampleInfo,
  onDismiss: () -> Unit,
  onSave: (county: String, testDate: String, notes: String) -> Unit
) {
  var selectedCounty by remember { mutableStateOf(currentInfo.county) }
  var customCountyText by remember { mutableStateOf("") }
  var isCustomCounty by remember { mutableStateOf(!SampleCounties.contains(currentInfo.county) || currentInfo.county.contains("Other")) }
  var testDateText by remember { mutableStateOf(currentInfo.testDate) }
  var notesText by remember { mutableStateOf(currentInfo.notes) }
  var dropdownExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.testTag("water_test_dialog"),
    title = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
          text = "Water Sample Details",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "Choose your county and the date of your last water test to link with your drawn chemical structures.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // County Dropdown
        ExposedDropdownMenuBox(
          expanded = dropdownExpanded,
          onExpandedChange = { dropdownExpanded = !dropdownExpanded },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = if (isCustomCounty && customCountyText.isNotEmpty()) customCountyText else selectedCounty,
            onValueChange = {},
            readOnly = true,
            label = { Text("County") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
              .testTag("county_dropdown_trigger")
          )

          ExposedDropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
          ) {
            SampleCounties.forEach { county ->
              DropdownMenuItem(
                text = { Text(county) },
                onClick = {
                  if (county.startsWith("Other")) {
                    isCustomCounty = true
                    selectedCounty = "Custom County"
                  } else {
                    isCustomCounty = false
                    selectedCounty = county
                  }
                  dropdownExpanded = false
                },
                modifier = Modifier.testTag("county_item_${county.replace(" ", "_")}")
              )
            }
          }
        }

        if (isCustomCounty) {
          OutlinedTextField(
            value = customCountyText,
            onValueChange = { customCountyText = it },
            label = { Text("Enter Custom County Name") },
            placeholder = { Text("e.g. Travis County") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("custom_county_input")
          )
        }

        // Date of Last Water Test
        OutlinedTextField(
          value = testDateText,
          onValueChange = { testDateText = it },
          label = { Text("Date of Last Water Test") },
          placeholder = { Text("e.g. October 24, 2026") },
          leadingIcon = {
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("test_date_input")
        )

        // Quick Date Presets
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SuggestionChip(
            onClick = { testDateText = "October 30, 2026" },
            label = { Text("Today") },
            modifier = Modifier.weight(1f)
          )
          SuggestionChip(
            onClick = { testDateText = "October 23, 2026" },
            label = { Text("1 Wk Ago") },
            modifier = Modifier.weight(1f)
          )
          SuggestionChip(
            onClick = { testDateText = "September 30, 2026" },
            label = { Text("1 Mo Ago") },
            modifier = Modifier.weight(1f)
          )
        }

        // Optional Notes
        OutlinedTextField(
          value = notesText,
          onValueChange = { notesText = it },
          label = { Text("Lab Notes (Optional)") },
          placeholder = { Text("e.g. Tap water from university chemistry lab bench 4") },
          maxLines = 3,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("water_test_notes_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalCounty = if (isCustomCounty && customCountyText.isNotBlank()) customCountyText else selectedCounty
          val finalDate = if (testDateText.isNotBlank()) testDateText else "Not Recorded"
          onSave(finalCounty, finalDate, notesText)
        },
        modifier = Modifier.testTag("save_water_test_button")
      ) {
        Text("Save Details")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
