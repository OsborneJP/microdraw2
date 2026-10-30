package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DrawingViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: DrawingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MicroDrawApp(viewModel)
      }
    }
  }
}

@Composable
fun MicroDrawApp(viewModel: DrawingViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("microdraw_main_scaffold"),
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      BoxWithConstraints {
        val isNarrow = maxWidth < 720.dp
        MicroDrawTopBar(
          uiState = uiState,
          onOpenWaterTest = { viewModel.setShowWaterTestDialog(true) },
          onOpenPresets = { viewModel.setShowPresetsDialog(true) },
          onOpenClearDialog = { viewModel.setShowClearConfirmDialog(true) },
          onOpenHelp = { viewModel.setShowHelpDialog(true) },
          onUndo = { viewModel.undo() },
          onRedo = { viewModel.redo() },
          onToggleProperties = { viewModel.togglePropertiesPanel() },
          isNarrowScreen = isNarrow
        )
      }
    }
  ) { innerPadding ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      val isWideScreen = maxWidth >= 720.dp

      if (isWideScreen) {
        // Wide screens (Tablets, Chromebooks, Landscape): Side-by-Side layout
        // Large drawing area on left, properties panel on right
        Row(modifier = Modifier.fillMaxSize()) {
          // Left Area: Drawing Tools and Large Canvas (fills most of screen)
          Column(
            modifier = Modifier
              .weight(0.65f)
              .fillMaxHeight()
          ) {
            DrawingToolsBar(
              uiState = uiState,
              onSetToolMode = { viewModel.setToolMode(it) },
              onSetElement = { viewModel.setSelectedElement(it) },
              onSetBondType = { viewModel.setSelectedBondType(it) },
              onSetHydrogenLocation = { viewModel.setSelectedHydrogenLocation(it) },
              onAdjustCharge = { viewModel.adjustAtomCharge(uiState.selectedAtomId ?: 0, it) },
              onStampBenzene = { viewModel.stampRing(350f, 280f, 6, true) },
              modifier = Modifier.testTag("drawing_tools_bar")
            )

            ChemicalCanvas(
              uiState = uiState,
              onAddAtom = { x, y -> viewModel.addAtomAt(x, y) },
              onAddBondBetween = { from, to -> viewModel.addBondBetween(from, to) },
              onAddBondAndNewAtom = { from, x, y -> viewModel.addBondAndNewAtom(from, x, y) },
              onSelectAtom = { viewModel.selectAtom(it) },
              onSelectBond = { viewModel.selectBond(it) },
              onMoveAtom = { id, x, y -> viewModel.moveAtom(id, x, y) },
              onCycleBond = { viewModel.cycleBondType(it) },
              onDeleteAtom = { viewModel.deleteAtom(it) },
              onDeleteBond = { viewModel.deleteBond(it) },
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chemical_drawing_canvas")
            )
          }

          VerticalDivider()

          // Right Area: Properties Panel
          PropertiesPanel(
            molecule = uiState.molecule,
            properties = uiState.chemicalProperties,
            waterSampleInfo = uiState.waterSampleInfo,
            onOpenWaterTestDialog = { viewModel.setShowWaterTestDialog(true) },
            modifier = Modifier
              .weight(0.35f)
              .fillMaxHeight()
              .testTag("properties_panel_right")
          )
        }
      } else {
        // Narrow screens (Mobile phones in portrait): Mobile-First layout
        // Drawing area fills most of the screen; properties panel moves below editor
        var isPropertiesExpanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize()) {
          // Drawing Tools Bar at top of canvas
          DrawingToolsBar(
            uiState = uiState,
            onSetToolMode = { viewModel.setToolMode(it) },
            onSetElement = { viewModel.setSelectedElement(it) },
            onSetBondType = { viewModel.setSelectedBondType(it) },
            onSetHydrogenLocation = { viewModel.setSelectedHydrogenLocation(it) },
            onAdjustCharge = { viewModel.adjustAtomCharge(uiState.selectedAtomId ?: 0, it) },
            onStampBenzene = { viewModel.stampRing(280f, 260f, 6, true) },
            modifier = Modifier.testTag("drawing_tools_bar_narrow")
          )

          // Large Drawing Area filling most of the screen
          Box(
            modifier = Modifier
              .weight(if (isPropertiesExpanded) 0.45f else 0.82f)
              .fillMaxWidth()
          ) {
            ChemicalCanvas(
              uiState = uiState,
              onAddAtom = { x, y -> viewModel.addAtomAt(x, y) },
              onAddBondBetween = { from, to -> viewModel.addBondBetween(from, to) },
              onAddBondAndNewAtom = { from, x, y -> viewModel.addBondAndNewAtom(from, x, y) },
              onSelectAtom = { viewModel.selectAtom(it) },
              onSelectBond = { viewModel.selectBond(it) },
              onMoveAtom = { id, x, y -> viewModel.moveAtom(id, x, y) },
              onCycleBond = { viewModel.cycleBondType(it) },
              onDeleteAtom = { viewModel.deleteAtom(it) },
              onDeleteBond = { viewModel.deleteBond(it) },
              modifier = Modifier
                .fillMaxSize()
                .testTag("chemical_drawing_canvas_narrow")
            )
          }

          HorizontalDivider()

          // Properties Panel below editor on narrow screens
          Column(
            modifier = Modifier
              .weight(if (isPropertiesExpanded) 0.55f else 0.18f)
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surface)
          ) {
            // Header bar for expanding/collapsing properties
            Surface(
              onClick = { isPropertiesExpanded = !isPropertiesExpanded },
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("properties_panel_header_toggle")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = "Properties & Water Test",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(MaterialTheme.colorScheme.primaryContainer)
                      .padding(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = uiState.molecule.calculateFormula(),
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = if (isPropertiesExpanded) "Hide" else "Show Details",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                  )
                  Icon(
                    imageVector = if (isPropertiesExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isPropertiesExpanded) "Collapse" else "Expand"
                  )
                }
              }
            }

            // Scrollable Properties content below
            PropertiesPanel(
              molecule = uiState.molecule,
              properties = uiState.chemicalProperties,
              waterSampleInfo = uiState.waterSampleInfo,
              onOpenWaterTestDialog = { viewModel.setShowWaterTestDialog(true) },
              modifier = Modifier
                .fillMaxSize()
                .testTag("properties_panel_bottom")
            )
          }
        }
      }
    }
  }

  // Dialogs
  if (uiState.showWaterTestDialog) {
    WaterTestDialog(
      currentInfo = uiState.waterSampleInfo,
      onDismiss = { viewModel.setShowWaterTestDialog(false) },
      onSave = { county, date, notes ->
        viewModel.updateWaterSampleInfo(county, date, notes)
      }
    )
  }

  if (uiState.showPresetsDialog) {
    PresetsDialog(
      onDismiss = { viewModel.setShowPresetsDialog(false) },
      onSelectPreset = { viewModel.loadPreset(it) }
    )
  }

  if (uiState.showClearConfirmDialog) {
    ClearConfirmDialog(
      onConfirm = { viewModel.clearAll() },
      onDismiss = { viewModel.setShowClearConfirmDialog(false) }
    )
  }

  if (uiState.showHelpDialog) {
    HelpDialog(
      onDismiss = { viewModel.setShowHelpDialog(false) }
    )
  }
}
