import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

class Rule603010Dialog extends StatelessWidget {
  const Rule603010Dialog({super.key});

  static void show(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => const Rule603010Dialog(),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      decoration: BoxDecoration(
        color: isDark ? ColorRule603010.darkStructuralCard : ColorRule603010.lightStructuralCard,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(SpatialRule603010.radiusSheet)),
      ),
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 28),
      child: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  margin: const EdgeInsets.only(bottom: 16),
                  decoration: BoxDecoration(
                    color: isDark ? ColorRule603010.darkStructuralBorder : ColorRule603010.lightStructuralBorder,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),

              // Title Header
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: ColorRule603010.accentLogisticsBlue.withOpacity(0.12),
                      borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
                    ),
                    child: const Icon(
                      Icons.palette_outlined,
                      color: ColorRule603010.accentLogisticsBlue,
                      size: 24,
                    ),
                  ),
                  const SizedBox(width: 12),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'The 60-30-10 Architecture',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                        SizedBox(height: 2),
                        Text(
                          'Color Architecture & Spatial Layout Rules',
                          style: TextStyle(fontSize: 12, color: ColorRule603010.lightStructuralTextSecondary),
                        ),
                      ],
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 20),

              // Visual Proportion Bar
              ClipRRect(
                borderRadius: BorderRadius.circular(SpatialRule603010.radiusMicro),
                child: SizedBox(
                  height: 28,
                  child: Row(
                    children: [
                      Expanded(
                        flex: 60,
                        child: Container(
                          color: isDark ? const Color(0xFF0F172A) : const Color(0xFFF1F5F9),
                          alignment: Alignment.center,
                          child: Text(
                            '60% Dominant',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: isDark ? Colors.white70 : const Color(0xFF334155),
                            ),
                          ),
                        ),
                      ),
                      Expanded(
                        flex: 30,
                        child: Container(
                          color: isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0),
                          alignment: Alignment.center,
                          child: Text(
                            '30% Structural',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: isDark ? Colors.white : const Color(0xFF0F172A),
                            ),
                          ),
                        ),
                      ),
                      Expanded(
                        flex: 10,
                        child: Container(
                          color: ColorRule603010.accentLogisticsBlue,
                          alignment: Alignment.center,
                          child: const Text(
                            '10%',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              const SizedBox(height: 20),

              // Section 1: Color Architecture
              const Text(
                'COLOR ARCHITECTURE BREAKDOWN',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1.1,
                  color: ColorRule603010.lightStructuralTextSecondary,
                ),
              ),
              const SizedBox(height: 10),

              _RuleCard(
                percentage: '60%',
                title: 'Dominant Canvas & Neutrals',
                description: 'Scaffolds, viewports, and primary canvas. Sets a peaceful, clean foundational backdrop with zero visual fatigue.',
                colors: isDark
                    ? const ['#090D16 (Canvas)', '#0F172A (Scaffold)']
                    : const ['#F8FAFC (Canvas)', '#F1F5F9 (Scaffold)'],
                colorHex: isDark ? const Color(0xFF0F172A) : const Color(0xFFF1F5F9),
              ),

              const SizedBox(height: 10),

              _RuleCard(
                percentage: '30%',
                title: 'Secondary Structural Elements',
                description: 'Cards, navigation bars, bottom sheets, search fields, dividers, and typography hierarchy. Organizes content with clear visual depth.',
                colors: isDark
                    ? const ['#1E293B (Containers)', '#334155 (Borders)', '#F8FAFC (Text)']
                    : const ['#FFFFFF (Containers)', '#E2E8F0 (Borders)', '#0F172A (Text)'],
                colorHex: isDark ? const Color(0xFF1E293B) : const Color(0xFFFFFFFF),
              ),

              const SizedBox(height: 10),

              const _RuleCard(
                percentage: '10%',
                title: 'Accent Focal Points & Critical CTAs',
                description: 'Primary action buttons, active navigation pills, critical status badges, and focal interactive triggers. Reserved for ~10% surface.',
                colors: ['#2563EB (Logistics Blue)', '#10B981 (Success)', '#EF4444 (Alert)'],
                colorHex: ColorRule603010.accentLogisticsBlue,
              ),

              const SizedBox(height: 20),

              // Section 2: Spatial & Layout Rule
              const Text(
                'SPATIAL & LAYOUT HIERARCHY (8PT GRID)',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1.1,
                  color: ColorRule603010.lightStructuralTextSecondary,
                ),
              ),
              const SizedBox(height: 10),

              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
                  border: Border.all(
                    color: isDark ? ColorRule603010.darkStructuralBorder : ColorRule603010.lightStructuralBorder,
                  ),
                ),
                child: const Column(
                  children: [
                    _SpatialRow(
                      proportion: '60%',
                      name: 'Macro Canvas & Negative Space',
                      specs: '16-24dp screen padding • 20-28dp section gaps • 12-16dp card gutters',
                    ),
                    Divider(height: 16),
                    _SpatialRow(
                      proportion: '30%',
                      name: 'Structural Component Sizing',
                      specs: '50dp button & input height • 16-20dp card padding • 72dp navigation bar',
                    ),
                    Divider(height: 16),
                    _SpatialRow(
                      proportion: '10%',
                      name: 'Micro Precision Details',
                      specs: '4-8dp icon gaps • 8-12-16dp radius scale • 10x4dp badge padding',
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 20),

              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Close Inspector'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _RuleCard extends StatelessWidget {
  final String percentage;
  final String title;
  final String description;
  final List<String> colors;
  final Color colorHex;

  const _RuleCard({
    required this.percentage,
    required this.title,
    required this.description,
    required this.colors,
    required this.colorHex,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: isDark ? ColorRule603010.darkStructuralCard : ColorRule603010.lightStructuralCard,
        borderRadius: BorderRadius.circular(SpatialRule603010.radiusElement),
        border: Border.all(
          color: isDark ? ColorRule603010.darkStructuralBorder : ColorRule603010.lightStructuralBorder,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 14,
                height: 14,
                decoration: BoxDecoration(
                  color: colorHex,
                  shape: BoxShape.circle,
                  border: Border.all(color: Colors.black26),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                '$percentage $title',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(
            description,
            style: const TextStyle(fontSize: 12, color: ColorRule603010.lightStructuralTextSecondary),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 6,
            runSpacing: 4,
            children: colors.map((c) {
              return Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: isDark ? const Color(0xFF0F172A) : const Color(0xFFF1F5F9),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  c,
                  style: TextStyle(
                    fontSize: 11,
                    fontFamily: 'monospace',
                    fontWeight: FontWeight.w600,
                    color: isDark ? Colors.white70 : const Color(0xFF334155),
                  ),
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }
}

class _SpatialRow extends StatelessWidget {
  final String proportion;
  final String name;
  final String specs;

  const _SpatialRow({
    required this.proportion,
    required this.name,
    required this.specs,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
          decoration: BoxDecoration(
            color: ColorRule603010.accentLogisticsBlue.withOpacity(0.15),
            borderRadius: BorderRadius.circular(4),
          ),
          child: Text(
            proportion,
            style: const TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.bold,
              color: ColorRule603010.accentLogisticsBlue,
            ),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(name, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
              const SizedBox(height: 2),
              Text(
                specs,
                style: const TextStyle(fontSize: 11, color: ColorRule603010.lightStructuralTextSecondary),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
