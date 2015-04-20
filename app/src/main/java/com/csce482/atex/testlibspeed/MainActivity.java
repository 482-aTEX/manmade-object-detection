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
        int numKernels = 2;
        FloatBuffer kernels = FloatBuffer.allocate(169*numKernels);
        for (int i = 0 ; i < numKernels ; i++) {
            kernels.put(new float[] { 4.6063702E-5f, -1.616728E-4f, -7.9628895E-4f, 0.0055037676f, -0.006672912f, -0.011353423f, 0.027107773f, -0.011353423f, -0.006672912f, 0.0055037676f, -7.9628895E-4f, -1.616728E-4f, 4.6063702E-5f,
                    4.628866E-5f, -1.6246234E-4f, -8.0017775E-4f, 0.005530646f, -0.0067055f, -0.011408869f, 0.027240157f, -0.011408869f, -0.0067055f, 0.005530646f, -8.0017775E-4f, -1.6246234E-4f, 4.628866E-5f,
                    4.6473528E-5f, -1.6311118E-4f, -8.0337346E-4f, 0.0055527347f, -0.0067322813f, -0.011454436f, 0.027348952f, -0.011454436f, -0.0067322813f, 0.0055527347f, -8.0337346E-4f, -1.6311118E-4f, 4.6473528E-5f,
                    4.6617843E-5f, -1.636177E-4f, -8.058682E-4f, 0.0055699763f, -0.006753185f, -0.011490001f, 0.027433872f, -0.011490001f, -0.006753185f, 0.0055699763f, -8.058682E-4f, -1.636177E-4f, 4.6617843E-5f,
                    4.6721198E-5f, -1.639804E-4f, -8.076546E-4f, 0.0055823238f, -0.006768156f, -0.011515473f, 0.02749469f, -0.011515473f, -0.006768156f, 0.0055823238f, -8.076546E-4f, -1.639804E-4f, 4.6721198E-5f,
                    4.678331E-5f, -1.641984E-4f, -8.087284E-4f, 0.0055897464f, -0.0067771543f, -0.011530784f, 0.027531244f, -0.011530784f, -0.0067771543f, 0.0055897464f, -8.087284E-4f, -1.641984E-4f, 4.678331E-5f,
                    4.6804023E-5f, -1.6427114E-4f, -8.090867E-4f, 0.0055922223f, -0.006780157f, -0.011535891f, 0.02754344f, -0.011535891f, -0.006780157f, 0.0055922223f, -8.090867E-4f, -1.6427114E-4f, 4.6804023E-5f,
                    4.678331E-5f, -1.641984E-4f, -8.087284E-4f, 0.0055897464f, -0.0067771543f, -0.011530784f, 0.027531244f, -0.011530784f, -0.0067771543f, 0.0055897464f, -8.087284E-4f, -1.641984E-4f, 4.678331E-5f,
                    4.6721198E-5f, -1.639804E-4f, -8.076546E-4f, 0.0055823238f, -0.006768156f, -0.011515473f, 0.02749469f, -0.011515473f, -0.006768156f, 0.0055823238f, -8.076546E-4f, -1.639804E-4f, 4.6721198E-5f,
                    4.6617843E-5f, -1.636177E-4f, -8.058682E-4f, 0.0055699763f, -0.006753185f, -0.011490001f, 0.027433872f, -0.011490001f, -0.006753185f, 0.0055699763f, -8.058682E-4f, -1.636177E-4f, 4.6617843E-5f,
                    4.6473528E-5f, -1.6311118E-4f, -8.0337346E-4f, 0.0055527347f, -0.0067322813f, -0.011454436f, 0.027348952f, -0.011454436f, -0.0067322813f, 0.0055527347f, -8.0337346E-4f, -1.6311118E-4f, 4.6473528E-5f,
                    4.628866E-5f, -1.6246234E-4f, -8.0017775E-4f, 0.005530646f, -0.0067055f, -0.011408869f, 0.027240157f, -0.011408869f, -0.0067055f, 0.005530646f, -8.0017775E-4f, -1.6246234E-4f, 4.628866E-5f,
                    4.6063702E-5f, -1.616728E-4f, -7.9628895E-4f, 0.0055037676f, -0.006672912f, -0.011353423f, 0.027107773f, -0.011353423f, -0.006672912f, 0.0055037676f, -7.9628895E-4f, -1.616728E-4f, 4.6063702E-5f,
            });
        }
        kernels.position(0);
        mcFilter = new MultiConvolutionFilter(kernels, 13, 13, numKernels);

        cFilter = new ConvolutionFilter(new float[] {
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,1/25f,}, 13, 13);

                4.6063702E-5f, -1.616728E-4f, -7.9628895E-4f, 0.0055037676f, -0.006672912f, -0.011353423f, 0.027107773f, -0.011353423f, -0.006672912f, 0.0055037676f, -7.9628895E-4f, -1.616728E-4f, 4.6063702E-5f,
                4.628866E-5f, -1.6246234E-4f, -8.0017775E-4f, 0.005530646f, -0.0067055f, -0.011408869f, 0.027240157f, -0.011408869f, -0.0067055f, 0.005530646f, -8.0017775E-4f, -1.6246234E-4f, 4.628866E-5f,
                4.6473528E-5f, -1.6311118E-4f, -8.0337346E-4f, 0.0055527347f, -0.0067322813f, -0.011454436f, 0.027348952f, -0.011454436f, -0.0067322813f, 0.0055527347f, -8.0337346E-4f, -1.6311118E-4f, 4.6473528E-5f,
                4.6617843E-5f, -1.636177E-4f, -8.058682E-4f, 0.0055699763f, -0.006753185f, -0.011490001f, 0.027433872f, -0.011490001f, -0.006753185f, 0.0055699763f, -8.058682E-4f, -1.636177E-4f, 4.6617843E-5f,
                4.6721198E-5f, -1.639804E-4f, -8.076546E-4f, 0.0055823238f, -0.006768156f, -0.011515473f, 0.02749469f, -0.011515473f, -0.006768156f, 0.0055823238f, -8.076546E-4f, -1.639804E-4f, 4.6721198E-5f,
                4.678331E-5f, -1.641984E-4f, -8.087284E-4f, 0.0055897464f, -0.0067771543f, -0.011530784f, 0.027531244f, -0.011530784f, -0.0067771543f, 0.0055897464f, -8.087284E-4f, -1.641984E-4f, 4.678331E-5f,
                4.6804023E-5f, -1.6427114E-4f, -8.090867E-4f, 0.0055922223f, -0.006780157f, -0.011535891f, 0.02754344f, -0.011535891f, -0.006780157f, 0.0055922223f, -8.090867E-4f, -1.6427114E-4f, 4.6804023E-5f,
                4.678331E-5f, -1.641984E-4f, -8.087284E-4f, 0.0055897464f, -0.0067771543f, -0.011530784f, 0.027531244f, -0.011530784f, -0.0067771543f, 0.0055897464f, -8.087284E-4f, -1.641984E-4f, 4.678331E-5f,
                4.6721198E-5f, -1.639804E-4f, -8.076546E-4f, 0.0055823238f, -0.006768156f, -0.011515473f, 0.02749469f, -0.011515473f, -0.006768156f, 0.0055823238f, -8.076546E-4f, -1.639804E-4f, 4.6721198E-5f,
                4.6617843E-5f, -1.636177E-4f, -8.058682E-4f, 0.0055699763f, -0.006753185f, -0.011490001f, 0.027433872f, -0.011490001f, -0.006753185f, 0.0055699763f, -8.058682E-4f, -1.636177E-4f, 4.6617843E-5f,
                4.6473528E-5f, -1.6311118E-4f, -8.0337346E-4f, 0.0055527347f, -0.0067322813f, -0.011454436f, 0.027348952f, -0.011454436f, -0.0067322813f, 0.0055527347f, -8.0337346E-4f, -1.6311118E-4f, 4.6473528E-5f,
                4.628866E-5f, -1.6246234E-4f, -8.0017775E-4f, 0.005530646f, -0.0067055f, -0.011408869f, 0.027240157f, -0.011408869f, -0.0067055f, 0.005530646f, -8.0017775E-4f, -1.6246234E-4f, 4.628866E-5f,
                4.6063702E-5f, -1.616728E-4f, -7.9628895E-4f, 0.0055037676f, -0.006672912f, -0.011353423f, 0.027107773f, -0.011353423f, -0.006672912f, 0.0055037676f, -7.9628895E-4f, -1.616728E-4f, 4.6063702E-5f,}, 13, 13);

        screen = new ScreenEndpoint(pipeline);
        input.addTarget(gFilter);
        gFilter.addTarget(mcFilter);
        mcFilter.addTarget(screen);
//        cFilter.addTarget(cFilter2);
//        cFilter2.addTarget(screen);
        pipeline.addRootRenderer(input);
        pipeline.startRendering();
//        filters = new ArrayList<BasicFilter>();
//
//        addFilter(new FlipFilter(FlipFilter.FLIP_HORIZONTAL));
//        addFilter(new CGAColourSpaceFilter());
//        addFilter(new KuwaharaRadius3Filter());
//        //addFilter(new KuwaharaFilter(8)); //Will not work on devices that don't support for loop on shader
//        addFilter(new VignetteFilter(new PointF(0.5f, 0.5f), new float[] {0.3f, 0.8f, 0.3f}, 0.3f, 0.75f));
//        addFilter(new GlassSphereFilter(new PointF(0.43f, 0.5f), 0.25f, 0.71f, 0.5f));
//        addFilter(new SphereRefractionFilter(new PointF(0.43f, 0.5f), 0.25f, 0.71f, 0.5f));
//        addFilter(new StretchDistortionFilter(new PointF(0.5f, 0.5f)));
//        addFilter(new PinchDistortionFilter(new PointF(0.43f, 0.5f), 0.25f, 0.5f, 0.5f));
//        addFilter(new BulgeDistortionFilter(new PointF(0.43f, 0.5f), 0.25f, 0.5f, 0.5f));
//        addFilter(new SwirlFilter(new PointF(0.4f, 0.5f), 0.5f, 1f));
//        addFilter(new PosterizeFilter(2f));
//        addFilter(new EmbossFilter(1.5f));
//        addFilter(new SmoothToonFilter(0.25f, 0.5f, 5f));
//        addFilter(new ToonFilter(0.4f, 10f));
//        addFilter(new ThresholdSketchFilter(0.7f));
//        addFilter(new SketchFilter());
//        addFilter(new CrosshatchFilter(0.005f, 0.0025f));
//        addFilter(new HalftoneFilter(0.01f, 1f));
//        addFilter(new PolkaDotFilter(0.9f, 0.03f, 1f));
//        addFilter(new PolarPixellateFilter(new PointF(0.4f, 0.5f), new PointF(0.05f, 0.05f)));
//        addFilter(new PixellateFilter(0.01f, 1f));
//        addFilter(new ZoomBlurFilter(2f, new PointF(0.4f, 0.5f)));
//        addFilter(new MotionBlurFilter(2f, 45f));
//        addFilter(new OpeningFilter(1));
//        addFilter(new OpeningRGBFilter(3));
//        addFilter(new ClosingFilter(2));
//        addFilter(new ClosingRGBFilter(4));
//        addFilter(new ErosionRGBFilter(3));
//        addFilter(new ErosionFilter(1));
//        addFilter(new DilationRGBFilter(2));
//        addFilter(new DilationFilter(4));
//        addFilter(new CannyEdgeDetectionFilter(1.0f, 0.1f, 0.4f));
//        addFilter(new ThresholdEdgeDetectionFilter(0.6f));
//        addFilter(new SobelEdgeDetectionFilter());
//        addFilter(new TiltShiftFilter(4f, 0.4f, 0.6f, 0.2f));
//        addFilter(new BilateralBlurFilter(1f));
//        addFilter(new MedianFilter());
//        addFilter(new GaussianBlurPositionFilter(4f, 1.2f, new PointF(0.4f,0.5f), 0.5f, 0.1f));
//        addFilter(new GaussianSelectiveBlurFilter(4f, 1.2f, new PointF(0.4f,0.5f), 0.5f, 0.1f));
//        addFilter(new SingleComponentGaussianBlurFilter(2.3f));
//        addFilter(new SingleComponentFastBlurFilter());
//        addFilter(new FastBlurFilter());
//        addFilter(new UnsharpMaskFilter(2.0f, 0.5f));
//        addFilter(new SharpenFilter(1f));
//        //addFilter(new LanczosResamplingFilter(256, 128));
//        addFilter(new CropFilter(0.25f,0f,0.75f,1f));
//        BasicFilter cFilter1 = new CropFilter(0.25f,0f,0.75f,1f);
//        cFilter1.rotateClockwise90Degrees(1);
//        addFilter(cFilter1);
//        BasicFilter cFilter2 = new CropFilter(0.25f,0f,0.75f,1f);
//        cFilter2.rotateClockwise90Degrees(2);
//        addFilter(cFilter2);
//        BasicFilter cFilter3 = new CropFilter(0.25f,0f,0.75f,1f);
//        cFilter3.rotateClockwise90Degrees(3);
//        addFilter(cFilter3);
//        addFilter(new TransformFilter(new float[] {
//                1f, 0f, 0f, 0f,
//                0f, 1f, 0f, 0f,
//                0f, 0f, 1f, 0f,
//                -0.5f, 0f, 0f, 1f
//        }, false, false));
//        addFilter(new ChromaKeyFilter(new float[] {1.0f, 0.3f, 0.0f}, 0.4f, 0.1f));
//        addFilter(new AdaptiveThresholdFilter());
//        addFilter(new BoxBlurFilter());
//        addFilter(new LuminanceThresholdFilter(0.4f));
//        addFilter(new OpacityFilter(0.5f));
//        addFilter(new SepiaFilter());
//        addFilter(new HazeFilter(0.3f,0.1f));
//        addFilter(new FalseColourFilter(new float[]{0.0f,0.0f,0.5f}, new float[]{1.0f,0.0f,0.0f}));
//        addFilter(new MonochromeFilter(new float[]{1.0f,0.8f,0.8f}, 1.0f));
//        addFilter(new ColourInvertFilter());
//        addFilter(new SoftEleganceFilter(this));
//        addFilter(new GaussianBlurFilter(2.3f));
//        addFilter(new MissEtikateFilter(this));
//        addFilter(new AmatorkaFilter(this));
//        addFilter(new LookupFilter(this, R.drawable.lookup_soft_elegance_1));
//        addFilter(new HighlightShadowFilter(0f, 1f));
//        Point[] defaultCurve = new Point[] {new Point(128,128), new Point(64,0), new Point(192,255)};
//        addFilter(new ToneCurveFilter(defaultCurve,defaultCurve,defaultCurve,defaultCurve));
//        addFilter(new HueFilter(3.14f/6f));
//        addFilter(new BrightnessFilter(0.5f));
//        addFilter(new ColourMatrixFilter(new float[]{	0.33f,0f,0f,0f,
//                0f,0.67f,0f,0f,
//                0f,0f,1.34f,0f,
//                0.2f,0.2f,0.2f,1.0f}, 0.5f));
//        addFilter(new RGBFilter(0.33f,0.67f,1.34f));
//        addFilter(new GreyScaleFilter());
//        addFilter(new ConvolutionFilter(new float[] {
//                1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f,
//                1/25f,1/25f,1/25f,1/25f,1/25f}, 5, 5));
//        addFilter(new ExposureFilter(0.95f));
//        addFilter(new ContrastFilter(1.5f));
//        addFilter(new SaturationFilter(0.5f));
//        addFilter(new GammaFilter(1.75f));
//        addFilter(new LevelsFilter(0.2f,0.8f,1f,0f,1f));
//
//        screen = new ScreenEndpoint(pipeline);
//
//        input.addTarget(screen);
//        for(BasicFilter filter : filters) {
//            filter.addTarget(screen);
//        }
//
//        pipeline.addRootRenderer(input);
//        pipeline.startRendering();
//        final Context context = this;
//        view.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View v, MotionEvent e) {
//                if(System.currentTimeMillis() - touchTime > 100) {
//                    pipeline.pauseRendering();
//                    touchTime = System.currentTimeMillis();
//                    if(curFilter == 0) {
//                        input.removeTarget(screen);
//                    } else {
//                        input.removeTarget(filters.get(curFilter-1));
//                        pipeline.addFilterToDestroy(filters.get(curFilter-1));
//                    }
//                    curFilter=(curFilter+1)%(filters.size()+1);
//                    if(curFilter == 0) {
//                        input.addTarget(screen);
//                    } else {
//                        input.addTarget(filters.get(curFilter-1));
//                        Toast.makeText(context, filters.get(curFilter - 1).getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
//                    }
//                    pipeline.startRendering();
//                    view.requestRender();
//                }
//                return false;
//            }
//        });
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
