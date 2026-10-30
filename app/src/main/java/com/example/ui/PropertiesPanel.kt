package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BondType
import com.example.model.ChemicalProperties
import com.example.model.Molecule
import com.example.model.WaterSampleInfo
import com.example.ui.theme.*

@Composable
fun PropertiesPanel(
  molecule: Molecule,
  properties: ChemicalProperties,
  waterSampleInfo: WaterSampleInfo,
  onOpenWaterTestDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.surface)
      .verticalScroll(scrollState)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Prominent Educational Disclaimer (Mandatory Requirement)
    Card(
      colors = CardDefaults.cardColors(containerColor = DisclaimerBg),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
        .testTag("educational_disclaimer_card")
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Warning",
          tint = DisclaimerText,
          modifier = Modifier.size(24.dp)
        )
        Column {
          Text(
            text = "Educational Tool Disclaimer",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DisclaimerText
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "This tool is educational and is not a substitute for a citable source of chemical properties.",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = DisclaimerText,
            lineHeight = 20.sp
          )
        }
      }
    }

    // 2. Student Lab Water Test Context
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("water_test_context_card")
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.WaterDrop,
              contentDescription = "Water Test",
              tint = ScienceBlue,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = "Lab Water Sample",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          FilledTonalButton(
            onClick = onOpenWaterTestDialog,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("edit_water_test_button")
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Change", style = MaterialTheme.typography.labelMedium)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "County",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = waterSampleInfo.county,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Last Test Date",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = waterSampleInfo.testDate,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        if (waterSampleInfo.notes.isNotEmpty()) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = waterSampleInfo.notes,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 3. Molecule Formula & Identity
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("molecule_formula_card")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Chemical Formula",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = molecule.calculateFormula(),
          style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp
          ),
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          textAlign = TextAlign.Center
        )
      }
    }

    // 4. Atom & Bond Counts Breakdown
    Text(
      text = "Structure Breakdown",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      StatCard(
        label = "Total Atoms",
        value = "${molecule.atoms.size}",
        modifier = Modifier.weight(1f)
      )
      StatCard(
        label = "Total Bonds",
        value = "${molecule.bonds.size}",
        modifier = Modifier.weight(1f)
      )
    }

    // Bond Types Detail
    val singleCount = molecule.bonds.count { it.type == BondType.SINGLE }
    val doubleCount = molecule.bonds.count { it.type == BondType.DOUBLE }
    val tripleCount = molecule.bonds.count { it.type == BondType.TRIPLE }
    val wedgeCount = molecule.bonds.count { it.type == BondType.WEDGE }
    val dashCount = molecule.bonds.count { it.type == BondType.DASH }

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Bonds by Type",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Single (-): $singleCount", style = MaterialTheme.typography.bodyMedium)
          Text("Double (=): $doubleCount", style = MaterialTheme.typography.bodyMedium)
          Text("Triple (≡): $tripleCount", style = MaterialTheme.typography.bodyMedium)
        }
        if (wedgeCount > 0 || dashCount > 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Wedge (up): $wedgeCount", style = MaterialTheme.typography.bodyMedium)
            Text("Dash (down): $dashCount", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }

    // 5. Chemical Properties (Marked with TODO placeholder comments as requested)
    Text(
      text = "Chemical Properties",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )

    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("chemical_properties_card")
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PropertyRow(
          label = "Formula Weight",
          value = properties.formulaWeight,
          subtext = "Based on standard atomic weights"
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // TODO: Replace placeholder chemical properties data with verified chemical database source
        PropertyRow(
          label = "Boiling Point",
          value = properties.boilingPoint,
          subtext = "Placeholder data (TODO: add citable source)"
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // TODO: Replace placeholder chemical properties data with verified chemical database source
        PropertyRow(
          label = "Melting Point",
          value = properties.meltingPoint,
          subtext = "Placeholder data (TODO: add citable source)"
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // TODO: Replace placeholder chemical properties data with verified chemical database source
        PropertyRow(
          label = "Water Solubility",
          value = properties.waterSolubility,
          subtext = "Placeholder data (TODO: add citable source)"
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // TODO: Replace placeholder chemical properties data with verified chemical database source
        PropertyRow(
          label = "Safe Drinking Limit (EPA)",
          value = properties.safeWaterLimitEPA,
          subtext = "Placeholder data (TODO: add citable source)"
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // TODO: Replace placeholder chemical properties data with verified chemical database source
        PropertyRow(
          label = "Density",
          value = properties.density,
          subtext = "Placeholder data (TODO: add citable source)"
        )
        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // TODO: Replace placeholder chemical properties data with verified chemical database source
        PropertyRow(
          label = "Acidity (pKa)",
          value = properties.acidityPka,
          subtext = "Placeholder data (TODO: add citable source)"
        )
      }
    }

    // 6. Plain 8th-Grade Student Chemistry Notes (No jargon)
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.School, contentDescription = "Learning", tint = ScienceBlue, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Easy Chemistry Guide",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "• Bonds: Lines show electrons shared between atoms.\n" +
                 "• Hydrogens: Added automatically to fill each atom's outer shell.\n" +
                 "• Water tests: Test dates and county records help monitor local drinking water safety.",
          style = MaterialTheme.typography.bodyMedium,
          lineHeight = 22.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(10.dp),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun PropertyRow(label: String, value: String, subtext: String? = null) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
      )
      if (subtext != null) {
        Text(
          text = subtext,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(
          if (value == "TODO") Color(0xFFFFEDD5) else MaterialTheme.colorScheme.primaryContainer
        )
        .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(
          fontFamily = JetBrainsMono,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        ),
        color = if (value == "TODO") Color(0xFFC2410C) else MaterialTheme.colorScheme.onPrimaryContainer
      )
    }
  }
}
