package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.cos
import kotlin.math.sin

enum class ToolMode(val label: String, val description: String) {
  DRAW("Draw", "Tap or drag to add atoms and connect bonds"),
  MOVE("Move", "Drag atoms to reposition structure"),
  ERASE("Erase", "Tap an atom or bond to delete it")
}

data class DrawingUiState(
  val molecule: Molecule = ChemicalPresets.water.molecule,
  val selectedAtomId: Int? = null,
  val selectedBondId: Int? = null,
  val activeToolMode: ToolMode = ToolMode.DRAW,
  val selectedElement: ElementType = ElementType.C,
  val selectedBondType: BondType = BondType.SINGLE,
  val selectedHydrogenLocation: HydrogenLocation = HydrogenLocation.AUTO,
  val waterSampleInfo: WaterSampleInfo = WaterSampleInfo(),
  val chemicalProperties: ChemicalProperties = ChemicalProperties(formulaWeight = "18.02 g/mol"),
  val canUndo: Boolean = false,
  val canRedo: Boolean = false,
  val showWaterTestDialog: Boolean = false,
  val showPresetsDialog: Boolean = false,
  val showClearConfirmDialog: Boolean = false,
  val showHelpDialog: Boolean = false,
  val showPropertiesOnNarrowScreen: Boolean = false
)

class DrawingViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(DrawingUiState())
  val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

  private val undoStack = mutableListOf<Molecule>()
  private val redoStack = mutableListOf<Molecule>()
  private var nextId = 100

  init {
    updateCalculatedProperties(_uiState.value.molecule)
  }

  private fun saveToUndo(current: Molecule) {
    undoStack.add(current)
    if (undoStack.size > 25) undoStack.removeAt(0)
    redoStack.clear()
  }

  fun setToolMode(mode: ToolMode) {
    _uiState.update { it.copy(activeToolMode = mode) }
  }

  fun setSelectedElement(element: ElementType) {
    _uiState.update { state ->
      val updatedState = state.copy(selectedElement = element)
      // If an atom is currently selected, also update its element
      if (state.selectedAtomId != null) {
        val updatedAtoms = state.molecule.atoms.map { atom ->
          if (atom.id == state.selectedAtomId) atom.copy(element = element) else atom
        }
        val newMol = state.molecule.copy(atoms = updatedAtoms)
        saveToUndo(state.molecule)
        updateCalculatedProperties(newMol)
        updatedState.copy(molecule = newMol)
      } else {
        updatedState
      }
    }
  }

  fun setSelectedBondType(type: BondType) {
    _uiState.update { state ->
      val updatedState = state.copy(selectedBondType = type)
      // If a bond is currently selected, update its type
      if (state.selectedBondId != null) {
        val updatedBonds = state.molecule.bonds.map { bond ->
          if (bond.id == state.selectedBondId) bond.copy(type = type) else bond
        }
        val newMol = state.molecule.copy(bonds = updatedBonds)
        saveToUndo(state.molecule)
        updateCalculatedProperties(newMol)
        updatedState.copy(molecule = newMol)
      } else {
        updatedState
      }
    }
  }

  fun setSelectedHydrogenLocation(location: HydrogenLocation) {
    _uiState.update { state ->
      val updatedState = state.copy(selectedHydrogenLocation = location)
      if (state.selectedAtomId != null) {
        val updatedAtoms = state.molecule.atoms.map { atom ->
          if (atom.id == state.selectedAtomId) atom.copy(hydrogenLocation = location) else atom
        }
        val newMol = state.molecule.copy(atoms = updatedAtoms)
        saveToUndo(state.molecule)
        updatedState.copy(molecule = newMol)
      } else {
        updatedState
      }
    }
  }

  fun selectAtom(atomId: Int?) {
    _uiState.update { state ->
      val atom = state.molecule.atoms.find { it.id == atomId }
      state.copy(
        selectedAtomId = atomId,
        selectedBondId = null,
        selectedElement = atom?.element ?: state.selectedElement,
        selectedHydrogenLocation = atom?.hydrogenLocation ?: state.selectedHydrogenLocation
      )
    }
  }

  fun selectBond(bondId: Int?) {
    _uiState.update { state ->
      val bond = state.molecule.bonds.find { it.id == bondId }
      state.copy(
        selectedBondId = bondId,
        selectedAtomId = null,
        selectedBondType = bond?.type ?: state.selectedBondType
      )
    }
  }

  fun addAtomAt(x: Float, y: Float) {
    val state = _uiState.value
    saveToUndo(state.molecule)
    val newAtom = Atom(
      id = ++nextId,
      element = state.selectedElement,
      x = x,
      y = y,
      hydrogenLocation = state.selectedHydrogenLocation
    )
    val newAtoms = state.molecule.atoms + newAtom
    var newBonds = state.molecule.bonds

    // If an atom was already selected, connect to it
    if (state.selectedAtomId != null) {
      val bond = Bond(
        id = ++nextId,
        fromAtomId = state.selectedAtomId,
        toAtomId = newAtom.id,
        type = state.selectedBondType
      )
      newBonds = newBonds + bond
    }

    val newMol = state.molecule.copy(atoms = newAtoms, bonds = newBonds)
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        selectedAtomId = newAtom.id,
        selectedBondId = null,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun addBondBetween(fromAtomId: Int, toAtomId: Int) {
    if (fromAtomId == toAtomId) return
    val state = _uiState.value
    // Check if bond already exists
    val existing = state.molecule.bonds.find {
      (it.fromAtomId == fromAtomId && it.toAtomId == toAtomId) ||
      (it.fromAtomId == toAtomId && it.toAtomId == fromAtomId)
    }
    if (existing != null) {
      // Cycle bond type
      cycleBondType(existing.id)
      return
    }
    saveToUndo(state.molecule)
    val newBond = Bond(
      id = ++nextId,
      fromAtomId = fromAtomId,
      toAtomId = toAtomId,
      type = state.selectedBondType
    )
    val newMol = state.molecule.copy(bonds = state.molecule.bonds + newBond)
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        selectedBondId = newBond.id,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun addBondAndNewAtom(fromAtomId: Int, targetX: Float, targetY: Float) {
    val state = _uiState.value
    saveToUndo(state.molecule)
    val newAtom = Atom(
      id = ++nextId,
      element = state.selectedElement,
      x = targetX,
      y = targetY,
      hydrogenLocation = state.selectedHydrogenLocation
    )
    val newBond = Bond(
      id = ++nextId,
      fromAtomId = fromAtomId,
      toAtomId = newAtom.id,
      type = state.selectedBondType
    )
    val newMol = state.molecule.copy(
      atoms = state.molecule.atoms + newAtom,
      bonds = state.molecule.bonds + newBond
    )
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        selectedAtomId = newAtom.id,
        selectedBondId = null,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun moveAtom(atomId: Int, newX: Float, newY: Float) {
    _uiState.update { state ->
      val updatedAtoms = state.molecule.atoms.map {
        if (it.id == atomId) it.copy(x = newX, y = newY) else it
      }
      state.copy(molecule = state.molecule.copy(atoms = updatedAtoms))
    }
  }

  fun cycleBondType(bondId: Int) {
    val state = _uiState.value
    saveToUndo(state.molecule)
    val updatedBonds = state.molecule.bonds.map { bond ->
      if (bond.id == bondId) {
        val nextType = when (bond.type) {
          BondType.SINGLE -> BondType.DOUBLE
          BondType.DOUBLE -> BondType.TRIPLE
          BondType.TRIPLE -> BondType.WEDGE
          BondType.WEDGE -> BondType.DASH
          BondType.DASH -> BondType.SINGLE
        }
        bond.copy(type = nextType)
      } else bond
    }
    val newMol = state.molecule.copy(bonds = updatedBonds)
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun adjustAtomCharge(atomId: Int, delta: Int) {
    val state = _uiState.value
    saveToUndo(state.molecule)
    val updatedAtoms = state.molecule.atoms.map { atom ->
      if (atom.id == atomId) {
        val newCharge = (atom.charge + delta).coerceIn(-2, 2)
        atom.copy(charge = newCharge)
      } else atom
    }
    val newMol = state.molecule.copy(atoms = updatedAtoms)
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun deleteAtom(atomId: Int) {
    val state = _uiState.value
    saveToUndo(state.molecule)
    val newAtoms = state.molecule.atoms.filter { it.id != atomId }
    val newBonds = state.molecule.bonds.filter { it.fromAtomId != atomId && it.toAtomId != atomId }
    val newMol = state.molecule.copy(atoms = newAtoms, bonds = newBonds)
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        selectedAtomId = null,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun deleteBond(bondId: Int) {
    val state = _uiState.value
    saveToUndo(state.molecule)
    val newBonds = state.molecule.bonds.filter { it.id != bondId }
    val newMol = state.molecule.copy(bonds = newBonds)
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        selectedBondId = null,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun stampRing(centerX: Float, centerY: Float, sides: Int = 6, isAromatic: Boolean = true) {
    val state = _uiState.value
    saveToUndo(state.molecule)
    val radius = 75f
    val newAtoms = mutableListOf<Atom>()
    val newBonds = mutableListOf<Bond>()
    val startAngle = -Math.PI / 2

    for (i in 0 until sides) {
      val angle = startAngle + i * (2 * Math.PI / sides)
      val x = (centerX + radius * cos(angle)).toFloat()
      val y = (centerY + radius * sin(angle)).toFloat()
      val atom = Atom(id = ++nextId, element = ElementType.C, x = x, y = y)
      newAtoms.add(atom)
    }

    for (i in 0 until sides) {
      val nextIdx = (i + 1) % sides
      val type = if (isAromatic && i % 2 == 0) BondType.DOUBLE else BondType.SINGLE
      newBonds.add(
        Bond(
          id = ++nextId,
          fromAtomId = newAtoms[i].id,
          toAtomId = newAtoms[nextIdx].id,
          type = type
        )
      )
    }

    val newMol = state.molecule.copy(
      atoms = state.molecule.atoms + newAtoms,
      bonds = state.molecule.bonds + newBonds
    )
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun clearAll() {
    val state = _uiState.value
    if (state.molecule.atoms.isEmpty()) return
    saveToUndo(state.molecule)
    val newMol = Molecule()
    updateCalculatedProperties(newMol)
    _uiState.update {
      it.copy(
        molecule = newMol,
        selectedAtomId = null,
        selectedBondId = null,
        showClearConfirmDialog = false,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun loadPreset(preset: ChemicalPreset) {
    val state = _uiState.value
    saveToUndo(state.molecule)
    updateCalculatedProperties(preset.molecule)
    _uiState.update {
      it.copy(
        molecule = preset.molecule,
        selectedAtomId = null,
        selectedBondId = null,
        showPresetsDialog = false,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun undo() {
    if (undoStack.isEmpty()) return
    val previous = undoStack.removeAt(undoStack.lastIndex)
    redoStack.add(_uiState.value.molecule)
    updateCalculatedProperties(previous)
    _uiState.update {
      it.copy(
        molecule = previous,
        selectedAtomId = null,
        selectedBondId = null,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun redo() {
    if (redoStack.isEmpty()) return
    val next = redoStack.removeAt(redoStack.lastIndex)
    undoStack.add(_uiState.value.molecule)
    updateCalculatedProperties(next)
    _uiState.update {
      it.copy(
        molecule = next,
        selectedAtomId = null,
        selectedBondId = null,
        canUndo = undoStack.isNotEmpty(),
        canRedo = redoStack.isNotEmpty()
      )
    }
  }

  fun updateWaterSampleInfo(county: String, testDate: String, notes: String) {
    _uiState.update {
      it.copy(
        waterSampleInfo = WaterSampleInfo(county = county, testDate = testDate, notes = notes),
        showWaterTestDialog = false
      )
    }
  }

  fun setShowWaterTestDialog(show: Boolean) {
    _uiState.update { it.copy(showWaterTestDialog = show) }
  }

  fun setShowPresetsDialog(show: Boolean) {
    _uiState.update { it.copy(showPresetsDialog = show) }
  }

  fun setShowClearConfirmDialog(show: Boolean) {
    _uiState.update { it.copy(showClearConfirmDialog = show) }
  }

  fun setShowHelpDialog(show: Boolean) {
    _uiState.update { it.copy(showHelpDialog = show) }
  }

  fun togglePropertiesPanel() {
    _uiState.update { it.copy(showPropertiesOnNarrowScreen = !it.showPropertiesOnNarrowScreen) }
  }

  /**
   * Recalculates chemical properties for the drawn structure.
   * Unsupplied properties are explicitly given "TODO" placeholder values,
   * marked with TODO comments so they can be hooked to actual databases later.
   */
  private fun updateCalculatedProperties(mol: Molecule) {
    val weight = mol.calculateMolecularWeight()
    val weightString = if (mol.atoms.isEmpty()) {
      // TODO: Replace placeholder chemical properties data with verified chemical database source
      "TODO"
    } else {
      String.format("%.2f g/mol", weight)
    }

    val note = when {
      mol.atoms.isEmpty() -> "Draw a structure above to view water sample analysis."
      mol.atoms.any { it.element == ElementType.N && it.element == ElementType.O } ->
        "Nitrogen-oxygen structures are often tested for fertilizer runoff."
      mol.atoms.any { it.element == ElementType.Cl } ->
        "Chlorinated compounds are monitored as municipal disinfection byproducts."
      mol.atoms.size == 3 && mol.atoms.any { it.element == ElementType.O } ->
        "Water molecule (H₂O), the universal solvent for all test samples."
      else -> "Chemical structure drawn for university lab analysis."
    }

    _uiState.update {
      it.copy(
        chemicalProperties = ChemicalProperties(
          formulaWeight = weightString,
          // TODO: Replace placeholder chemical properties data with verified chemical database source
          boilingPoint = "TODO",
          // TODO: Replace placeholder chemical properties data with verified chemical database source
          meltingPoint = "TODO",
          // TODO: Replace placeholder chemical properties data with verified chemical database source
          waterSolubility = "TODO",
          // TODO: Replace placeholder chemical properties data with verified chemical database source
          safeWaterLimitEPA = "TODO",
          // TODO: Replace placeholder chemical properties data with verified chemical database source
          density = "TODO",
          // TODO: Replace placeholder chemical properties data with verified chemical database source
          acidityPka = "TODO",
          waterContextNote = note
        )
      )
    }
  }
}
