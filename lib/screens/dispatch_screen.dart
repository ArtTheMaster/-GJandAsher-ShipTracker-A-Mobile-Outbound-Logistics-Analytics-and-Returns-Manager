import 'package:flutter/material.dart';
import '../controllers/ship_tracker_controller.dart';
import '../models/models.dart';

class DispatchScreen extends StatefulWidget {
  final ShipTrackerController controller;

  const DispatchScreen({super.key, required this.controller});

  @override
  State<DispatchScreen> createState() => _DispatchScreenState();
}

class _DispatchScreenState extends State<DispatchScreen> {
  CourierType _selectedCourier = CourierType.jtExpress;
  final _riderNameController = TextEditingController();
  final _notesController = TextEditingController();
  final Set<String> _selectedShipmentIds = {};

  @override
  void dispose() {
    _riderNameController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  void _dispatch() {
    widget.controller.createDispatchHandover(
      courier: _selectedCourier,
      riderName: _riderNameController.text,
      selectedShipmentIds: _selectedShipmentIds.toList(),
      notes: _notesController.text,
      callback: (success, msg) {
        if (success) {
          setState(() {
            _selectedShipmentIds.clear();
            _riderNameController.clear();
            _notesController.clear();
          });
        }
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(msg),
            backgroundColor: success ? const Color(0xFF059669) : const Color(0xFFDC2626),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final user = widget.controller.currentUser;
    final canDispatch = user?.role == UserRole.owner || (user?.canDispatch ?? false);

    return Scaffold(
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Permission Banner
            if (!canDispatch)
              Container(
                margin: const EdgeInsets.only(bottom: 16),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFFFEE2E2),
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: const Color(0xFFFCA5A5)),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.gpp_bad_outlined, color: Color(0xFFDC2626)),
                    SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Dispatch privilege restricted. Ask Owner Nolan to enable dispatch on your staff account.',
                        style: TextStyle(color: Color(0xFFB91C1C), fontSize: 12),
                      ),
                    ),
                  ],
                ),
              ),

            // Handover Generator Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Row(
                      children: [
                        Icon(Icons.local_shipping, color: Color(0xFF1E3A8A)),
                        SizedBox(width: 8),
                        Text(
                          'Batch Handover Manifest',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    DropdownButtonFormField<CourierType>(
                      value: _selectedCourier,
                      decoration: const InputDecoration(labelText: 'Handover Courier Partner'),
                      items: CourierType.values.map((c) {
                        return DropdownMenuItem(value: c, child: Text(c.displayName));
                      }).toList>,
                      onChanged: (val) {
                        setState(() {
                          _selectedCourier = val!;
                          _selectedShipmentIds.clear();
                        });
                      },
                    ),
                    const SizedBox(height: 14),
                    TextField(
                      controller: _riderNameController,
                      decoration: const InputDecoration(
                        labelText: 'Courier Rider Name & Plate #',
                        hintText: 'e.g. Mark Gonzales (Plate NA-4921)',
                        prefixIcon: Icon(Icons.two_wheeler_outlined),
                      ),
                    ),
                    const SizedBox(height: 14),
                    TextField(
                      controller: _notesController,
                      decoration: const InputDecoration(
                        labelText: 'Handover Manifest Notes',
                        hintText: 'e.g. 1 Sack, sealed handover seal #9910',
                        prefixIcon: Icon(Icons.notes_outlined),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Prepared Parcels Selector
            AnimatedBuilder(
              animation: widget.controller,
              builder: (context, _) {
                // Filter prepared or scanned parcels for selected courier
                final eligible = widget.controller.shipments
                    .where((s) => (s.status == ShipmentStatus.scanned || s.status == ShipmentStatus.prepared) && s.courier == _selectedCourier)
                    .toList();

                return Card(
                  child: Padding(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Text(
                              'Available Parcels (${eligible.length})',
                              style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                            ),
                            const Spacer(),
                            if (eligible.isNotEmpty)
                              TextButton(
                                onPressed: () {
                                  setState(() {
                                    if (_selectedShipmentIds.length == eligible.length) {
                                      _selectedShipmentIds.clear();
                                    } else {
                                      _selectedShipmentIds.addAll(eligible.map((e) => e.id));
                                    }
                                  });
                                },
                                child: Text(
                                  _selectedShipmentIds.length == eligible.length ? 'Deselect All' : 'Select All',
                                ),
                              ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        if (eligible.isEmpty)
                          Padding(
                            padding: const EdgeInsets.symmetric(vertical: 20),
                            child: Center(
                              child: Text(
                                'No scanned parcels pending for ${_selectedCourier.displayName}.',
                                style: const TextStyle(color: Color(0xFF64748B), fontSize: 13),
                              ),
                            ),
                          )
                        else
                          ...eligible.map((item) {
                            final isChecked = _selectedShipmentIds.contains(item.id);
                            return CheckboxListTile(
                              value: isChecked,
                              contentPadding: EdgeInsets.zero,
                              title: Text(item.trackingNumber, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                              subtitle: Text('${item.recipientName} • ${item.platform.displayName}'),
                              onChanged: (val) {
                                setState(() {
                                  if (val == true) {
                                    _selectedShipmentIds.add(item.id);
                                  } else {
                                    _selectedShipmentIds.remove(item.id);
                                  }
                                });
                              },
                            );
                          }),
                        const Divider(height: 24),
                        ElevatedButton.icon(
                          onPressed: canDispatch && _selectedShipmentIds.isNotEmpty ? _dispatch : null,
                          icon: const Icon(Icons.send_rounded),
                          label: Text('Generate Batch Handover (${_selectedShipmentIds.length} Parcels)'),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),

            const SizedBox(height: 20),

            // Dispatched Batches History
            const Text(
              'RECENT HANDOVER BATCHES',
              style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFF64748B), letterSpacing: 1.1),
            ),
            const SizedBox(height: 8),
            AnimatedBuilder(
              animation: widget.controller,
              builder: (context, _) {
                final batches = widget.controller.batches;
                if (batches.isEmpty) {
                  return const Text('No batches created yet.', style: TextStyle(color: Colors.grey, fontSize: 13));
                }
                return ListView.separated(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: batches.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 8),
                  itemBuilder: (context, index) {
                    final b = batches[index];
                    return Card(
                      child: ListTile(
                        leading: CircleAvatar(
                          backgroundColor: b.courier.badgeColor.withOpacity(0.12),
                          child: Icon(Icons.local_shipping, color: b.courier.badgeColor, size: 20),
                        ),
                        title: Text(b.batchNumber, style: const TextStyle(fontWeight: FontWeight.bold)),
                        subtitle: Text('${b.courier.displayName} • Rider: ${b.courierRiderName}\nDispatched by ${b.dispatchedByName}'),
                        trailing: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                          decoration: BoxDecoration(
                            color: const Color(0xFF10B981).withOpacity(0.1),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(
                            '${b.totalParcels} Parcels',
                            style: const TextStyle(color: Color(0xFF059669), fontWeight: FontWeight.bold, fontSize: 12),
                          ),
                        ),
                      ),
                    );
                  },
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
