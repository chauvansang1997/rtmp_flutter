import 'package:flutter/material.dart';

class CameraTexture {
  CameraTexture({this.id, this.previewQuarterTurns, this.previewSize});

  final int id;
  final int previewQuarterTurns;
  final Size previewSize;

  double get aspectRatio => previewSize.height / previewSize.width;
}
