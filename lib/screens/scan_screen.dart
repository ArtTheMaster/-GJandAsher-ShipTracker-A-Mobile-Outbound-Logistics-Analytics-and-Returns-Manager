import 'package:flutter/material.dart';
import '../controllers/ship_tracker_controller.dart';
import '../models/models.dart';
import '../theme/app_theme.dart';

class ScanScreen extends StatefulWidget {
  final ShipTrackerController controller;

  const ScanScreen({super.key, required this.controller});

  @override
  State<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends State<ScanScreen> {
  final _barcodeController = TextEditingController();
  final _recipientNameController = TextEditingController();
  final _recipientPhoneController = TextEditingController();
  final _notesController = TextEditingController();

  @override
  void dispose() {
    _barcodeController.dispose();
    _recipientNameController.dispose();
    _recipientPhoneController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  void _onCodeChanged(String val) {
    widget.controller.onScanCodeChanged(val);
  }

  void _simulateScan(String code) {
    _barcodeController.text = code;
    widget.controller.onScanCodeChanged(code);
  }

  void _submit() {
    widget.controller.setScanRecipient(
      _recipientNameController.text,
      _recipientPhoneController.text,
      _notesController.text,
    );

    widget.controller.submitScannedParcel((success, message) {
      if (success) {
        _barcodeController.clear();
        _recipientNameController.clear();
        _recipientPhoneController.clear();
        _notesController.clear();
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(message),
          backgroundColor: success ? const Color(0xFF059669) : const Color(0xFFDC2626),
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final user = widget.controller.currentUser;
    final canScan = user?.role == UserRole.owner || (user?.canScanParcels ?? false);

    return Scaffold(
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Permission check banner
            if (!canScan)
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
                        'Scanning permission restricted. Ask Owner Nolan to enable parcel scanning on your staff account.',
                        style: TextStyle(color: Color(0xFFB91C1C), fontSize: 12),
                      ),
                    ),
                  ],
                ),
              ),

            // Barcode Input Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.qr_code_scanner, color: Color(0xFF1E3A8A)),
                        const SizedBox(width: 8),
                        const Text(
                          'Scan or Input Airway Bill',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                        const Spacer(),
                        Row(
                          children: [
                            const Text('Offline Mode', style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600)),
                            Switch(
                              value: widget.controller.isOfflineScanMode,
                              onChanged: (_) => widget.controller.toggleOfflineMode(),
                            ),
                          ],
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _barcodeController,
                      enabled: canScan,
                      onChanged: _onCodeChanged,
                      decoration: InputDecoration(
                        labelText: 'Barcode / Tracking Number',
                        hintText: 'Scan or type e.g. SPXPH89217482',
                        prefixIcon: const Icon(Icons.barcode_reader),
                        suffixIcon: IconButton(
                          icon: const Icon(Icons.clear),
                          onPressed: () {
                            _barcodeController.clear();
                            _onCodeChanged('');
                          },
                        ),
                      ),
                    ),
                    const SizedBox(height: 10),

                    // Quick simulator buttons for quick testing
                    const Text('QUICK TEST BARCODES (AUTO-DETECTION):', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFF64748B))),
                    const SizedBox(height: 8),
                    Column(
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: OutlinedButton.icon(
                                style: OutlinedButton.styleFrom(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
                                  shape: RoundedCornerShape(10),
                                ),
                                icon: const Icon(Icons.circle, size: 8, color: Color(0xFFEE4D2D)),
                                label: const Text('Shopee SPX', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600), textAlign: TextAlign.center, overflow: TextOverflow.ellipsis),
                                onPressed: canScan ? () => _simulateScan('SPXPH${DateTime.now().millisecondsSinceEpoch.toString().substring(7)}') : null,
                              ),
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: OutlinedButton.icon(
                                style: OutlinedButton.styleFrom(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
                                  shape: RoundedCornerShape(10),
                                ),
                                icon: const Icon(Icons.circle, size: 8, color: Color(0xFFE11D48)),
                                label: const Text('J&T Express', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600), textAlign: TextAlign.center, overflow: TextOverflow.ellipsis),
                                onPressed: canScan ? () => _simulateScan('JT998${DateTime.now().millisecondsSinceEpoch.toString().substring(7)}PH') : null,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Row(
                          children: [
                            Expanded(
                              child: OutlinedButton.icon(
                                style: OutlinedButton.styleFrom(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
                                  shape: RoundedCornerShape(10),
                                ),
                                icon: const Icon(Icons.circle, size: 8, color: Color(0xFF0060FF)),
                                label: const Text('Lazada LEX', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600), textAlign: TextAlign.center, overflow: TextOverflow.ellipsis),
                                onPressed: canScan ? () => _simulateScan('MPLZD${DateTime.now().millisecondsSinceEpoch.toString().substring(7)}') : null,
                              ),
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: OutlinedButton.icon(
                                style: OutlinedButton.styleFrom(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
                                  shape: RoundedCornerShape(10),
                                ),
                                icon: const Icon(Icons.circle, size: 8, color: Color(0xFF06B6D4)),
                                label: const Text('TikTok Shop', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600), textAlign: TextAlign.center, overflow: TextOverflow.ellipsis),
                                onPressed: canScan ? () => _simulateScan('TT990${DateTime.now().millisecondsSinceEpoch.toString().substring(7)}') : null,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // Detection & Classification Preview Card
            AnimatedBuilder(
              animation: widget.controller,
              builder: (context, _) {
                final duplicate = widget.controller.scanDuplicateWarning;
                return Column(
                  children: [
                    if (duplicate != null)
                      Container(
                        margin: const EdgeInsets.only(bottom: 16),
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: const Color(0xFFFEF2F2),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(color: const Color(0xFFEF4444)),
                        ),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Icon(Icons.warning_amber_rounded, color: Color(0xFFDC2626)),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Text(
                                    'DUPLICATE PARCEL DETECTED!',
                                    style: TextStyle(color: Color(0xFF991B1B), fontWeight: FontWeight.bold, fontSize: 13),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    'This parcel was already logged as ${duplicate.status.label} on ${duplicate.platform.displayName}. Submission will be blocked.',
                                    style: const TextStyle(color: Color(0xFFB91C1C), fontSize: 12),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),

                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(20),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text('Auto-Detected Platform & Logistics', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
                            const SizedBox(height: 4),
                            Text(
                              widget.controller.scanClassificationNote.isNotEmpty
                                  ? widget.controller.scanClassificationNote
                                  : 'Awaiting barcode input for auto-classification',
                              style: const TextStyle(fontSize: 12, color: Color(0xFF64748B)),
                            ),
                            const SizedBox(height: 16),
                            Row(
                              children: [
                                Expanded(
                                  child: DropdownButtonFormField<PlatformType>(
                                    value: widget.controller.scanPlatform,
                                    decoration: const InputDecoration(labelText: 'Platform'),
                                    items: PlatformType.values.map((p) {
                                      return DropdownMenuItem(value: p, child: Text(p.displayName));
                                    }).toList(),
                                    onChanged: canScan ? (val) => widget.controller.setScanPlatform(val!) : null,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: DropdownButtonFormField<CourierType>(
                                    value: widget.controller.scanCourier,
                                    decoration: const InputDecoration(labelText: 'Courier'),
                                    items: CourierType.values.map((c) {
                                      return DropdownMenuItem(value: c, child: Text(c.displayName));
                                    }).toList(),
                                    onChanged: canScan ? (val) => widget.controller.setScanCourier(val!) : null,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                );
              },
            ),

            const SizedBox(height: 16),

            // Optional Recipient Details
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Recipient & Parcel Notes (Optional)', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 14),
                    TextField(
                      controller: _recipientNameController,
                      decoration: const InputDecoration(
                        labelText: 'Buyer / Recipient Name',
                        hintText: 'e.g. Maria Santos',
                        prefixIcon: Icon(Icons.person_outline),
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _notesController,
                      decoration: const InputDecoration(
                        labelText: 'Notes or Contents',
                        hintText: 'e.g. 2x Shirts, fragile packaging',
                        prefixIcon: Icon(Icons.edit_note_outlined),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),

            ElevatedButton.icon(
              onPressed: canScan ? _submit : null,
              icon: const Icon(Icons.check_circle_outline),
              label: const Text('Log Scanned Parcel to Warehouse'),
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
