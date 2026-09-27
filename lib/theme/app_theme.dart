import 'package:flutter/material.dart';
import 'rule_60_30_10.dart';

export 'rule_60_30_10.dart';

/// AppTheme strictly enforces the 60-30-10 Design Architecture:
/// - 60% Dominant (Canvas, Scaffolds, Base neutral backgrounds)
/// - 30% Structural (Cards, Sheets, NavigationBar, Inputs, Borders, Headers)
/// - 10% Accent (Primary action CTAs, Active indicators, Critical highlights)
class AppTheme {
  static const primaryColor = ColorRule603010.lightAccentPrimary;
  static const secondaryColor = ColorRule603010.accentLogisticsBlue;
  static const accentAmber = ColorRule603010.accentAmberWarning;
  static const successEmerald = ColorRule603010.accentEmeraldSuccess;
  static const errorRose = ColorRule603010.accentRoseAlert;

  // ---------------------------------------------------------------------------
  // LIGHT THEME (60% Light Slate, 30% Crisp White Containers, 10% Vibrant Navy)
  // ---------------------------------------------------------------------------
  static ThemeData lightTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.light,
    colorScheme: const ColorScheme(
      brightness: Brightness.light,
      primary: ColorRule603010.lightAccentPrimary, // 10% Accent
      onPrimary: Colors.white,
      secondary: ColorRule603010.accentLogisticsBlue,
      onSecondary: Colors.white,
      surface: ColorRule603010.lightStructuralCard, // 30% Structural
      onSurface: ColorRule603010.lightStructuralTextPrimary,
      surfaceVariant: ColorRule603010.lightDominantCanvas,
      onSurfaceVariant: ColorRule603010.lightStructuralTextSecondary,
      background: ColorRule603010.lightDominantScaffold, // 60% Dominant
      onBackground: ColorRule603010.lightStructuralTextPrimary,
      error: ColorRule603010.accentRoseAlert,
      onError: Colors.white,
      outline: ColorRule603010.lightStructuralBorder,
      outlineVariant: Color(0xFFE2E8F0),
    ),
    scaffoldBackgroundColor: ColorRule603010.lightDominantScaffold, // 60% Dominant
    canvasColor: ColorRule603010.lightDominantCanvas,

    // 30% Structural App Bar
    appBarTheme: const AppBarTheme(
      backgroundColor: ColorRule603010.lightStructuralCard,
      foregroundColor: ColorRule603010.lightStructuralTextPrimary,
      elevation: 0,
      centerTitle: false,
      surfaceTintColor: Colors.transparent,
    ),

    // 30% Structural Navigation Bar with 10% Accent Active Indicator Pill
    navigationBarTheme: NavigationBarThemeData(
      height: SpatialRule603010.navigationBarHeight,
      backgroundColor: ColorRule603010.lightStructuralNavBg,
      surfaceTintColor: Colors.transparent,
      elevation: 3,
      indicatorColor: ColorRule603010.activePillIndicator, // 10% Accent
      labelTextStyle: MaterialStateProperty.resolveWith((states) {
        if (states.contains(MaterialState.selected)) {
          return const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: ColorRule603010.lightAccentPrimary,
          );
        }
        return const TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w500,
          color: ColorRule603010.lightStructuralTextSecondary,
        );
      }),
      iconTheme: MaterialStateProperty.resolveWith((states) {
        if (states.contains(MaterialState.selected)) {
          return const IconThemeData(color: ColorRule603010.lightAccentPrimary, size: 24);
        }
        return const IconThemeData(color: ColorRule603010.lightStructuralTextSecondary, size: 24);
      }),
    ),

    // 30% Structural Card Architecture
    cardTheme: CardTheme(
      elevation: 0,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusCard),
        side: const BorderSide(
          color: ColorRule603010.lightStructuralBorder,
          width: SpatialRule603010.borderWidth,
        ),
      ),
      color: ColorRule603010.lightStructuralCard,
    ),

    // 30% Structural Input Fields with 10% Focus Accent
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: ColorRule603010.lightStructuralInputFill,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        borderSide: const BorderSide(color: ColorRule603010.lightStructuralBorder),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        borderSide: const BorderSide(color: ColorRule603010.lightStructuralBorder),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        borderSide: const BorderSide(
          color: ColorRule603010.accentLogisticsBlue, // 10% Accent focus
          width: SpatialRule603010.borderWidthThick,
        ),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    ),

    // 10% Accent CTA Action Buttons (50dp structural height)
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        elevation: 0,
        backgroundColor: ColorRule603010.lightAccentPrimary,
        foregroundColor: Colors.white,
        minimumSize: const Size(double.infinity, SpatialRule603010.buttonHeight),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        ),
        textStyle: const TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
      ),
    ),

    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: ColorRule603010.lightAccentPrimary,
        side: const BorderSide(color: ColorRule603010.lightStructuralBorder),
        minimumSize: const Size(0, SpatialRule603010.buttonHeight),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        ),
      ),
    ),

    dividerTheme: const DividerThemeData(
      color: ColorRule603010.lightStructuralBorder,
      thickness: SpatialRule603010.borderWidth,
      space: 1,
    ),
  );

  // ---------------------------------------------------------------------------
  // DARK THEME (60% Deep Obsidian, 30% Slate 800 Containers, 10% Royal Sky Blue)
  // ---------------------------------------------------------------------------
  static ThemeData darkTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    colorScheme: const ColorScheme(
      brightness: Brightness.dark,
      primary: ColorRule603010.darkAccentPrimary, // 10% Accent
      onPrimary: Colors.white,
      secondary: ColorRule603010.accentAmberWarning,
      onSecondary: Colors.black,
      surface: ColorRule603010.darkStructuralCard, // 30% Structural
      onSurface: ColorRule603010.darkStructuralTextPrimary,
      surfaceVariant: ColorRule603010.darkDominantCanvas,
      onSurfaceVariant: ColorRule603010.darkStructuralTextSecondary,
      background: ColorRule603010.darkDominantScaffold, // 60% Dominant
      onBackground: ColorRule603010.darkStructuralTextPrimary,
      error: ColorRule603010.accentRoseAlert,
      onError: Colors.white,
      outline: ColorRule603010.darkStructuralBorder,
      outlineVariant: Color(0xFF334155),
    ),
    scaffoldBackgroundColor: ColorRule603010.darkDominantScaffold, // 60% Dominant
    canvasColor: ColorRule603010.darkDominantCanvas,

    // 30% Structural App Bar
    appBarTheme: const AppBarTheme(
      backgroundColor: ColorRule603010.darkStructuralCard,
      foregroundColor: ColorRule603010.darkStructuralTextPrimary,
      elevation: 0,
      centerTitle: false,
      surfaceTintColor: Colors.transparent,
    ),

    // 30% Structural Navigation Bar with 10% Accent Active Indicator Pill
    navigationBarTheme: NavigationBarThemeData(
      height: SpatialRule603010.navigationBarHeight,
      backgroundColor: ColorRule603010.darkStructuralNavBg,
      surfaceTintColor: Colors.transparent,
      elevation: 3,
      indicatorColor: ColorRule603010.activePillIndicatorDark, // 10% Accent
      labelTextStyle: MaterialStateProperty.resolveWith((states) {
        if (states.contains(MaterialState.selected)) {
          return const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: ColorRule603010.darkAccentPrimary,
          );
        }
        return const TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w500,
          color: ColorRule603010.darkStructuralTextSecondary,
        );
      }),
      iconTheme: MaterialStateProperty.resolveWith((states) {
        if (states.contains(MaterialState.selected)) {
          return const IconThemeData(color: ColorRule603010.darkAccentPrimary, size: 24);
        }
        return const IconThemeData(color: ColorRule603010.darkStructuralTextSecondary, size: 24);
      }),
    ),

    // 30% Structural Card Architecture
    cardTheme: CardTheme(
      elevation: 0,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusCard),
        side: const BorderSide(
          color: ColorRule603010.darkStructuralBorder,
          width: SpatialRule603010.borderWidth,
        ),
      ),
      color: ColorRule603010.darkStructuralCard,
    ),

    // 30% Structural Input Fields with 10% Focus Accent
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: ColorRule603010.darkStructuralInputFill,
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        borderSide: const BorderSide(color: ColorRule603010.darkStructuralBorder),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        borderSide: const BorderSide(color: ColorRule603010.darkStructuralBorder),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        borderSide: const BorderSide(
          color: ColorRule603010.darkAccentPrimary, // 10% Accent focus
          width: SpatialRule603010.borderWidthThick,
        ),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    ),

    // 10% Accent CTA Action Buttons (50dp structural height)
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        elevation: 0,
        backgroundColor: ColorRule603010.darkAccentPrimary,
        foregroundColor: Colors.white,
        minimumSize: const Size(double.infinity, SpatialRule603010.buttonHeight),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        ),
        textStyle: const TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
      ),
    ),

    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: ColorRule603010.darkAccentPrimary,
        side: const BorderSide(color: ColorRule603010.darkStructuralBorder),
        minimumSize: const Size(0, SpatialRule603010.buttonHeight),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        ),
      ),
    ),

    dividerTheme: const DividerThemeData(
      color: ColorRule603010.darkStructuralBorder,
      thickness: SpatialRule603010.borderWidth,
      space: 1,
    ),
  );
}
