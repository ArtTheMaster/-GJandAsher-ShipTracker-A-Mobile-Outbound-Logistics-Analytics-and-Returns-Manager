import 'package:flutter/material.dart';
import '../controllers/ship_tracker_controller.dart';
import '../models/models.dart';

class DashboardScreen extends StatelessWidget {
  final ShipTrackerController controller;

  const DashboardScreen({super.key, required this.controller});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: AnimatedBuilder(
        animation: controller,
        builder: (context, _) {
          final shipments = controller.shipments;
          final total = shipments.length;
          final dispatched = shipments.where((s) => s.status == ShipmentStatus.dispatched || s.status == ShipmentStatus.delivered).length;
          final prepared = shipments.where((s) => s.status == ShipmentStatus.prepared || s.status == ShipmentStatus.scanned).length;
          final returnsCount = controller.returns.length;
          final returnRate = total > 0 ? ((returnsCount / total) * 100).toStringAsFixed(1) : '0.0';

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Owner Banner
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFF1E3A8A), Color(0xFF0284C7)],
                    ),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Row(
                    children: [
                      const CircleAvatar(
                        radius: 24,
                        backgroundColor: Colors.white24,
                        child: Icon(Icons.analytics_outlined, color: Colors.white, size: 28),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Executive Logistics Analytics',
                              style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              'Authorized for Nolan Caparros (Owner)',
                              style: TextStyle(color: Colors.white.withOpacity(0.8), fontSize: 12),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 16),

                // High-level KPI Grid
                Row(
                  children: [
                    Expanded(
                      child: _MetricCard(
                        title: 'Total Parcels',
                        value: '$total',
                        color: const Color(0xFF1E3A8A),
                        icon: Icons.inventory_2_outlined,
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: _MetricCard(
                        title: 'Dispatched',
                        value: '$dispatched',
                        color: const Color(0xFF059669),
                        icon: Icons.local_shipping_outlined,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: _MetricCard(
                        title: 'Ready In Hub',
                        value: '$prepared',
                        color: const Color(0xFF8B5CF6),
                        icon: Icons.access_time_outlined,
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: _MetricCard(
                        title: 'Return Rate',
                        value: '$returnRate%',
                        color: const Color(0xFFEF4444),
                        icon: Icons.assignment_return_outlined,
                      ),
                    ),
                  ],
                ),

                const SizedBox(height: 20),

                // Platform Distribution
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Platform Volume Share',
                          style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 14),
                        ...PlatformType.values.map((p) {
                          final count = shipments.where((s) => s.platform == p).length;
                          final pct = total > 0 ? (count / total) : 0.0;
                          return Padding(
                            padding: const EdgeInsets.only(bottom: 10),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Icon(p.icon, size: 14, color: p.brandColor),
                                    const SizedBox(width: 6),
                                    Text(p.displayName, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                                    const Spacer(),
                                    Text('$count ($pct)', style: TextStyle(fontSize: 12, color: Theme.of(context).colorScheme.onSurfaceVariant)),
                                  ],
                                ),
                                const SizedBox(height: 4),
                                LinearProgressIndicator(
                                  value: pct.toDouble(),
                                  backgroundColor: Theme.of(context).colorScheme.surfaceVariant,
                                  color: p.brandColor,
                                  minHeight: 6,
                                  borderRadius: BorderRadius.circular(4),
                                ),
                              ],
                            ),
                          );
                        }),
                      ],
                    ),
                  ),
                ),

                const SizedBox(height: 14),

                // Courier Distribution
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Courier Dispatch Breakdown',
                          style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 14),
                        ...CourierType.values.map((c) {
                          final count = shipments.where((s) => s.courier == c).length;
                          final pct = total > 0 ? (count / total) : 0.0;
                          return Padding(
                            padding: const EdgeInsets.only(bottom: 10),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Text(c.displayName, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                                    const Spacer(),
                                    Text('$count (${(pct * 100).toStringAsFixed(0)}%)', style: const TextStyle(fontSize: 12, color: Color(0xFF64748B))),
                                  ],
                                ),
                                const SizedBox(height: 4),
                                LinearProgressIndicator(
                                  value: pct.toDouble(),
                                  backgroundColor: Colors.grey[200],
                                  color: c.badgeColor,
                                  minHeight: 6,
                                  borderRadius: BorderRadius.circular(4),
                                ),
                              ],
                            ),
                          );
                        }),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _MetricCard extends StatelessWidget {
  final String title;
  final String value;
  final Color color;
  final IconData icon;

  const _MetricCard({
    required this.title,
    required this.value,
    required this.color,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(icon, color: color, size: 20),
                const Spacer(),
                Container(
                  width: 8,
                  height: 8,
                  decoration: BoxDecoration(shape: BoxShape.circle, color: color),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              value,
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: color),
            ),
            const SizedBox(height: 2),
            Text(
              title,
              style: TextStyle(fontSize: 12, color: Theme.of(context).colorScheme.onSurfaceVariant, fontWeight: FontWeight.w500),
            ),
          ],
        ),
      ),
    );
  }
}
