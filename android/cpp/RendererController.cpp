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


#include "RendererController.h"
#include "shaders.h"

RendererController::RendererController()
                            : renderer(NULL) {
    // initialize render mutex
    pthread_mutexattr_t attr;
    pthread_mutexattr_init(&attr);
    pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_NORMAL);

    if (pthread_mutex_init(&_mutex_controller, &attr) != 0)
        __android_log_print(ANDROID_LOG_DEBUG,"RendererController: ", "RendererController() _mutex_controller init failed");
}

RendererController::~RendererController() {
    nativeOnStop();
    pthread_mutex_destroy(&_mutex_controller);
}

void RendererController::nativeSetSurface(JNIEnv* jenv,
                                          jobject surface,
                                          int func,
                                          const char *name,
                                          int width, int height,
                                          float scaleX, float scaleY,
                                          int clearR, int clearG, int clearB, int clearA) {

    pthread_mutex_lock(&_mutex_controller);
    if (renderer != NULL) {
        renderer->stop();
        if (renderer != NULL) {
            free(renderer);
            renderer = NULL;
        }
    }

    // set shader and draw frame functions
    typedef bool (*sf)(void *);
    typedef void (*df)(void *);
    sf shaderFunc;
    df drawFunc;
    int ES_version;
    bool continuous;
    switch (func) {
        case 1:
            ES_version = 2;
            continuous = true;
            shaderFunc = &initShader1;
            drawFunc = &drawFrame1;
            break;
        case 2:
            ES_version = 2;
            continuous = true;
            shaderFunc = &initShader2;
            drawFunc = &drawFrame2;
            break;
        case 3:
            ES_version = 2;
            continuous = true;
            shaderFunc = &initShader3;
            drawFunc = &drawFrame3;
            break;
        case 0:
        default:
            ES_version = 1;
            continuous = true;
            shaderFunc = &initShaderDefaults;
            drawFunc = &defaultDrawFrame;
            break;
    }

    renderer = new Renderer(ES_version,
                            shaderFunc, drawFunc,
                            continuous,
                            name,
                            width, height,
                            scaleX,scaleY,
                            clearR,clearG,clearB,clearA);

    renderer->setNativeWindow(jenv, surface);





    renderer->start();
    pthread_mutex_unlock(&_mutex_controller);
}

void RendererController::nativeOnStop() {
    if (renderer==NULL) return;
    pthread_mutex_lock(&_mutex_controller);
    if (renderer!=NULL) {
        renderer->stop();
        renderer->destroy();
        free(renderer);
    }
    pthread_mutex_unlock(&_mutex_controller);
}



void RendererController::setNativeWindowClearColor(const unsigned char r,
                                                   const unsigned char g,
                                                   const unsigned char b,
                                                   const unsigned char a)
{
    pthread_mutex_lock(&_mutex_controller);
    if (renderer != NULL)
        renderer->setClearColor( r, g, b, a );
    pthread_mutex_unlock(&_mutex_controller);
}

