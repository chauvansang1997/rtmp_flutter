import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:stream_with_rtmp/camera.dart';
import 'package:camera_with_rtmp_example/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:video_player/video_player.dart';
import 'package:wakelock/wakelock.dart';

class MultipleStreamPage extends StatefulWidget {
  @override
  _MultipleStreamPageState createState() => _MultipleStreamPageState();
}

class _MultipleStreamPageState extends State<MultipleStreamPage>
    with WidgetsBindingObserver {

  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  CameraController controller;
  String imagePath;
  String videoPath;
  String url;
  VideoPlayerController videoController;
  VoidCallback videoPlayerListener;
  bool enableAudio = true;
  bool useOpenGL = true;
  TextEditingController _textFieldController =
  TextEditingController(
      text: "rtmps://live-api-s.facebook.com:443/rtmp/464699098270219?s_bl=1&s_psm=1&s_sc=464699124936883&s_sw=0&s_vt=api-s&a=Abw7tWb4_fGeNek7");

  Timer _timer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // App state changed before we got the chance to initialize.
    if (controller == null || !controller.value.isInitialized) {
      return;
    }
    if (state == AppLifecycleState.inactive) {
      controller?.dispose();
      if (_timer != null) {
        _timer.cancel();
        _timer = null;
      }
    } else if (state == AppLifecycleState.resumed) {
      if (controller != null) {
        onNewCameraSelected(controller.description);
      }
    }
  }

  void onNewCameraSelected(CameraDescription cameraDescription) async {
    if (controller != null) {
      await controller.dispose();
    }
    controller = CameraController(
      cameraDescription,
      ResolutionPreset.medium,
      enableAudio: enableAudio,
      androidUseOpenGL: useOpenGL,
    );

    // If the controller is updated then update the UI.
    controller.addListener(() {
      if (mounted) setState(() {});
      if (controller.value.hasError) {
        showInSnackBar('Camera error ${controller.value.errorDescription}');
        if (_timer != null) {
          _timer.cancel();
          _timer = null;
        }
        Wakelock.disable();
      }
    });

    try {
      await controller.initialize();
    } on CameraException catch (e) {
      _showCameraException(e);
    }

    if (mounted) {
      setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _scaffoldKey,
      appBar: _buildAppbar(),
      body: _buildBody(),
    );
  }

  Widget _buildAppbar() {
    return AppBar(
      title: Text('Facebook stream'),
    );
  }

  Widget _buildBody() {
    return Column(
      children: [
        Expanded(child: _cameraPreviewWidget()),
        Container(
          height: 50,
          width: double.infinity,
          child: _buildCameraList(),
        ),
        Row(
          children: [
            RaisedButton(
              onPressed: () {
                startVideoStreaming();
              },
              child: Text('Open camera'),
            ),
            RaisedButton(
              onPressed: () {
                startVideoStreaming();
              },
              child: Text('Start stream'),
            ),
            RaisedButton(
              onPressed: () {
               setFilter();
              },
              child: Text('Add filer'),
            ),
          ],
        )
      ],
    );
  }

  /// Display the preview from the camera (or a message if the preview is not available).
  Widget _cameraPreviewWidget() {
    if (controller == null || !controller.value.isInitialized) {
      return const Text(
        'Tap a camera',
        style: TextStyle(
          color: Colors.white,
          fontSize: 24.0,
          fontWeight: FontWeight.w900,
        ),
      );
    } else {
      return AspectRatio(
        aspectRatio: controller.value.aspectRatio,
        child: CameraPreview(controller),
      );
    }
  }

  Widget _buildCameraList() {
    return ListView.separated(
      itemCount: cameras.length,
      separatorBuilder: (BuildContext context, int index) {
        return SizedBox(height: 10);
      },
      itemBuilder: (BuildContext context, int index) {
        return ListTile(title: Text(cameras[index].name), onTap: () {
          onNewCameraSelected(cameras[index]);
        },);
      },
    );
  }

  Future<void> stopVideoStreaming() async {
    if (!controller.value.isStreamingVideoRtmp) {
      return null;
    }

    try {
      await controller.stopVideoStreaming();
      if (_timer != null) {
        _timer.cancel();
        _timer = null;
      }
    } on CameraException catch (e) {
      // _showCameraException(e);
      return null;
    }
  }

  void showInSnackBar(String message) {
    _scaffoldKey.currentState.showSnackBar(SnackBar(content: Text(message)));
  }

  Future<String> startVideoStreaming() async {
    if (!controller.value.isInitialized) {
      showInSnackBar('Error: select a camera first.');
      return null;
    }

    // if (controller.value.isStreamingVideoRtmp) {
    //   return null;
    // }

    // Open up a dialog for the url
    String myUrl = await _getUrl();

    try {
      if (_timer != null) {
        _timer.cancel();
        _timer = null;
      }
      url = myUrl;
      await controller.startVideoStreaming(url);
      // _timer = Timer.periodic(Duration(seconds: 1), (timer) async {
      //   var stats = await controller.getStreamStatistics();
      //   print(stats);
      // });
    } on CameraException catch (e) {
      _showCameraException(e);
      return null;
    }
    return url;
  }

  Future<String> _getUrl() async {
    // Open up a dialog for the url
    String result = _textFieldController.text;

    return await showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text('Url to Stream to'),
            content: TextField(
              controller: _textFieldController,
              decoration: InputDecoration(hintText: "Url to Stream to"),
              onChanged: (String str) => result = str,
            ),
            actions: <Widget>[
              new FlatButton(
                child: new Text(
                    MaterialLocalizations
                        .of(context)
                        .cancelButtonLabel),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
              FlatButton(
                child: Text(MaterialLocalizations
                    .of(context)
                    .okButtonLabel),
                onPressed: () {
                  Navigator.pop(context, result);
                },
              )
            ],
          );
        });
  }

  void _showCameraException(CameraException e) {
    logError(e.code, e.description);
    showInSnackBar('Error: ${e.code}\n${e.description}');
  }

  Future<void> setFilter() async {
    // Uint8List bytes = await _readFileByte('assets/frame.png');
    ByteData bytes = await rootBundle.load('assets/frame_medium.png');
    await controller.setImageFilter(bytes.buffer.asUint8List());
  }

  Future<Uint8List> _readFileByte(String filePath) async {
    Uri myUri = Uri.parse(filePath);
    File file = new File.fromUri(myUri);
    Uint8List bytes;
    await file.readAsBytes().then((value) {
      bytes = Uint8List.fromList(value);
      print('reading of bytes is completed');
    }).catchError((onError) {
      print('Exception Error while reading audio from path:' +
          onError.toString());
    });
    return bytes;
  }
}
