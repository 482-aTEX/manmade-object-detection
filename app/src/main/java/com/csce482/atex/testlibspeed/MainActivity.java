package com.csce482.atex.testlibspeed;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES31;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import project.android.imageprocessing.FastImageProcessingPipeline;
import project.android.imageprocessing.FastImageProcessingView;
import project.android.imageprocessing.filter.BasicFilter;
import project.android.imageprocessing.filter.colour.*;
import project.android.imageprocessing.filter.effect.*;
import project.android.imageprocessing.filter.processing.*;
import project.android.imageprocessing.input.CameraPreviewInput;
import project.android.imageprocessing.input.GLTextureOutputRenderer;
import project.android.imageprocessing.output.ScreenEndpoint;

/**
 * Created by cloudburst on 4/15/15.
 */
public class MainActivity extends Activity {
    private FastImageProcessingView view;
    private List<BasicFilter> filters;
    private int curFilter;
    private GLTextureOutputRenderer input;
    private long touchTime;
    private FastImageProcessingPipeline pipeline;
    private ScreenEndpoint screen;
    private ConvolutionFilter cFilter;
    private ConvolutionFilter cFilter2;
    private GreyScaleFilter gFilter;
    private MultiConvolutionFilter mcFilter;

    private void addFilter(BasicFilter filter) {
        filters.add(filter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        view = new FastImageProcessingView(this);
        pipeline = new FastImageProcessingPipeline();
        view.setPipeline(pipeline);
        setContentView(view);
        input = new CameraPreviewInput(view);
        gFilter = new GreyScaleFilter();
        int numKernels = 24;
        FloatBuffer kernels = FloatBuffer.allocate(81*numKernels);
        for (int i = 0 ; i < numKernels ; i++) {
            kernels.put(new float[] { 4.6063702E-5f, -1.616728E-4f, -7.9628895E-4f, 0.0055037676f, -0.006672912f, -0.011353423f, 0.027107773f, -0.011353423f, -0.006672912f,
                    4.628866E-5f, -1.6246234E-4f, -8.0017775E-4f, 0.005530646f, -0.0067055f, -0.011408869f, 0.027240157f, -0.011408869f, -0.0067055f,
                    4.6473528E-5f, -1.6311118E-4f, -8.0337346E-4f, 0.0055527347f, -0.0067322813f, -0.011454436f, 0.027348952f, -0.011454436f, -0.0067322813f,
                    4.6617843E-5f, -1.636177E-4f, -8.058682E-4f, 0.0055699763f, -0.006753185f, -0.011490001f, 0.027433872f, -0.011490001f, -0.006753185f,
                    4.6721198E-5f, -1.639804E-4f, -8.076546E-4f, 0.0055823238f, -0.006768156f, -0.011515473f, 0.02749469f, -0.011515473f, -0.006768156f,
                    4.678331E-5f, -1.641984E-4f, -8.087284E-4f, 0.0055897464f, -0.0067771543f, -0.011530784f, 0.027531244f, -0.011530784f, -0.0067771543f,
                    4.6804023E-5f, -1.6427114E-4f, -8.090867E-4f, 0.0055922223f, -0.006780157f, -0.011535891f, 0.02754344f, -0.011535891f, -0.006780157f,
                    4.678331E-5f, -1.641984E-4f, -8.087284E-4f, 0.0055897464f, -0.0067771543f, -0.011530784f, 0.027531244f, -0.011530784f, -0.0067771543f,
                    4.6721198E-5f, -1.639804E-4f, -8.076546E-4f, 0.0055823238f, -0.006768156f, -0.011515473f, 0.02749469f, -0.011515473f, -0.006768156f,
            });
        }
        kernels.position(0);
        mcFilter = new MultiConvolutionFilter(kernels, 9, 9, numKernels);
        screen = new ScreenEndpoint(pipeline);
        input.addTarget(gFilter);
        gFilter.addTarget(mcFilter);
        mcFilter.addTarget(screen);
        pipeline.addRootRenderer(input);
        pipeline.startRendering();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((CameraPreviewInput) input).onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((CameraPreviewInput) input).onResume();
    }
}
