package com.bavagnoli.flutteropengl;
// Copyright 2019 Marco Bavagnoli <marco.bavagnoli@gmail.com>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.TextureRegistry;

/** FlutteropenglPlugin */
public class FlutteropenglPlugin implements FlutterPlugin,MethodCallHandler {
  private static String TAG = FlutteropenglPlugin.class.getSimpleName();
  private TextureRegistry textures;
  private LongSparseArray<OpenGLNDKController> rendersNDK = new LongSparseArray<>();
  private static MethodChannel channel;

  private float getFloat(Object obj) {
    if (obj instanceof Float)
      return ((Float) obj).floatValue();
    return -1;
  }

  private double getDouble(Object obj) {
    if (obj instanceof Double)
      return ((Double) obj).doubleValue();
    return -1;
  }

  private int getInt(Object obj) {
    if (obj instanceof Integer)
      return ((Integer) obj).intValue();
    return -1;
  }

  private long getLong(Object obj) {
    if (obj instanceof Long)
      return ((Long) obj).longValue();
    return -1;
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    Map<String, Object> arguments = (Map<String, Object>) call.arguments;
    if (arguments != null)
      Log.d("FlutteropenglPlugin", call.method + " " + call.arguments.toString());
    OpenGLNDKController renderNDK;
    TextureRegistry.SurfaceTextureEntry entry;
    SurfaceTexture surfaceTexture;
    int drawingFunction;
    int width;
    int height;
    long textureId;

    switch (call.method) {

      /// NDK
      case "createNDK":
        entry = textures.createSurfaceTexture();
        surfaceTexture = entry.surfaceTexture();

        width = (int)getDouble(arguments.get("width"));
        height = (int)getDouble(arguments.get("height"));
        drawingFunction = getInt(arguments.get("drawingFunction"));

        // check if all arguments are sane
        if (width == -1) Log.i(TAG, "onMethodCall: width not passed corrected");
        if (height == -1) Log.i(TAG, "onMethodCall: height not passed corrected");
        if (drawingFunction == -1) Log.i(TAG, "onMethodCall: drawingFunction not passed corrected");

        renderNDK = new OpenGLNDKController(surfaceTexture, drawingFunction, width, height);

        rendersNDK.put(entry.id(), renderNDK);

        result.success(entry.id());
        break;

      case "stopNDK":
        textureId = getInt(arguments.get("textureId"));
        if (textureId == -1) Log.i(TAG, "onMethodCall: textureId not passed corrected");
        if (rendersNDK.get(textureId) == null) break;
        rendersNDK.get(textureId).stop();
        rendersNDK.delete(textureId);
        result.success(true);
        break;

      default:
        result.notImplemented();
    }
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    if(this.textures==null){
      this.textures= binding.getTextureRegistry();
    }
    Log.e("onAttachedToEngine", "onAttachedToEngine");
    channel = new MethodChannel(binding.getBinaryMessenger(), "flutteropengl");
    channel.setMethodCallHandler(this);

    // initialize BmpManager's AssetManager for future textures use
    BmpManager.setAssetsManager(binding);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

  }
}
