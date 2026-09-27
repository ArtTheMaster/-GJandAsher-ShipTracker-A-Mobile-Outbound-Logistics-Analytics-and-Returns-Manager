import 'package:flutter/material.dart';
import '../controllers/ship_tracker_controller.dart';
import '../models/models.dart';

class ShipmentsScreen extends StatelessWidget {
  final ShipTrackerController controller;

  const ShipmentsScreen({super.key, required this.controller});

  void _openAddParcelDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => _ManualParcelDialog(controller: controller),
    );
  }

  void _showShipmentDetail(BuildContext context, Shipment shipment) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => _ShipmentDetailSheet(shipment: shipment, controller: controller),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: Column(
        children: [
          // Search & Filter Header
          Container(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
            color: theme.scaffoldBackgroundColor,
            child: Column(
              children: [
                TextField(
                  onChanged: controller.setSearchQuery,
                  decoration: InputDecoration(
                    hintText: 'Search tracking #, recipient, or notes...',
                    prefixIcon: const Icon(Icons.search),
                    suffixIcon: controller.searchQuery.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear),
                            onPressed: () => controller.setSearchQuery(''),
                          )
                        : null,
                  ),
                ),
                const SizedBox(height: 8),
                // Status Filter Chips
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: [
                      FilterChip(
                        label: const Text('All Status'),
                        selected: controller.filterStatus == null,
                        onSelected: (_) => controller.setFilterStatus(null),
                      ),
                      const SizedBox(width: 6),
                      ...ShipmentStatus.values.map((status) {
                        return Padding(
                          padding: const EdgeInsets.only(right: 6),
                          child: FilterChip(
                            label: Text(status.label),
                            selected: controller.filterStatus == status,
                            onSelected: (selected) {
                              controller.setFilterStatus(selected ? status : null);
                            },
                          ),
                        );
                      }),
                    ],
                  ),
                ),
              ],
            ),
          ),

          // Shipments List
          Expanded(
            child: AnimatedBuilder(
              animation: controller,
              builder: (context, _) {
                final list = controller.filteredShipments;

                if (list.isEmpty) {
                  return Center(
                    child: Padding(
                      padding: const EdgeInsets.all(32),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.inventory_2_outlined, size: 56, color: Colors.grey[400]),
                          const SizedBox(height: 12),
                          const Text(
                            'No Parcels Found',
                            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            controller.searchQuery.isNotEmpty
                                ? 'No shipments match "${controller.searchQuery}"'
                                : 'Scan barcode or tap + to log outbound shipments.',
                            textAlign: TextAlign.center,
                            style: TextStyle(color: Colors.grey[600], fontSize: 13),
                          ),
                        ],
                      ),
                    ),
                  );
                }

                return ListView.separated(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  itemCount: list.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 10),
                  itemBuilder: (context, index) {
                    final item = list[index];
                    return InkWell(
                      onTap: () => _showShipmentDetail(context, item),
                      borderRadius: BorderRadius.circular(16),
                      child: Card(
                        margin: EdgeInsets.zero,
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                    decoration: BoxDecoration(
                                      color: item.status.color.withOpacity(0.12),
                                      borderRadius: BorderRadius.circular(6),
                                      border: Border.all(color: item.status.color.withOpacity(0.3)),
                                    ),
                                    child: Text(
                                      item.status.label.toUpperCase(),
                                      style: TextStyle(
                                        fontSize: 11,
                                        fontWeight: FontWeight.bold,
                                        color: item.status.color,
                                      ),
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                    decoration: BoxDecoration(
                                      color: item.platform.brandColor.withOpacity(0.1),
                                      borderRadius: BorderRadius.circular(6),
                                    ),
                                    child: Row(
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        Icon(item.platform.icon, size: 12, color: item.platform.brandColor),
                                        const SizedBox(width: 4),
                                        Text(
                                          item.platform.displayName,
                                          style: TextStyle(
                                            fontSize: 11,
                                            fontWeight: FontWeight.bold,
                                            color: item.platform.brandColor,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  const Spacer(),
                                  Text(
                                    item.courier.shortName,
                                    style: TextStyle(
                                      fontSize: 12,
                                      fontWeight: FontWeight.bold,
                                      color: item.courier.badgeColor,
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 10),
                              Text(
                                item.trackingNumber,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                  letterSpacing: 0.5,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Row(
                                children: [
                                  Icon(Icons.person_outline, size: 14, color: theme.colorScheme.onSurfaceVariant),
                                  const SizedBox(width: 4),
                                  Expanded(
                                    child: Text(
                                      '${item.recipientName} • ${item.recipientAddress}',
                                      style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ),
                                ],
                              ),
                              if (item.notes.isNotEmpty) ...[
                                const SizedBox(height: 6),
                                Container(
                                  padding: const EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                    color: theme.colorScheme.surfaceVariant.withOpacity(0.5),
                                    borderRadius: BorderRadius.circular(6),
                                    border: Border.all(color: theme.colorScheme.outlineVariant),
                                  ),
                                  child: Row(
                                    children: [
                                      Icon(Icons.note_alt_outlined, size: 14, color: theme.colorScheme.primary),
                                      const SizedBox(width: 6),
                                      Expanded(
                                        child: Text(
                                          item.notes,
                                          style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurface),
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openAddParcelDialog(context),
        icon: const Icon(Icons.add),
        label: const Text('New Parcel'),
        backgroundColor: const Color(0xFF1E3A8A),
        foregroundColor: Colors.white,
      ),
    );
  }
}

class _ShipmentDetailSheet extends StatelessWidget {
  final Shipment shipment;
  final ShipTrackerController controller;

  const _ShipmentDetailSheet({required this.shipment, required this.controller});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  shipment.trackingNumber,
                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            'Timeline & Logs:',
            style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.grey[600]),
          ),
          const SizedBox(height: 4),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.grey[100],
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              shipment.timelineLog,
              style: const TextStyle(fontSize: 12, fontFamily: 'monospace'),
            ),
          ),
          const SizedBox(height: 16),
          const Text('Update Status:'),
          const SizedBox(height: 8),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: ShipmentStatus.values.map((s) {
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: ActionChip(
                    label: Text(s.label),
                    backgroundColor: s.color.withOpacity(0.1),
                    side: BorderSide(color: s.color),
                    onPressed: () {
                      controller.advanceShipmentStatus(shipment, s);
                      Navigator.pop(context);
                    },
                  ),
                );
              }).toList(),
            ),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }
}

class _ManualParcelDialog extends StatefulWidget {
  final ShipTrackerController controller;

  const _ManualParcelDialog({required this.controller});

  @override
  State<_ManualParcelDialog> createState() => _ManualParcelDialogState();
}

class _ManualParcelDialogState extends State<_ManualParcelDialog> {
  final _trackingController = TextEditingController();
  final _recipientController = TextEditingController();
  final _phoneController = TextEditingController();
  final _addressController = TextEditingController();
  final _notesController = TextEditingController();

  PlatformType _platform = PlatformType.shopee;
  CourierType _courier = CourierType.jtExpress;
  ShipmentStatus _status = ShipmentStatus.prepared;

  @override
  void dispose() {
    _trackingController.dispose();
    _recipientController.dispose();
    _phoneController.dispose();
    _addressController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  void _save() {
    widget.controller.createManualShipment(
      trackingNumber: _trackingController.text,
      platform: _platform,
      courier: _courier,
      status: _status,
      recipientName: _recipientController.text,
      recipientPhone: _phoneController.text,
      recipientAddress: _addressController.text,
      notes: _notesController.text,
      callback: (success, msg) {
        if (success) {
          Navigator.pop(context);
        }
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 500),
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Text('Add Outbound Parcel', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              TextField(
                controller: _trackingController,
                decoration: const InputDecoration(labelText: 'Tracking Number', hintText: 'e.g. SPXPH09281729'),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<PlatformType>(
                value: _platform,
                decoration: const InputDecoration(labelText: 'Platform'),
                items: PlatformType.values.map((p) {
                  return DropdownMenuItem(value: p, child: Text(p.displayName));
                }).toList(),
                onChanged: (val) => setState(() => _platform = val!),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<CourierType>(
                value: _courier,
                decoration: const InputDecoration(labelText: 'Courier'),
                items: CourierType.values.map((c) {
                  return DropdownMenuItem(value: c, child: Text(c.displayName));
                }).toList(),
                onChanged: (val) => setState(() => _courier = val!),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _recipientController,
                decoration: const InputDecoration(labelText: 'Recipient Name', hintText: 'e.g. Juanita Reyes'),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _notesController,
                decoration: const InputDecoration(labelText: 'Notes', hintText: 'Special instructions'),
              ),
              const SizedBox(height: 20),
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancel')),
                  const SizedBox(width: 8),
                  ElevatedButton(onPressed: _save, child: const Text('Save Parcel')),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
