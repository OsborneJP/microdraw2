package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Standard chemical elements supported in microDraw.
 * Simple 8th-grade friendly element definitions with standard valence.
 */
enum class ElementType(
  val symbol: String,
  val commonName: String,
  val atomicWeight: Double,
  val standardValence: Int,
  val displayColor: Color
) {
  C("C", "Carbon", 12.011, 4, AtomCarbon),
  H("H", "Hydrogen", 1.008, 1, AtomHydrogen),
  O("O", "Oxygen", 15.999, 2, AtomOxygen),
  N("N", "Nitrogen", 14.007, 3, AtomNitrogen),
  Cl("Cl", "Chlorine", 35.453, 1, AtomChlorine),
  S("S", "Sulfur", 32.065, 2, AtomSulfur),
  P("P", "Phosphorus", 30.974, 3, AtomPhosphorus),
  Br("Br", "Bromine", 79.904, 1, AtomBromine),
  F("F", "Fluorine", 18.998, 1, AtomFluorine),
  I("I", "Iodine", 126.904, 1, AtomIodine),
  Na("Na", "Sodium", 22.990, 1, AtomSodium),
  Ca("Ca", "Calcium", 40.078, 2, AtomCalcium)
}

/**
 * Bond types between atoms in 2D chemical drawing.
 */
enum class BondType(
  val displayName: String,
  val symbolDescription: String,
  val bondOrder: Int
) {
  SINGLE("Single", "One shared pair (-)", 1),
  DOUBLE("Double", "Two shared pairs (=)", 2),
  TRIPLE("Triple", "Three shared pairs (≡)", 3),
  WEDGE("Wedge", "Pointing towards you", 1),
  DASH("Dash", "Pointing away from you", 1)
}

/**
 * Position where attached hydrogen atoms appear around the parent atom.
 */
enum class HydrogenLocation(val displayName: String) {
  AUTO("Auto (Best side)"),
  RIGHT("Right (e.g. CH₃)"),
  LEFT("Left (e.g. H₃C)"),
  TOP("Above"),
  BOTTOM("Below"),
  NONE("Hidden / None")
}

/**
 * Represents a single atom in the drawn molecule.
 */
data class Atom(
  val id: Int,
  val element: ElementType,
  val x: Float,
  val y: Float,
  val charge: Int = 0,
  val explicitHydrogens: Int? = null,
  val hydrogenLocation: HydrogenLocation = HydrogenLocation.AUTO
)

/**
 * Represents a chemical bond connecting two atoms.
 */
data class Bond(
  val id: Int,
  val fromAtomId: Int,
  val toAtomId: Int,
  val type: BondType = BondType.SINGLE
)

/**
 * Complete drawn chemical structure with chemical formula calculation.
 */
data class Molecule(
  val atoms: List<Atom> = emptyList(),
  val bonds: List<Bond> = emptyList()
) {
  /**
   * Calculates the number of implicit hydrogens on a given atom.
   * Based on standard chemical valence and current bonds.
   */
  fun calculateImplicitHydrogens(atom: Atom): Int {
    if (atom.explicitHydrogens != null) {
      return atom.explicitHydrogens
    }
    if (atom.element == ElementType.H) return 0

    // Sum bond orders connected to this atom
    var connectedBondOrder = 0
    for (bond in bonds) {
      if (bond.fromAtomId == atom.id || bond.toAtomId == atom.id) {
        connectedBondOrder += bond.type.bondOrder
      }
    }
    val targetValence = atom.element.standardValence + atom.charge
    val neededH = targetValence - connectedBondOrder
    return if (neededH > 0) neededH else 0
  }

  /**
   * Formats the molecular formula using Hill notation (Carbon first, Hydrogen second, then alphabetical).
   */
  fun calculateFormula(): String {
    if (atoms.isEmpty()) return "Empty Canvas"

    val counts = mutableMapOf<String, Int>()
    var totalHydrogens = 0

    for (atom in atoms) {
      if (atom.element == ElementType.H) {
        totalHydrogens += 1
      } else {
        counts[atom.element.symbol] = (counts[atom.element.symbol] ?: 0) + 1
        totalHydrogens += calculateImplicitHydrogens(atom)
      }
    }

    if (totalHydrogens > 0) {
      counts["H"] = totalHydrogens
    }

    val sb = StringBuilder()
    // Hill system: C first if present
    if (counts.containsKey("C")) {
      sb.append("C")
      val count = counts["C"]!!
      if (count > 1) sb.append(toSubscript(count))
      counts.remove("C")

      if (counts.containsKey("H")) {
        sb.append("H")
        val hCount = counts["H"]!!
        if (hCount > 1) sb.append(toSubscript(hCount))
        counts.remove("H")
      }
    }

    // Remaining elements in alphabetical order
    counts.keys.sorted().forEach { sym ->
      val count = counts[sym]!!
      sb.append(sym)
      if (count > 1) sb.append(toSubscript(count))
    }

    // Add net charge if non-zero
    val totalCharge = atoms.sumOf { it.charge }
    if (totalCharge != 0) {
      sb.append(if (totalCharge > 0) "⁺" else "⁻")
      if (kotlin.math.abs(totalCharge) > 1) {
        sb.append(toSuperscript(kotlin.math.abs(totalCharge)))
      }
    }

    return sb.toString()
  }

  /**
   * Computes standard formula weight from IUPAC atomic weights.
   */
  fun calculateMolecularWeight(): Double {
    var weight = 0.0
    for (atom in atoms) {
      weight += atom.element.atomicWeight
      weight += calculateImplicitHydrogens(atom) * ElementType.H.atomicWeight
    }
    return weight
  }

  private fun toSubscript(num: Int): String {
    val subDigits = mapOf(
      '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
      '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉'
    )
    return num.toString().map { subDigits[it] ?: it }.joinToString("")
  }

  private fun toSuperscript(num: Int): String {
    val superDigits = mapOf(
      '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
      '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹'
    )
    return num.toString().map { superDigits[it] ?: it }.joinToString("")
  }
}

/**
 * Chemical properties of the drawn molecule.
 * As requested: Uses placeholder "TODO" values marked with TODO comments.
 * Does not invent statistics.
 */
data class ChemicalProperties(
  val formulaWeight: String,
  // TODO: Replace placeholder chemical properties data with verified chemical database source
  val boilingPoint: String = "TODO",
  // TODO: Replace placeholder chemical properties data with verified chemical database source
  val meltingPoint: String = "TODO",
  // TODO: Replace placeholder chemical properties data with verified chemical database source
  val waterSolubility: String = "TODO",
  // TODO: Replace placeholder chemical properties data with verified chemical database source
  val safeWaterLimitEPA: String = "TODO",
  // TODO: Replace placeholder chemical properties data with verified chemical database source
  val density: String = "TODO",
  // TODO: Replace placeholder chemical properties data with verified chemical database source
  val acidityPka: String = "TODO",
  // Contextual relevance note for students analyzing water samples
  val waterContextNote: String = "Relevant in testing environmental water samples."
)

/**
 * Student's water test metadata: County & Date of last test.
 */
data class WaterSampleInfo(
  val county: String = "Travis County",
  val testDate: String = "October 24, 2026",
  val notes: String = "University environmental chemistry field lab sample."
)

val SampleCounties = listOf(
  "Travis County",
  "Wake County",
  "Hennepin County",
  "Allegheny County",
  "Cook County",
  "King County",
  "Orange County",
  "Fairfax County",
  "Miami-Dade County",
  "Maricopa County",
  "Harris County",
  "Boulder County",
  "Other (Enter Custom)"
)
