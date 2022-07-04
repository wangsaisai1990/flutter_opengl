import 'dart:async';

import 'package:flutter/services.dart';

class Flutteropengl {
  static const MethodChannel _channel = const MethodChannel('flutteropengl');

  int textureId;

  Future<int> initializeNDK(
      int drawingFunction, double width, double height) async {
    textureId = await _channel.invokeMethod('createNDK', {
      'drawingFunction': drawingFunction,
      'width': width,
      'height': height,
    });
    return textureId;
  }

  Future<Null> stopNDK() async {
    await _channel.invokeMethod('stopNDK', {'textureId': textureId});
  }

  bool get isInitialized => textureId != null;
}
