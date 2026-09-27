import 'package:flutter/material.dart';
import '../controllers/ship_tracker_controller.dart';
import '../models/models.dart';
import '../theme/app_theme.dart';

class ReturnsScreen extends StatelessWidget {
  final ShipTrackerController controller;

  const ReturnsScreen({super.key, required this.controller});

  void _openLogReturnDialog(BuildContext context) {
    final user = controller.currentUser;
    if (user?.role == UserRole.staff && !user!.canHandleReturns) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('🔒 Permission Denied: Your staff account cannot log returns. Consult Owner Nolan.'),
          backgroundColor: Color(0xFFDC2626),
        ),
      );
      return;
    }

    showDialog(
      context: context,
      builder: (ctx) => _LogReturnDialog(controller: controller),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isOwner = controller.currentUser?.role == UserRole.owner;

    return Scaffold(
      body: AnimatedBuilder(
        animation: controller,
        builder: (context, _) {
          final list = controller.returns;

          if (list.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.assignment_return_outlined, size: 56, color: Colors.grey[400]),
                  const SizedBox(height: 12),
                  const Text('No Returns Recorded', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 6),
                  const Text('Tap "Log Return" to record RTS or customer rejected parcels.', style: TextStyle(color: Color(0xFF64748B))),
                ],
              ),
            );
          }

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: list.length,
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final ret = list[index];

              return Card(
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
                              color: ret.refundStatus.color.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              ret.refundStatus.label.toUpperCase(),
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                                color: ret.refundStatus.color,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: ret.itemCondition.color.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              ret.itemCondition.label,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                                color: ret.itemCondition.color,
                              ),
                            ),
                          ),
                          const Spacer(),
                          Text(
                            ret.courier.shortName,
                            style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: ret.courier.badgeColor),
                          ),
                        ],
                      ),
                      const SizedBox(height: 10),
                      Text(
                        ret.trackingNumber,
                        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Reason: ${ret.reason.label}',
                        style: const TextStyle(fontSize: 13, color: Color(0xFF1E293B), fontWeight: FontWeight.w500),
                      ),
                      if (ret.notes.isNotEmpty) ...[
                        const SizedBox(height: 4),
                        Text(
                          'Inspection Notes: ${ret.notes}',
                          style: const TextStyle(fontSize: 12, color: Color(0xFF64748B)),
                        ),
                      ],
                      const SizedBox(height: 6),
                      Text(
                        'Logged by ${ret.loggedByName}${ret.approvedByName != null ? ' • Approved by ${ret.approvedByName}' : ''}',
                        style: const TextStyle(fontSize: 11, color: Color(0xFF94A3B8)),
                      ),

                      // Owner Approval Actions
                      if (isOwner && ret.refundStatus == RefundStatus.pendingApproval) ...[
                        const Divider(height: 20),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            TextButton.icon(
                              onPressed: () => controller.approveRefundStatus(ret, RefundStatus.deniedDisputed),
                              icon: const Icon(Icons.block, size: 16, color: Colors.red),
                              label: const Text('Dispute Refund', style: TextStyle(color: Colors.red)),
                            ),
                            const SizedBox(width: 8),
                            ElevatedButton.icon(
                              onPressed: () => controller.approveRefundStatus(ret, RefundStatus.processedRefunded),
                              icon: const Icon(Icons.check, size: 16),
                              label: const Text('Approve Refund'),
                              style: ElevatedButton.styleFrom(
                                backgroundColor: const Color(0xFF059669),
                                minimumSize: const Size(120, 38),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _openLogReturnDialog(context),
        icon: const Icon(Icons.assignment_return),
        label: const Text('Log Return'),
        backgroundColor: const Color(0xFF1E3A8A),
        foregroundColor: Colors.white,
      ),
    );
  }
}

class _LogReturnDialog extends StatefulWidget {
  final ShipTrackerController controller;

  const _LogReturnDialog({required this.controller});

  @override
  State<_LogReturnDialog> createState() => _LogReturnDialogState();
}

class _LogReturnDialogState extends State<_LogReturnDialog> {
  Shipment? _selectedShipment;
  ReturnReason _reason = ReturnReason.customerRejectedCod;
  ItemCondition _condition = ItemCondition.goodResalable;
  final _notesController = TextEditingController();

  @override
  void dispose() {
    _notesController.dispose();
    super.dispose();
  }

  void _submit() {
    if (_selectedShipment == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a shipment to return.')),
      );
      return;
    }

    widget.controller.logParcelReturn(
      shipment: _selectedShipment!,
      reason: _reason,
      condition: _condition,
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
    final eligible = widget.controller.shipments.where((s) => s.status != ShipmentStatus.returned).toList();

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
              const Text('Log Customer Return / RTS', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              DropdownButtonFormField<Shipment>(
                value: _selectedShipment,
                isExpanded: true,
                decoration: const InputDecoration(labelText: 'Select Parcel'),
                hint: const Text('Choose tracking number...'),
                items: eligible.map((s) {
                  return DropdownMenuItem(
                    value: s,
                    child: Text('${s.trackingNumber} (${s.platform.displayName})'),
                  );
                }).toList(),
                onChanged: (val) => setState(() => _selectedShipment = val),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<ReturnReason>(
                value: _reason,
                decoration: const InputDecoration(labelText: 'Return Reason'),
                items: ReturnReason.values.map((r) {
                  return DropdownMenuItem(value: r, child: Text(r.label));
                }).toList(),
                onChanged: (val) => setState(() => _reason = val!),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<ItemCondition>(
                value: _condition,
                decoration: const InputDecoration(labelText: 'Physical Inspection Condition'),
                items: ItemCondition.values.map((c) {
                  return DropdownMenuItem(value: c, child: Text(c.label));
                }).toList(),
                onChanged: (val) => setState(() => _condition = val!),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _notesController,
                decoration: const InputDecoration(
                  labelText: 'Inspection Notes',
                  hintText: 'e.g. Package unopened, item in pristine condition',
                ),
              ),
              const SizedBox(height: 20),
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancel')),
                  const SizedBox(width: 8),
                  ElevatedButton(onPressed: _submit, child: const Text('Record Return')),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
