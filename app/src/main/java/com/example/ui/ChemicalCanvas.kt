package com.example.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.DrawingUiState
import com.example.viewmodel.ToolMode
import kotlin.math.*

private const val ATOM_RADIUS_DP = 22f
private const val TOUCH_TARGET_RADIUS_DP = 32f

@Composable
fun ChemicalCanvas(
  uiState: DrawingUiState,
  onAddAtom: (Float, Float) -> Unit,
  onAddBondBetween: (Int, Int) -> Unit,
  onAddBondAndNewAtom: (Int, Float, Float) -> Unit,
  onSelectAtom: (Int?) -> Unit,
  onSelectBond: (Int?) -> Unit,
  onMoveAtom: (Int, Float, Float) -> Unit,
  onCycleBond: (Int) -> Unit,
  onDeleteAtom: (Int) -> Unit,
  onDeleteBond: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val density = LocalDensity.current
  val atomRadiusPx = with(density) { ATOM_RADIUS_DP.dp.toPx() }
  val touchRadiusPx = with(density) { TOUCH_TARGET_RADIUS_DP.dp.toPx() }

  // Drag interaction state
  var draggedAtomId by remember { mutableStateOf<Int?>(null) }
  var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }
  var isDraggingBond by remember { mutableStateOf(false) }

  val bondLineColor = if (MaterialTheme.colorScheme.background == DarkCanvasBg) BondLineColorDark else BondLineColor
  val gridColor = if (MaterialTheme.colorScheme.background == DarkCanvasBg) Color(0x22FFFFFF) else Color(0x18000000)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .pointerInput(uiState.molecule, uiState.activeToolMode) {
        detectTapGestures { tapOffset ->
          // Check if tapped an atom
          val tappedAtom = uiState.molecule.atoms.find { atom ->
            val dist = hypot(atom.x - tapOffset.x, atom.y - tapOffset.y)
            dist <= touchRadiusPx
          }

          if (tappedAtom != null) {
            when (uiState.activeToolMode) {
              ToolMode.ERASE -> onDeleteAtom(tappedAtom.id)
              ToolMode.DRAW -> {
                if (uiState.selectedAtomId != null && uiState.selectedAtomId != tappedAtom.id) {
                  onAddBondBetween(uiState.selectedAtomId, tappedAtom.id)
                } else {
                  onSelectAtom(tappedAtom.id)
                }
              }
              ToolMode.MOVE -> onSelectAtom(tappedAtom.id)
            }
            return@detectTapGestures
          }

          // Check if tapped a bond
          val tappedBond = uiState.molecule.bonds.find { bond ->
            val a1 = uiState.molecule.atoms.find { it.id == bond.fromAtomId }
            val a2 = uiState.molecule.atoms.find { it.id == bond.toAtomId }
            if (a1 != null && a2 != null) {
              distanceToSegment(tapOffset, Offset(a1.x, a1.y), Offset(a2.x, a2.y)) <= 24f
            } else false
          }

          if (tappedBond != null) {
            when (uiState.activeToolMode) {
              ToolMode.ERASE -> onDeleteBond(tappedBond.id)
              ToolMode.DRAW -> onCycleBond(tappedBond.id)
              ToolMode.MOVE -> onSelectBond(tappedBond.id)
            }
            return@detectTapGestures
          }

          // Tapped empty space
          when (uiState.activeToolMode) {
            ToolMode.DRAW -> onAddAtom(tapOffset.x, tapOffset.y)
            else -> {
              onSelectAtom(null)
              onSelectBond(null)
            }
          }
        }
      }
      .pointerInput(uiState.molecule, uiState.activeToolMode) {
        detectDragGestures(
          onDragStart = { startOffset ->
            val hitAtom = uiState.molecule.atoms.find { atom ->
              hypot(atom.x - startOffset.x, atom.y - startOffset.y) <= touchRadiusPx
            }
            if (hitAtom != null) {
              draggedAtomId = hitAtom.id
              dragCurrentOffset = startOffset
              isDraggingBond = (uiState.activeToolMode == ToolMode.DRAW)
              onSelectAtom(hitAtom.id)
            } else {
              draggedAtomId = null
              dragCurrentOffset = null
              isDraggingBond = false
            }
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val atomId = draggedAtomId ?: return@detectDragGestures
            val current = dragCurrentOffset ?: return@detectDragGestures
            val newOffset = current + dragAmount
            dragCurrentOffset = newOffset

            if (uiState.activeToolMode == ToolMode.MOVE) {
              onMoveAtom(atomId, newOffset.x, newOffset.y)
            }
          },
          onDragEnd = {
            val startAtomId = draggedAtomId
            val endOffset = dragCurrentOffset
            if (startAtomId != null && endOffset != null && isDraggingBond) {
              val startAtom = uiState.molecule.atoms.find { it.id == startAtomId }
              if (startAtom != null) {
                val dragDistance = hypot(endOffset.x - startAtom.x, endOffset.y - startAtom.y)
                if (dragDistance > 35f) {
                  // Check if dropped onto another atom
                  val targetAtom = uiState.molecule.atoms.find { other ->
                    other.id != startAtomId && hypot(other.x - endOffset.x, other.y - endOffset.y) <= touchRadiusPx
                  }
                  if (targetAtom != null) {
                    onAddBondBetween(startAtomId, targetAtom.id)
                  } else {
                    // Create new atom and connect
                    onAddBondAndNewAtom(startAtomId, endOffset.x, endOffset.y)
                  }
                }
              }
            }
            draggedAtomId = null
            dragCurrentOffset = null
            isDraggingBond = false
          },
          onDragCancel = {
            draggedAtomId = null
            dragCurrentOffset = null
            isDraggingBond = false
          }
        )
      }
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawBackgroundGrid(gridColor)

      // 1. Draw Bonds
      for (bond in uiState.molecule.bonds) {
        val a1 = uiState.molecule.atoms.find { it.id == bond.fromAtomId }
        val a2 = uiState.molecule.atoms.find { it.id == bond.toAtomId }
        if (a1 != null && a2 != null) {
          val isSelected = (bond.id == uiState.selectedBondId)
          drawChemicalBond(
            start = Offset(a1.x, a1.y),
            end = Offset(a2.x, a2.y),
            type = bond.type,
            atomRadius = atomRadiusPx,
            color = if (isSelected) SelectionGlow else bondLineColor,
            isSelected = isSelected
          )
        }
      }

      // Draw active bond preview if dragging
      if (isDraggingBond && draggedAtomId != null && dragCurrentOffset != null) {
        val startAtom = uiState.molecule.atoms.find { it.id == draggedAtomId }
        if (startAtom != null) {
          drawChemicalBond(
            start = Offset(startAtom.x, startAtom.y),
            end = dragCurrentOffset!!,
            type = uiState.selectedBondType,
            atomRadius = atomRadiusPx,
            color = SelectionGlow,
            isSelected = true
          )
        }
      }

      // 2. Draw Atoms
      for (atom in uiState.molecule.atoms) {
        val isSelected = (atom.id == uiState.selectedAtomId)
        val implicitH = uiState.molecule.calculateImplicitHydrogens(atom)

        drawChemicalAtom(
          atom = atom,
          radius = atomRadiusPx,
          implicitHydrogens = implicitH,
          isSelected = isSelected
        )
      }
    }
  }
}

private fun DrawScope.drawBackgroundGrid(gridColor: Color) {
  val step = 40.dp.toPx()
  var x = 0f
  while (x < size.width) {
    var y = 0f
    while (y < size.height) {
      drawCircle(
        color = gridColor,
        radius = 1.5.dp.toPx(),
        center = Offset(x, y)
      )
      y += step
    }
    x += step
  }
}

private fun DrawScope.drawChemicalBond(
  start: Offset,
  end: Offset,
  type: BondType,
  atomRadius: Float,
  color: Color,
  isSelected: Boolean
) {
  val dx = end.x - start.x
  val dy = end.y - start.y
  val totalDist = hypot(dx, dy)
  if (totalDist <= atomRadius * 1.5f) return

  val angle = atan2(dy, dx)
  // Shorten line by atomRadius so it connects at the circle boundary
  val actualStart = Offset(
    start.x + cos(angle) * atomRadius,
    start.y + sin(angle) * atomRadius
  )
  val actualEnd = Offset(
    end.x - cos(angle) * atomRadius,
    end.y - sin(angle) * atomRadius
  )

  val normalX = -sin(angle)
  val normalY = cos(angle)
  val bondSpacing = 5.dp.toPx()
  val strokeWidth = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx()

  when (type) {
    BondType.SINGLE -> {
      drawLine(
        color = color,
        start = actualStart,
        end = actualEnd,
        strokeWidth = strokeWidth
      )
    }

    BondType.DOUBLE -> {
      val offset1 = Offset(normalX * bondSpacing, normalY * bondSpacing)
      val offset2 = Offset(-normalX * bondSpacing, -normalY * bondSpacing)
      drawLine(color = color, start = actualStart + offset1, end = actualEnd + offset1, strokeWidth = strokeWidth)
      drawLine(color = color, start = actualStart + offset2, end = actualEnd + offset2, strokeWidth = strokeWidth)
    }

    BondType.TRIPLE -> {
      val offset = Offset(normalX * (bondSpacing * 1.5f), normalY * (bondSpacing * 1.5f))
      drawLine(color = color, start = actualStart, end = actualEnd, strokeWidth = strokeWidth)
      drawLine(color = color, start = actualStart + offset, end = actualEnd + offset, strokeWidth = strokeWidth)
      drawLine(color = color, start = actualStart - offset, end = actualEnd - offset, strokeWidth = strokeWidth)
    }

    BondType.WEDGE -> {
      // Solid wedge polygon tapering from start to end
      val wedgeEndWidth = 8.dp.toPx()
      val p1 = actualStart
      val p2 = Offset(actualEnd.x + normalX * wedgeEndWidth, actualEnd.y + normalY * wedgeEndWidth)
      val p3 = Offset(actualEnd.x - normalX * wedgeEndWidth, actualEnd.y - normalY * wedgeEndWidth)
      val path = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
      }
      drawPath(path = path, color = color)
    }

    BondType.DASH -> {
      // Series of dashes widening towards actualEnd
      val segments = 7
      for (i in 0..segments) {
        val t = i.toFloat() / segments
        val midX = actualStart.x + (actualEnd.x - actualStart.x) * t
        val midY = actualStart.y + (actualEnd.y - actualStart.y) * t
        val halfW = (t * 8.dp.toPx()).coerceAtLeast(1.5f)
        val d1 = Offset(midX + normalX * halfW, midY + normalY * halfW)
        val d2 = Offset(midX - normalX * halfW, midY - normalY * halfW)
        drawLine(color = color, start = d1, end = d2, strokeWidth = 2.5.dp.toPx())
      }
    }
  }
}

private fun DrawScope.drawChemicalAtom(
  atom: Atom,
  radius: Float,
  implicitHydrogens: Int,
  isSelected: Boolean
) {
  val center = Offset(atom.x, atom.y)

  // Selection ring
  if (isSelected) {
    drawCircle(
      color = SelectionGlow,
      radius = radius + 8.dp.toPx(),
      center = center,
      style = Stroke(width = 3.dp.toPx())
    )
  }

  // Atom circle body
  drawCircle(
    color = atom.element.displayColor,
    radius = radius,
    center = center
  )

  // High contrast white ring around atom
  drawCircle(
    color = Color.White,
    radius = radius,
    center = center,
    style = Stroke(width = 2.dp.toPx())
  )

  // Draw element symbol text using Native Canvas Paint
  val paint = Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 42f
    isAntiAlias = true
    typeface = Typeface.DEFAULT_BOLD
    textAlign = Paint.Align.CENTER
  }

  val textBounds = android.graphics.Rect()
  paint.getTextBounds(atom.element.symbol, 0, atom.element.symbol.length, textBounds)
  val textY = center.y + textBounds.height() / 2f - 2f

  drawContext.canvas.nativeCanvas.drawText(
    atom.element.symbol,
    center.x,
    textY,
    paint
  )

  // Draw attached hydrogens if applicable
  if (implicitHydrogens > 0 && atom.element != ElementType.H && atom.hydrogenLocation != HydrogenLocation.NONE) {
    drawAttachedHydrogens(center, radius, implicitHydrogens, atom.hydrogenLocation)
  }

  // Draw charge badge (+ or -)
  if (atom.charge != 0) {
    drawChargeBadge(center, radius, atom.charge)
  }
}

private fun DrawScope.drawAttachedHydrogens(
  center: Offset,
  radius: Float,
  count: Int,
  location: HydrogenLocation
) {
  val hLabel = if (count > 1) "H$count" else "H"
  val paint = Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 28f
    isAntiAlias = true
    typeface = Typeface.DEFAULT_BOLD
    textAlign = Paint.Align.CENTER
  }

  val pos = when (location) {
    HydrogenLocation.AUTO, HydrogenLocation.RIGHT -> Offset(center.x + radius + 18.dp.toPx(), center.y + 8f)
    HydrogenLocation.LEFT -> Offset(center.x - radius - 18.dp.toPx(), center.y + 8f)
    HydrogenLocation.TOP -> Offset(center.x, center.y - radius - 10.dp.toPx())
    HydrogenLocation.BOTTOM -> Offset(center.x, center.y + radius + 22.dp.toPx())
    HydrogenLocation.NONE -> return
  }

  // Background pill for readability
  drawCircle(
    color = Color(0xCC0F172A),
    radius = 14.dp.toPx(),
    center = Offset(pos.x, pos.y - 8f)
  )

  drawContext.canvas.nativeCanvas.drawText(hLabel, pos.x, pos.y, paint)
}

private fun DrawScope.drawChargeBadge(center: Offset, radius: Float, charge: Int) {
  val badgeCenter = Offset(center.x + radius - 4.dp.toPx(), center.y - radius + 4.dp.toPx())
  drawCircle(
    color = if (charge > 0) Color(0xFFEF4444) else Color(0xFF3B82F6),
    radius = 10.dp.toPx(),
    center = badgeCenter
  )
  drawCircle(
    color = Color.White,
    radius = 10.dp.toPx(),
    center = badgeCenter,
    style = Stroke(width = 1.5.dp.toPx())
  )

  val chargeText = when {
    charge == 1 -> "+"
    charge == -1 -> "-"
    charge > 1 -> "+$charge"
    else -> "$charge"
  }

  val paint = Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 22f
    isAntiAlias = true
    typeface = Typeface.DEFAULT_BOLD
    textAlign = Paint.Align.CENTER
  }
  drawContext.canvas.nativeCanvas.drawText(chargeText, badgeCenter.x, badgeCenter.y + 7f, paint)
}

private fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
  val l2 = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
  if (l2 == 0f) return hypot(p.x - a.x, p.y - a.y)
  val t = (((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2).coerceIn(0f, 1f)
  val projection = Offset(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y))
  return hypot(p.x - projection.x, p.y - projection.y)
}
