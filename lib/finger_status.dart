import 'package:zk_finger/finger_status_type.dart';

class FingerStatus {
  FingerStatus(this.message, this.statusType, this.id, this.data);

  final String message;
  final FingerStatusType statusType;
  final String id;
  final String data;

  
  log() {
    print('\nMessage: $message');
    print('\nId: $id');
    print('\nStatus: ${statusType}');
    print('\nData: $data');
  }
}
