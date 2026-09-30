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
    final theme = Theme.of(context);
    final isOwner = controller.currentUser?.role == UserRole.owner;

    return Scaffold(
      body: AnimatedBuilder(
        animation: controller,
        builder: (context, _) {
          final list = controller.returns;
          final pendingRefunds = list.where((r) => r.refundStatus == RefundStatus.pendingApproval).length;
          final resalableCount = list.where((r) => r.itemCondition == ItemCondition.goodResalable).length;

          return CustomScrollView(
            slivers: [
              // Top Summary & Action Header
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      // Header Card with Title and Action
                      Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: theme.colorScheme.surface,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: theme.colorScheme.outlineVariant),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            Row(
                              children: [
                                Container(
                                  padding: const EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFFEF4444).withOpacity(0.12),
                                    borderRadius: BorderRadius.circular(10),
                                  ),
                                  child: const Icon(
                                    Icons.assignment_return_outlined,
                                    color: Color(0xFFEF4444),
                                    size: 22,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'Returns & RTS Hub',
                                        style: TextStyle(
                                          fontSize: 17,
                                          fontWeight: FontWeight.bold,
                                          color: theme.colorScheme.onSurface,
                                        ),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        'Inspection & refund workflows',
                                        style: TextStyle(
                                          fontSize: 12,
                                          color: theme.colorScheme.onSurfaceVariant,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                const SizedBox(width: 8),
                                ElevatedButton.icon(
                                  onPressed: () => _openLogReturnDialog(context),
                                  icon: const Icon(Icons.add, size: 16),
                                  label: const Text('Log Return'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: const Color(0xFFDC2626),
                                    foregroundColor: Colors.white,
                                    minimumSize: const Size(110, 40),
                                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 14),
                            const Divider(height: 1),
                            const SizedBox(height: 14),
                            // KPI row
                            Row(
                              children: [
                                Expanded(
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                                    decoration: BoxDecoration(
                                      color: const Color(0xFFEA580C).withOpacity(0.1),
                                      borderRadius: BorderRadius.circular(10),
                                      border: Border.all(color: const Color(0xFFEA580C).withOpacity(0.2)),
                                    ),
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          'Pending Refund',
                                          style: TextStyle(
                                            fontSize: 11,
                                            fontWeight: FontWeight.w600,
                                            color: theme.brightness == Brightness.dark ? const Color(0xFFFB923C) : const Color(0xFFC2410C),
                                          ),
                                        ),
                                        const SizedBox(height: 2),
                                        Text(
                                          '$pendingRefunds Parcels',
                                          style: TextStyle(
                                            fontSize: 15,
                                            fontWeight: FontWeight.bold,
                                            color: theme.brightness == Brightness.dark ? Colors.white : const Color(0xFF9A3412),
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                                    decoration: BoxDecoration(
                                      color: const Color(0xFF059669).withOpacity(0.1),
                                      borderRadius: BorderRadius.circular(10),
                                      border: Border.all(color: const Color(0xFF059669).withOpacity(0.2)),
                                    ),
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          'Restocked Resalable',
                                          style: TextStyle(
                                            fontSize: 11,
                                            fontWeight: FontWeight.w600,
                                            color: theme.brightness == Brightness.dark ? const Color(0xFF34D399) : const Color(0xFF047857),
                                          ),
                                        ),
                                        const SizedBox(height: 2),
                                        Text(
                                          '$resalableCount Parcels',
                                          style: TextStyle(
                                            fontSize: 15,
                                            fontWeight: FontWeight.bold,
                                            color: theme.brightness == Brightness.dark ? Colors.white : const Color(0xFF065F46),
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 16),
                      Text(
                        'Recorded Returns (${list.length})',
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                          color: theme.colorScheme.onSurface,
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              // Returns List
              if (list.isEmpty)
                SliverFillRemaining(
                  hasScrollBody: false,
                  child: Center(
                    child: Padding(
                      padding: const EdgeInsets.all(32),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.assignment_return_outlined, size: 52, color: theme.colorScheme.onSurfaceVariant.withOpacity(0.5)),
                          const SizedBox(height: 12),
                          Text(
                            'No Returns Recorded',
                            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            'Tap "Log Return" above to record RTS or customer rejected parcels.',
                            textAlign: TextAlign.center,
                            style: TextStyle(color: theme.colorScheme.onSurfaceVariant, fontSize: 13),
                          ),
                        ],
                      ),
                    ),
                  ),
                )
              else
                SliverPadding(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
                  sliver: SliverList(
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        final ret = list[index];

                        return Padding(
                          padding: const EdgeInsets.only(bottom: 12),
                          child: Card(
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
                                          color: ret.refundStatus.color.withOpacity(0.15),
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
                                          color: ret.itemCondition.color.withOpacity(0.15),
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
                                    style: TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.bold,
                                      color: theme.colorScheme.onSurface,
                                      letterSpacing: 0.3,
                                    ),
                                  ),
                                  const SizedBox(height: 6),
                                  Row(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        'Reason: ',
                                        style: TextStyle(
                                          fontSize: 13,
                                          fontWeight: FontWeight.w600,
                                          color: theme.colorScheme.onSurface,
                                        ),
                                      ),
                                      Expanded(
                                        child: Text(
                                          ret.reason.label,
                                          style: TextStyle(
                                            fontSize: 13,
                                            fontWeight: FontWeight.w500,
                                            color: theme.colorScheme.onSurfaceVariant,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                  if (ret.notes.isNotEmpty) ...[
                                    const SizedBox(height: 4),
                                    Text(
                                      'Notes: ${ret.notes}',
                                      style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurfaceVariant),
                                    ),
                                  ],
                                  const SizedBox(height: 8),
                                  Text(
                                    'Logged by ${ret.loggedByName}${ret.approvedByName != null ? ' • Approved by ${ret.approvedByName}' : ''}',
                                    style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant.withOpacity(0.8)),
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
                                            foregroundColor: Colors.white,
                                            minimumSize: const Size(120, 38),
                                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                                          ),
                                        ),
                                      ],
                                    ),
                                  ],
                                ],
                              ),
                            ),
                          ),
                        );
                      },
                      childCount: list.length,
                    ),
                  ),
                ),
            ],
          );
        },
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
                  ElevatedButton(
                    onPressed: _submit,
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size(120, 44),
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                    ),
                    child: const Text('Record Return'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

