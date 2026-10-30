package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import com.example.viewmodel.DrawingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("microDraw", appName)
  }

  @Test
  fun `water preset calculates correct formula`() {
    val water = ChemicalPresets.water.molecule
    val formula = water.calculateFormula()
    assertEquals("H₂O", formula)
    assertEquals(18.015, water.calculateMolecularWeight(), 0.05)
  }

  @Test
  fun `viewModel adds atom and tracks state`() {
    val vm = DrawingViewModel()
    vm.clearAll()
    assertTrue(vm.uiState.value.molecule.atoms.isEmpty())

    vm.setSelectedElement(ElementType.C)
    vm.addAtomAt(100f, 100f)
    assertEquals(1, vm.uiState.value.molecule.atoms.size)
    assertEquals("CH₄", vm.uiState.value.molecule.calculateFormula())
  }
}
