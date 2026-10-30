package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.ScienceBlue
import com.example.ui.theme.SelectionGlow
import com.example.viewmodel.DrawingUiState
import com.example.viewmodel.ToolMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroDrawTopBar(
  uiState: DrawingUiState,
  onOpenWaterTest: () -> Unit,
  onOpenPresets: () -> Unit,
  onOpenClearDialog: () -> Unit,
  onOpenHelp: () -> Unit,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onToggleProperties: () -> Unit,
  isNarrowScreen: Boolean
) {
  TopAppBar(
    title = {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(ScienceBlue),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Science,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
          Text(
            text = "microDraw",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        // Subtitle showing student's county and water test date
        Text(
          text = "${uiState.waterSampleInfo.county} • Test: ${uiState.waterSampleInfo.testDate}",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .clickable { onOpenWaterTest() }
            .testTag("top_bar_water_info_chip")
        )
      }
    },
    actions = {
      IconButton(
        onClick = onUndo,
        enabled = uiState.canUndo,
        modifier = Modifier.testTag("undo_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
      }

      IconButton(
        onClick = onRedo,
        enabled = uiState.canRedo,
        modifier = Modifier.testTag("redo_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
      }

      IconButton(
        onClick = onOpenPresets,
        modifier = Modifier.testTag("presets_button")
      ) {
        Icon(Icons.Default.Category, contentDescription = "Preset Molecules")
      }

      IconButton(
        onClick = onOpenClearDialog,
        modifier = Modifier.testTag("clear_button")
      ) {
        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Canvas")
      }

      if (isNarrowScreen) {
        IconButton(
          onClick = onToggleProperties,
          modifier = Modifier.testTag("toggle_properties_button")
        ) {
          Icon(
            imageVector = if (uiState.showPropertiesOnNarrowScreen) Icons.Default.Draw else Icons.Default.Analytics,
            contentDescription = "Toggle Properties"
          )
        }
      }

      IconButton(
        onClick = onOpenHelp,
        modifier = Modifier.testTag("help_button")
      ) {
        Icon(Icons.Default.HelpOutline, contentDescription = "Help Guide")
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  )
}

@Composable
fun DrawingToolsBar(
  uiState: DrawingUiState,
  onSetToolMode: (ToolMode) -> Unit,
  onSetElement: (ElementType) -> Unit,
  onSetBondType: (BondType) -> Unit,
  onSetHydrogenLocation: (HydrogenLocation) -> Unit,
  onAdjustCharge: (Int) -> Unit,
  onStampBenzene: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Row 1: Mode Switcher & Quick Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Tool Mode Segmented Buttons
      SingleChoiceSegmentedButtonRow(modifier = Modifier.height(44.dp)) {
        ToolMode.values().forEachIndexed { index, mode ->
          SegmentedButton(
            selected = uiState.activeToolMode == mode,
            onClick = { onSetToolMode(mode) },
            shape = SegmentedButtonDefaults.itemShape(index = index, count = ToolMode.values().size),
            icon = {},
            modifier = Modifier.testTag("tool_mode_${mode.name.lowercase()}")
          ) {
            Text(mode.label, style = MaterialTheme.typography.labelMedium)
          }
        }
      }

      // Quick Stamp: Benzene Ring
      FilledTonalButton(
        onClick = onStampBenzene,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier
          .height(44.dp)
          .testTag("stamp_benzene_button")
      ) {
        Icon(Icons.Default.Polyline, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("+ Benzene", style = MaterialTheme.typography.labelMedium)
      }

      // Charge Controls (for selected atom)
      if (uiState.selectedAtomId != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          FilledTonalIconButton(
            onClick = { onAdjustCharge(1) },
            modifier = Modifier.size(40.dp).testTag("plus_charge_button")
          ) {
            Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp)
          }
          FilledTonalIconButton(
            onClick = { onAdjustCharge(-1) },
            modifier = Modifier.size(40.dp).testTag("minus_charge_button")
          ) {
            Text("–", fontWeight = FontWeight.Bold, fontSize = 18.sp)
          }
        }
      }
    }

    // Row 2: Element Selector Chips (horizontal scrollable)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Element:",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      ElementType.values().forEach { element ->
        val isSelected = (uiState.selectedElement == element)
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(element.displayColor)
            .border(
              width = if (isSelected) 3.dp else 1.dp,
              color = if (isSelected) SelectionGlow else Color.White,
              shape = CircleShape
            )
            .clickable { onSetElement(element) }
            .testTag("element_chip_${element.symbol}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = element.symbol,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          )
        }
      }
    }

    // Row 3: Bond Type & Hydrogen Position Selectors
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Bond:",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      BondType.values().forEach { bond ->
        FilterChip(
          selected = uiState.selectedBondType == bond,
          onClick = { onSetBondType(bond) },
          label = { Text(bond.displayName, style = MaterialTheme.typography.labelMedium) },
          modifier = Modifier.testTag("bond_chip_${bond.name.lowercase()}")
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = "Hydrogens:",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      HydrogenLocation.values().forEach { hLoc ->
        FilterChip(
          selected = uiState.selectedHydrogenLocation == hLoc,
          onClick = { onSetHydrogenLocation(hLoc) },
          label = { Text(hLoc.displayName.split(" ")[0], style = MaterialTheme.typography.labelMedium) },
          modifier = Modifier.testTag("hydrogen_loc_${hLoc.name.lowercase()}")
        )
      }
    }
  }
}

@Composable
fun PresetsDialog(
  onDismiss: () -> Unit,
  onSelectPreset: (ChemicalPreset) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.testTag("presets_dialog"),
    title = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Category, contentDescription = null, tint = ScienceBlue)
        Text("Example Molecules", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Select a preset chemical structure commonly studied in university water quality labs:",
          style = MaterialTheme.typography.bodyMedium
        )

        ChemicalPresets.allPresets.forEach { preset ->
          Card(
            onClick = { onSelectPreset(preset) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("preset_card_${preset.name.replace(" ", "_")}")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = preset.name,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = preset.description,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Badge(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
              ) {
                Text(
                  text = preset.commonFormula,
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
fun ClearConfirmDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.testTag("clear_confirm_dialog"),
    icon = {
      Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
    },
    title = {
      Text("Clear Canvas?")
    },
    text = {
      Text(
        "Are you sure you want to erase all atoms and bonds on the drawing canvas? You can undo this action.",
        style = MaterialTheme.typography.bodyMedium
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.testTag("confirm_clear_button")
      ) {
        Text("Clear All")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.testTag("help_dialog"),
    title = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.School, contentDescription = null, tint = ScienceBlue)
        Text("How to Use microDraw", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          "• Adding Atoms: Tap anywhere on the canvas to place an atom of your chosen element.",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          "• Connecting Bonds: Tap an atom, then drag to another atom or empty space to draw a bond.",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          "• Changing Bonds: Tap any bond to cycle through Single (-), Double (=), Triple (≡), Wedge, or Dash.",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          "• Moving Atoms: Switch to 'Move' mode to drag atoms around.",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          "• Erasing: Switch to 'Erase' mode and tap any atom or bond to delete it.",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          "• County & Date: Tap the water test banner at the top to record your local water sample location and test date.",
          style = MaterialTheme.typography.bodyMedium
        )
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Got It")
      }
    }
  )
}
