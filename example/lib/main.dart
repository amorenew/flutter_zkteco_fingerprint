import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:zk_finger/finger_status.dart';
import 'package:zk_finger/finger_status_type.dart';
import 'package:zk_finger/zk_finger.dart';

void main() => runApp(MaterialApp(home: MyApp()));

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String? _platformVersion = 'Unknown';

  TextEditingController _registerationCodeController =
      TextEditingController(text: "DEVD6586");
  TextEditingController _biometricController = TextEditingController(
      text:
          '''H4sIAAAAAAAAAO3K8U+UZQAH8O87773jvJMnBM6VySTM1hF4ptMVXbVTHjo7zDiC0QwKmtODjgQC
mXix7rxuOwGr5TWJLFdsWLtSaezubDzv+94BYlJbbbRg6GIEpkMdZYu7933r32jj8/NnD+90Pr4V
WEWpJo3X6Sd+BGZpLgfAezOnia7BHRLCFXD2qyDITL+Oh+kvpA8F+Il8QUvQlW5GEKP2LpQjh5yH
iy6SIFyQyXbaAIk4MIQf7J+gGZ+Rg2ihETKENgRIHvUgRiZQgiv2l+HFE8SPXjpMTuBjZBvd9Cw2
EB/qsPwcwzm8RvYjQW+nr8NlHCLH6DjOkAswYd4exwIaSSnuUo5sgYw58uFgeEaajN4aNXUOLWZM
KqPBPNP0K6dv9gU3Zx1N9lvr3txbucgNL04Pf/7H3pMjln3Rt04tRapO63OMe8YvbTK4OevqB0/J
5bcWDrvmKtY0VlRP3h0MRcKd/vB3s2rsTqnHm8EXLLjPdZhL63xd39TojFPmwbx516s1jrVzr+eP
zVxdf7vX/XawZczZb3/mnq774lL10q9/fTnwwNmtSU3bG8Jgz9fv9DQv1e4rc23IbK3J6/Y2Byw7
NuZzXHv2dQfnWn3oiKjt4KlNv79B1CWqDLnHQwYEjOeZ4BPE95kev2e+MCKwKi16x5DyL6t63J99
QmU+xaXHp/mXFTmWUnbq8dGNY8wnhIQMnPlzXUGs0KzIydyf0a/pS4wxFt/d7T2+HsZKq/hBvfh0
e+d7AvMIGDBultVsGHbv9D/GmFgbECruW9XotDLWcaD4iIleKxqQmBSNvzvJxAOiHpXlWdFlRWby
PYaWh2b/TiW1sB1s6oz38+hxzf9T6OSed0uquciiRBRZkdUUr8g2Lui27spK7GjS+j1ShxTgxYCN
8x12jggTuBZ/kSmimnpSViNGhJtZuC0w6iNwtO5iL4W6GI+L7Q6LhOhTNll5hOD73m1+SfGIjH0r
mOhG7/Z6FtQxcVwS25kBBr+zVUipyaQqoUooEv1aTFc7i2MaTF2oLBRw6UaNov7XkJOK1DNPGmbS
aqWYYtUga+q3Rp6unX9UUH0MwlcNFh7blLkWXcUWbtPRMlvC8GwJB0NxGVasWLHi/+dfvq8JzgAI
AAA=''');

  String? score;
  String? verifiedId;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String? platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await ZkFinger.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    ZkFinger.imageStream.receiveBroadcastStream().listen(mapFingerImage);
    ZkFinger.statusChangeStream.receiveBroadcastStream().listen(updateStatus);

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Uint8List? fingerImages;
  String statusText = '';
  String stringLengthBytes = '';

  FingerStatus? fingerStatus;
  FingerStatusType? tempStatusType;

  void updateStatus(dynamic value) {
    Map<dynamic, dynamic> statusMap = value as Map<dynamic, dynamic>;
    FingerStatusType statusType =
        FingerStatusType.values[statusMap['fingerStatus']];
    fingerStatus = FingerStatus(
        statusMap['message'], statusType, statusMap['id'], statusMap['data']);

    print(fingerStatus);
    print(tempStatusType);

    if (statusType == tempStatusType &&
        tempStatusType == FingerStatusType.CAPTURE_ERROR) {
      //ignore capture error when finger device get stucked
    } else {
      tempStatusType = statusType;
      setState(() {
        setBiometricBase64TextField();
        statusText = statusText +
            fingerStatus!.statusType.toString() +
            " Id: " +
            fingerStatus!.id +
            '\n';
      });
    }
  }

  void setBiometricBase64TextField() {
    if (fingerStatus!.statusType == FingerStatusType.ENROLL_SUCCESS) {
      resetFieldsData();
      _biometricController.text = fingerStatus!.data;
      verifiedId = fingerStatus!.id + ' enroll';
    } else if (fingerStatus!.statusType ==
        FingerStatusType.ENROLL_ALREADY_EXIST) {
      resetFieldsData();
      score = fingerStatus!.data;
      verifiedId = fingerStatus!.id + ' already enrolled';
    } else if (fingerStatus!.statusType == FingerStatusType.VERIFIED_SUCCESS) {
      resetFieldsData();
      verifiedId = fingerStatus!.id + ' verified';
      score = fingerStatus!.data;
    } else if (fingerStatus!.statusType == FingerStatusType.FINGER_REGISTERED) {
      resetFieldsData();
      verifiedId = fingerStatus!.id + ' register';
      _biometricController.text = fingerStatus!.data;
    } else if (fingerStatus!.statusType == FingerStatusType.ENROLL_CONFIRM) {
      resetFieldsData();
      verifiedId = fingerStatus!.id + ' confirm';
      _biometricController.text = 'Current Confirm Index ${fingerStatus!.data}';
    }
    stringLengthBytes = 'Text Size: ${_biometricController.text.length} bytes';
    statusText = statusText + stringLengthBytes + '\n';
  }

  void resetFieldsData() {
    _biometricController.text = '';
    verifiedId = '';
    score = '';
  }

  void mapFingerImage(dynamic imageBytes) {
    setState(() {
      fingerImages = imageBytes;
    });
  }

  bool? isDeviceSupported;
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Row(
            children: <Widget>[
              Column(
                children: <Widget>[
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      elevation: 5,
                      padding: const EdgeInsets.all(12.0),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30.0),
                      ),
                    ),
                    onPressed: () async {
                      bool? isSupported = await ZkFinger.isDeviceSupported();
                      setState(() {
                        isDeviceSupported = isSupported;
                        statusText = statusText +
                            "Is zkteco Finger Print Supported: $isDeviceSupported";
                      });
                    },
                    child: Text(
                      'Is Device Supported',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      elevation: 5,
                      padding: const EdgeInsets.all(12.0),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30.0),
                      ),
                    ),
                    onPressed: () async {
                      await ZkFinger.openConnection(isLogEnabled: false);
                    },
                    child: Text(
                      'Open Connection',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      elevation: 5,
                      padding: const EdgeInsets.all(12.0),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30.0),
                      ),
                    ),
                    onPressed: () async {
                      await ZkFinger.startListen(
                          userId: _registerationCodeController.text);
                    },
                    child: Text(
                      'Start Listening',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      elevation: 5,
                      padding: const EdgeInsets.all(12.0),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30.0),
                      ),
                    ),
                    onPressed: () async {
                      await ZkFinger.enroll(
                          userId: _registerationCodeController.text);
                    },
                    child: Text(
                      'Enroll Finger',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      elevation: 5,
                      padding: const EdgeInsets.all(12.0),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30.0),
                      ),
                    ),
                    onPressed: () async {
                      await ZkFinger.verify(
                          userId: _registerationCodeController.text);
                    },
                    child: Text(
                      'Verify Finger',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      elevation: 5,
                      padding: const EdgeInsets.all(12.0),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30.0),
                      ),
                    ),
                    onPressed: () async {
                      await ZkFinger.clearFingerDatabase();
                    },
                    child: Text(
                      'Clear finger\nDatabase',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      elevation: 5,
                      padding: const EdgeInsets.all(12.0),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30.0),
                      ),
                    ),
                    onPressed: () async {
                      await ZkFinger.stopListen();
                    },
                    child: Text(
                      'Stop Listening',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blue,
                      elevation: 5,
                      padding: const EdgeInsets.all(12.0),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(30.0),
                      ),
                    ),
                    onPressed: () async {
                      await ZkFinger.closeConnection();
                    },
                    child: Text(
                      'Close Connection',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                  _getFingerStatusImage()
                ],
              ),
              fingerImages != null
                  ? Image.memory(
                      fingerImages!,
                      width: MediaQuery.of(context).size.width * .2,
                      height: double.infinity,
                      fit: BoxFit.contain,
                    )
                  : Text('Running on: $_platformVersion\n'),
              Container(
                width: MediaQuery.of(context).size.width * .3,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    TextFormField(
                      controller: _registerationCodeController,
                      decoration:
                          InputDecoration(labelText: "Registeration Code"),
                    ),
                    Text('Biometric Base64 Text:',
                        style: TextStyle(fontSize: 14, color: Colors.blue)),
                    TextFormField(
                        controller: _biometricController,
                        maxLines: null,
                        style: TextStyle(fontSize: 7)),
                    Text('Score: $score',
                        style: TextStyle(fontSize: 14, color: Colors.blue)),
                    Text('Verified Id: $verifiedId',
                        style: TextStyle(fontSize: 14, color: Colors.blue)),
                    ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue,
                        elevation: 5,
                        padding: const EdgeInsets.all(12.0),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(30.0),
                        ),
                      ),
                      onPressed: () async {
                        await ZkFinger.registerFinger(
                            userId: _registerationCodeController.text,
                            dataBase64: _biometricController.text);
                      },
                      child: Text(
                        'Register User Biometric Base64 Data',
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                  width: MediaQuery.of(context).size.width * .3,
                  child: Text('statusText: $statusText'))
            ],
          ),
        ),
      ),
    );
  }

  Widget _getFingerStatusImage() {
    if (fingerStatus == null) {
      return SvgPicture.asset(
        'assets/finger.svg',
        color: Colors.lime,
        width: 70,
        height: 120,
      );
    }
    Color svgColor = Colors.black12;
    switch (fingerStatus!.statusType) {
      case FingerStatusType.STARTED_ALREADY:
      case FingerStatusType.STARTED_SUCCESS:
        svgColor = Colors.blue;
        break;
      case FingerStatusType.VERIFIED_START_FIRST:
      case FingerStatusType.VERIFIED_SUCCESS:
        svgColor = Colors.pink;
        break;
      case FingerStatusType.ENROLL_ALREADY_EXIST:
      case FingerStatusType.ENROLL_CONFIRM:
      case FingerStatusType.ENROLL_STARTED:
      case FingerStatusType.ENROLL_SUCCESS:
        svgColor = Colors.deepOrange;
        break;
      case FingerStatusType.STOPPED_ALREADY:
      case FingerStatusType.STOPPED_SUCCESS:
        svgColor = Colors.cyan;
        break;
      case FingerStatusType.FINGER_REGISTERED:
        svgColor = Colors.green;
        break;
      case FingerStatusType.FINGER_CLEARED:
        svgColor = Colors.yellow;
        break;
      case FingerStatusType.STARTED_FAILED:
      case FingerStatusType.STARTED_ERROR:
      case FingerStatusType.VERIFIED_FAILED:
      case FingerStatusType.ENROLL_FAILED:
      case FingerStatusType.STOPPED_ERROR:
      case FingerStatusType.CAPTURE_ERROR:
        svgColor = Colors.redAccent;
        break;
      default:
        svgColor = Colors.black38;
    }

    return SvgPicture.asset(
      'assets/finger.svg',
      color: svgColor,
      width: 70,
      height: 120,
    );
  }
}
