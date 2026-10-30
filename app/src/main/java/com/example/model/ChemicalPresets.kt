package com.example.model

/**
 * Pre-configured common chemical structures for chemistry students and water analysis.
 */
data class ChemicalPreset(
  val name: String,
  val commonFormula: String,
  val category: String,
  val description: String,
  val molecule: Molecule
)

object ChemicalPresets {

  val water = ChemicalPreset(
    name = "Water",
    commonFormula = "H₂O",
    category = "Solvent",
    description = "The universal solvent tested in every water sample.",
    molecule = Molecule(
      atoms = listOf(
        Atom(id = 1, element = ElementType.O, x = 300f, y = 250f, charge = 0),
        Atom(id = 2, element = ElementType.H, x = 230f, y = 310f),
        Atom(id = 3, element = ElementType.H, x = 370f, y = 310f)
      ),
      bonds = listOf(
        Bond(id = 1, fromAtomId = 1, toAtomId = 2, type = BondType.SINGLE),
        Bond(id = 2, fromAtomId = 1, toAtomId = 3, type = BondType.SINGLE)
      )
    )
  )

  val nitrate = ChemicalPreset(
    name = "Nitrate Ion",
    commonFormula = "NO₃⁻",
    category = "Water Contaminant",
    description = "Common agricultural fertilizer contaminant tested in well water.",
    molecule = Molecule(
      atoms = listOf(
        Atom(id = 1, element = ElementType.N, x = 300f, y = 250f, charge = 1),
        Atom(id = 2, element = ElementType.O, x = 300f, y = 160f, charge = 0),
        Atom(id = 3, element = ElementType.O, x = 220f, y = 300f, charge = -1),
        Atom(id = 4, element = ElementType.O, x = 380f, y = 300f, charge = -1)
      ),
      bonds = listOf(
        Bond(id = 1, fromAtomId = 1, toAtomId = 2, type = BondType.DOUBLE),
        Bond(id = 2, fromAtomId = 1, toAtomId = 3, type = BondType.SINGLE),
        Bond(id = 3, fromAtomId = 1, toAtomId = 4, type = BondType.SINGLE)
      )
    )
  )

  val ethanol = ChemicalPreset(
    name = "Ethanol",
    commonFormula = "C₂H₅OH",
    category = "Alcohol",
    description = "Simple grain alcohol, often studied in organic chemistry.",
    molecule = Molecule(
      atoms = listOf(
        Atom(id = 1, element = ElementType.C, x = 210f, y = 260f),
        Atom(id = 2, element = ElementType.C, x = 290f, y = 215f),
        Atom(id = 3, element = ElementType.O, x = 370f, y = 260f),
        Atom(id = 4, element = ElementType.H, x = 440f, y = 220f)
      ),
      bonds = listOf(
        Bond(id = 1, fromAtomId = 1, toAtomId = 2, type = BondType.SINGLE),
        Bond(id = 2, fromAtomId = 2, toAtomId = 3, type = BondType.SINGLE),
        Bond(id = 3, fromAtomId = 3, toAtomId = 4, type = BondType.SINGLE)
      )
    )
  )

  val benzene = ChemicalPreset(
    name = "Benzene Ring",
    commonFormula = "C₆H₆",
    category = "Aromatic",
    description = "Classic six-carbon aromatic ring with alternating double bonds.",
    molecule = Molecule(
      atoms = listOf(
        Atom(id = 1, element = ElementType.C, x = 300f, y = 160f),
        Atom(id = 2, element = ElementType.C, x = 370f, y = 200f),
        Atom(id = 3, element = ElementType.C, x = 370f, y = 280f),
        Atom(id = 4, element = ElementType.C, x = 300f, y = 320f),
        Atom(id = 5, element = ElementType.C, x = 230f, y = 280f),
        Atom(id = 6, element = ElementType.C, x = 230f, y = 200f)
      ),
      bonds = listOf(
        Bond(id = 1, fromAtomId = 1, toAtomId = 2, type = BondType.DOUBLE),
        Bond(id = 2, fromAtomId = 2, toAtomId = 3, type = BondType.SINGLE),
        Bond(id = 3, fromAtomId = 3, toAtomId = 4, type = BondType.DOUBLE),
        Bond(id = 4, fromAtomId = 4, toAtomId = 5, type = BondType.SINGLE),
        Bond(id = 5, fromAtomId = 5, toAtomId = 6, type = BondType.DOUBLE),
        Bond(id = 6, fromAtomId = 6, toAtomId = 1, type = BondType.SINGLE)
      )
    )
  )

  val chloroform = ChemicalPreset(
    name = "Chloroform",
    commonFormula = "CHCl₃",
    category = "Water Byproduct",
    description = "Disinfection byproduct formed when chlorine treats water.",
    molecule = Molecule(
      atoms = listOf(
        Atom(id = 1, element = ElementType.C, x = 300f, y = 250f),
        Atom(id = 2, element = ElementType.H, x = 300f, y = 170f),
        Atom(id = 3, element = ElementType.Cl, x = 220f, y = 300f),
        Atom(id = 4, element = ElementType.Cl, x = 380f, y = 300f),
        Atom(id = 5, element = ElementType.Cl, x = 300f, y = 340f)
      ),
      bonds = listOf(
        Bond(id = 1, fromAtomId = 1, toAtomId = 2, type = BondType.SINGLE),
        Bond(id = 2, fromAtomId = 1, toAtomId = 3, type = BondType.SINGLE),
        Bond(id = 3, fromAtomId = 1, toAtomId = 4, type = BondType.SINGLE),
        Bond(id = 4, fromAtomId = 1, toAtomId = 5, type = BondType.SINGLE)
      )
    )
  )

  val allPresets = listOf(water, nitrate, ethanol, benzene, chloroform)
}
