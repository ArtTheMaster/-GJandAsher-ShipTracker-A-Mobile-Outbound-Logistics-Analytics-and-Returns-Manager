import 'package:flutter/material.dart';

/// ============================================================================
/// 60-30-10 ARCHITECTURAL DESIGN SYSTEM (Color & Spatial Layout Rules)
/// ============================================================================
///
/// 1. THE 60-30-10 COLOR ARCHITECTURE RULE:
///    - 60% Dominant / Base: Foundational neutral canvas (scaffold, canvas,
///      backgrounds). Occupies the vast majority of visual surface to provide
///      tranquility, high readability, and eliminate eye strain.
///    - 30% Secondary / Structural: Cards, sheets, nav bars, inputs, borders,
///      and primary/secondary typography. Organizes content and gives form.
///    - 10% Accent / Focal Point: High-impact accents reserved strictly for
///      primary action buttons (CTAs), active indicators, focal status pills,
///      and critical interactive triggers. Kept to ~10% for high intentionality.
///
/// 2. THE 60-30-10 SPATIAL & LAYOUT RULE (8pt Grid Hierarchy):
///    - 60% Macro Canvas & Breathing Room: Page gutters, outer padding (16-24dp),
///      section gaps (20-28dp), list separation (12-16dp). Ensures screens
///      never feel cramped or claustrophobic.
///    - 30% Structural Layout & Component Dimensions: Standardized heights
///      (Buttons 50dp, Inputs 50dp, AppBar 56dp, Nav 72dp), card internal
///      padding (16-20dp), and header row heights (44-48dp).
///    - 10% Micro Precision Details: Tight micro gaps (4-8dp), badge padding
///      (h: 10dp, v: 4dp), border radii (8dp micro, 12dp elements, 16dp macro),
///      and 1dp structural borders.
/// ============================================================================

class ColorRule603010 {
  // --------------------------------------------------------------------------
  // 60% DOMINANT (Base Canvas & Neutral Backdrop)
  // --------------------------------------------------------------------------
  static const Color lightDominantCanvas = Color(0xFFF8FAFC); // Slate 50
  static const Color lightDominantScaffold = Color(0xFFF1F5F9); // Slate 100
  static const Color lightDominantBase = Color(0xFFFFFFFF);

  static const Color darkDominantCanvas = Color(0xFF090D16); // Deep Obsidian
  static const Color darkDominantScaffold = Color(0xFF0F172A); // Slate 900
  static const Color darkDominantBase = Color(0xFF0B1120);

  // --------------------------------------------------------------------------
  // 30% SECONDARY / STRUCTURAL (Containers, Cards, Typography, Outlines)
  // --------------------------------------------------------------------------
  static const Color lightStructuralCard = Color(0xFFFFFFFF);
  static const Color lightStructuralBorder = Color(0xFFE2E8F0); // Slate 200
  static const Color lightStructuralTextPrimary = Color(0xFF0F172A); // Slate 900
  static const Color lightStructuralTextSecondary = Color(0xFF64748B); // Slate 500
  static const Color lightStructuralInputFill = Color(0xFFF8FAFC);
  static const Color lightStructuralNavBg = Color(0xFFFFFFFF);

  static const Color darkStructuralCard = Color(0xFF1E293B); // Slate 800
  static const Color darkStructuralBorder = Color(0xFF334155); // Slate 700
  static const Color darkStructuralTextPrimary = Color(0xFFF8FAFC); // Slate 50
  static const Color darkStructuralTextSecondary = Color(0xFF94A3B8); // Slate 400
  static const Color darkStructuralInputFill = Color(0xFF0F172A);
  static const Color darkStructuralNavBg = Color(0xFF1E293B);

  // --------------------------------------------------------------------------
  // 10% ACCENT / FOCAL POINTS (Primary CTAs, Active States, Critical Signals)
  // --------------------------------------------------------------------------
  static const Color accentLogisticsBlue = Color(0xFF2563EB); // Electric Royal Blue
  static const Color accentSkyLight = Color(0xFF38BDF8); // Glow highlight
  static const Color accentAmberWarning = Color(0xFFF59E0B); // Amber Alert
  static const Color accentEmeraldSuccess = Color(0xFF10B981); // Emerald Signal
  static const Color accentRoseAlert = Color(0xFFEF4444); // Rose Critical

  static const Color lightAccentPrimary = Color(0xFF1E3A8A); // Deep Navy Primary
  static const Color darkAccentPrimary = Color(0xFF3B82F6); // Vibrant Blue Primary
  static const Color activePillIndicator = Color(0xFFDBEAFE); // Soft blue accent tint
  static const Color activePillIndicatorDark = Color(0xFF1E3A8A);
}

class SpatialRule603010 {
  // --------------------------------------------------------------------------
  // 60% MACRO CANVAS & BREATHING ROOM (Negative Space & Margins)
  // --------------------------------------------------------------------------
  static const double screenMargin = 16.0;
  static const double screenMarginWide = 24.0;
  static const double sectionSpacing = 20.0;
  static const double macroSectionGap = 28.0;
  static const double listSeparatorGap = 12.0;
  static const double cardMarginBottom = 16.0;

  // --------------------------------------------------------------------------
  // 30% STRUCTURAL LAYOUT & COMPONENT SIZING (Architecture)
  // --------------------------------------------------------------------------
  static const double cardPadding = 16.0;
  static const double cardPaddingLarge = 20.0;
  static const double buttonHeight = 50.0;
  static const double inputHeight = 50.0;
  static const double minTouchTarget = 48.0;
  static const double headerRowHeight = 44.0;
  static const double navigationBarHeight = 72.0;

  // --------------------------------------------------------------------------
  // 10% MICRO PRECISION DETAILS (Micro Spacing, Badges, Geometry)
  // --------------------------------------------------------------------------
  static const double microGap = 4.0;
  static const double inlineGap = 8.0;
  static const double iconTextGap = 8.0;
  static const double borderWidth = 1.0;
  static const double borderWidthThick = 2.0;

  static const EdgeInsets badgePadding = EdgeInsets.symmetric(horizontal: 10, vertical: 4);
  static const EdgeInsets chipPadding = EdgeInsets.symmetric(horizontal: 8, vertical: 4);

  // Geometric Hierarchy
  static const double radiusMicro = 6.0;
  static const double radiusElement = 12.0;
  static const double radiusCard = 16.0;
  static const double radiusSheet = 24.0;
}
