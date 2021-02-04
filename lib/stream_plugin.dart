import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:rxdart/rxdart.dart';
import 'package:stream_with_rtmp/model/camera_texture.dart';
import 'package:stream_with_rtmp/model/exceptions.dart';

import 'camera.dart';

class StreamPlugin {
  static final StreamPlugin _singleton = StreamPlugin._internal();

  StreamPlugin._internal();

  factory StreamPlugin() {
    return _singleton;
  }

  final MethodChannel _channel = const MethodChannel('plugins.flutter.io/camera_with_rtmp');

  int _textureId;

  int get textureId => _textureId;

  BehaviorSubject<String> _errorSubject;

  StreamSubscription<dynamic> _eventSubscription;

  StreamSubscription<dynamic> _streamEventSubscription;

  CameraDescription description;
  ResolutionPreset resolutionPreset;
  ResolutionPreset streamingPreset;
  Completer<void> _creatingCompleter;

  /// Whether to include audio when recording a video.
  bool enableAudio;
  bool androidUseOpenGL;

  Future<CameraTexture> initialize() async {
    CameraTexture cameraTexture;
    try {
      _creatingCompleter = Completer<void>();
      _errorSubject = BehaviorSubject<String>();
      final Map<String, dynamic> reply = await _channel.invokeMapMethod<String, dynamic>(
        'initialize',
        <String, dynamic>{
          'cameraName': description.name,
          'resolutionPreset': serializeResolutionPreset(resolutionPreset),
          'streamingPreset': serializeResolutionPreset(streamingPreset ?? resolutionPreset),
          'enableAudio': enableAudio,
          'enableAndroidOpenGL': androidUseOpenGL ?? false
        },
      );
      _textureId = reply['textureId'];
      cameraTexture = CameraTexture(
        id: _textureId,
        previewQuarterTurns: reply['previewQuarterTurns'],
        previewSize: Size(
          reply['previewWidth'].toDouble(),
          reply['previewHeight'].toDouble(),
        ),
      );
      _eventSubscription = EventChannel('plugins.flutter.io/camera_with_rtmp/cameraEvents$_textureId')
          .receiveBroadcastStream()
          .listen(_listener);
    } on PlatformException catch (e) {
      throw CameraException(e.code, e.message);
    }
    _creatingCompleter.complete();
    return cameraTexture;
  }

  Future<void> setImageFilter(Uint8List image) async {
    await _channel.invokeMethod<void>('setImageFilter', {'image': image});
  }

  void _listener(dynamic event) {
    final Map<dynamic, dynamic> map = event;

    switch (map['eventType']) {
      case 'error':
        break;
      case 'camera_closing':
        break;
      case 'rtmp_connected':
        break;
      case 'rtmp_retry':
        break;
      case 'rtmp_stopped':
        break;
      case 'rotation_update':
        break;
    }
  }

  /// Start a video streaming to the url in [url`].
  ///
  /// This uses rtmp to do the sending the remote side.
  ///
  /// Throws a [CameraException] if the capture fails.
  Future<void> startVideoStreaming(
      {@required String url, @required String streamId, int bitrate = 1200 * 1024, bool androidUseOpenGL}) async {
    try {
      await _channel.invokeMethod<void>('startVideoStreaming', <String, dynamic>{
        'textureId': _textureId,
        'url': url,
        'bitrate': bitrate,
        'streamId': streamId,
      });
    } on PlatformException catch (e) {
      throw StreamException(e.code, e.message);
    }
  }

  /// Pause video recording.
  ///
  /// This feature is only available on iOS and Android sdk 24+.
  Future<void> pauseVideoStreaming() async {
    try {
      await _channel.invokeMethod<void>(
        'pauseVideoStreaming',
        <String, dynamic>{'textureId': _textureId},
      );
    } on PlatformException catch (e) {
      throw StreamException(e.code, e.message);
    }
  }

  Future<void> switchCamera(int cameraId, {int bitrate = 1200 * 1024}) async {
    try {
      await _channel.invokeMethod<void>(
        'switchCamera',
        <String, dynamic>{'bitrate': bitrate, "cameraId": cameraId},
      );
    } on PlatformException catch (e) {
      throw StreamException(e.code, e.message);
    }
  }

  /// Stop streaming.
  Future<void> stopStreaming({@required String streamUrl}) async {
    try {
      print("Stop streaming call");
      await _channel.invokeMethod<void>(
        'stopStreaming',
        <String, dynamic>{'streamUrl': streamUrl},
      );
    } on PlatformException catch (e) {
      print("GOt exception " + e.toString());
      throw StreamException(e.code, e.message);
    }
  }

  /// Stop streaming.
  Future<void> stopVideoStreaming() async {
    try {
      print("Stop video streaming call");
      await _channel.invokeMethod<void>(
        'stopRecordingOrStreaming',
        <String, dynamic>{'textureId': _textureId},
      );
    } on PlatformException catch (e) {
      print("GOt exception " + e.toString());
      throw StreamException(e.code, e.message);
    }
  }

  Future<void> dispose() async {
    if (_creatingCompleter != null) {
      await _creatingCompleter.future;
      await _channel.invokeMethod<void>(
        'dispose',
        <String, dynamic>{'textureId': _textureId},
      );
      _errorSubject?.close();
      _eventSubscription?.cancel();
      _streamEventSubscription?.cancel();
    }
  }
}
