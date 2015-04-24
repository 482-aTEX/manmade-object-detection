package project.android.imageprocessing.filter.processing;

/**
 * Created by cloudburst on 4/19/15.
 */

import android.opengl.GLES31;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import project.android.imageprocessing.filter.MultiPixelRenderer;

/**
 * A basic convolution filter implementation of the MultiPixelRenderer.
 * This class works for convolution filterBuffer of any size; however, if the size is an even number,
 * the filter will favour the bottom right.
 * @author Chris Batt
 */
public class MultiConvolutionFilter extends MultiPixelRenderer {
    protected static final String UNIFORM_FILTER = "kern";
    private FloatBuffer filterBuffer;
    private int filterSize;
    private int numFilters;
    private int width, height;
    private int image_width, image_height;

    /**
     * @param filters
     * The convolution filter that should be applied to each pixel in the input texture.
     * @param filterWidth
     * The width of the convolution filter.
     * @param filterHeight
     * The height of the convolution filter.
     */
    public MultiConvolutionFilter(FloatBuffer filters, int filterWidth, int filterHeight, int n, int imageWidth, int imageHeight) {
        super();
        this.filterBuffer = filters;
        filterSize = filterWidth*filterHeight;
        numFilters = n;
        this.width = filterWidth;
        this.height = filterHeight;
        this.image_width = imageWidth;
        this.image_height = imageHeight;
    }

    private int getFilterSize() {
        return filterSize;
    }

    @Override
    protected String getVertexShader() {
        return
                "#version 300 es\n" +
                        "in vec4 "+ATTRIBUTE_POSITION+";\n"
                        + "in vec2 "+ATTRIBUTE_TEXCOORD+";\n"
                        + "out vec2 "+VARYING_TEXCOORD+";\n"

                        + "void main() {\n"
                        + "  "+VARYING_TEXCOORD+" = "+ATTRIBUTE_TEXCOORD+";\n"
                        + "   gl_Position = "+ATTRIBUTE_POSITION+";\n"
                        + "}\n";
    }

    @Override
    protected String getFragmentShader() {
        int middleWidth = (width-1)/2;
        int middleHeight = (height-1)/2;
        int grayindex = 0;
        int kernindex = 0;

        String program = "";
        program +=  "#version 300 es\n" +
                    "precision highp float;\n" +
                    "uniform sampler2D "+UNIFORM_TEXTURE0+";\n" +
                    "uniform float "+UNIFORM_TEXELWIDTH+";\n" +
                    "uniform float "+UNIFORM_TEXELHEIGHT+";\n" +
                    "out vec4 outcolor;\n";

//        program +=  "const int ARRAY_SIZE = "+(169*numFilters)+";\n" +
//                    "uniform kern_array {\n" +
//                    "    float kern[ARRAY_SIZE];\n" +
//                    "};\n";
//
        program +=  "in vec2 "+VARYING_TEXCOORD+";\n";

        program +=  "float gray_vals["+getFilterSize()+"];\n";

        program +=  "void main(){\n";
        program +=  "   vec2 widthStep = vec2("+UNIFORM_TEXELWIDTH+", 0.0);\n";
        program +=  "   vec2 heightStep = vec2(0.0, "+UNIFORM_TEXELHEIGHT+");\n";

        program +=  "   float product = 0.0;\n";
        program +=  "   float energies[24];\n";


        // Get pixel values from texture
        for(int j = 0; j < height; j++) {
        for(int i = 0; i < width; i++) {
        program +=  "   gray_vals["+grayindex+"] = texture("+UNIFORM_TEXTURE0+","+VARYING_TEXCOORD+" + widthStep * " + (i-middleWidth) + ".0 + heightStep * " + (j-middleHeight) + ".0).r;\n";
        grayindex++;
        }}


        // convolve each kernel on pixel matrix and store energy values
        for (int i = 0 ; i < numFilters ; i++) {
        grayindex = 0;
        for(int j = 0; j < height; j++) {
        for(int k = 0; k < width; k++) {
        program +=  "   energies["+i+"] += (gray_vals["+grayindex+"] * " + filterBuffer.get(kernindex) + ");\n";//kern["+kernindex+"];\n";
        grayindex++;
        kernindex++;
        }
        }
        program +=  "   energies["+i+"] = energies["+i+"] * energies["+i+"];\n";
        }

        //Calculate SGOED

        program += "   float orientations[6];\n";
        for(int i = 0; i < 6; i++) {
            program += "   orientations[" + i + "] = 0.0;\n";
            for(int j = 0; j < 4; j++) {
                program += "   orientations[" + i + "] += energies[" + (i * 4 + j) + "];\n";
            }
        }

        program += "   float sgoed = 0.0;\n";
        for(int i = 1; i < 7; i++) {
            program += "   sgoed += abs(orientations[" + (i%6) + "] - orientations[" + (i-1) + "]);\n";
        }

        program +=  "   if(sgoed > 10.0) {\n"+
                    "      outcolor = vec4(1.0, 1.0, 1.0, 1.0);\n"+
                    "   }\n"+
                    "   else {\n"+
                    "      outcolor = vec4(0.3, 0.3, 0.3, 1.0);\n"+
                    "   }\n";

        program +=  "}\n";
        return program;
    }

    @Override
    protected void initShaderHandles() {

        //==================Create and Bind SSBO====================================
        /*FloatBuffer energyBuf = FloatBuffer.allocate(24*image_width*image_height);
        IntBuffer ssbo = IntBuffer.allocate(1);

        GLES31.glGenBuffers(1, ssbo);
        GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, ssbo.get(0));
        GLES31.glBufferData(GLES31.GL_SHADER_STORAGE_BUFFER, 24*image_width*image_height, energyBuf, GLES31.GL_DYNAMIC_COPY);

        int glsbI = GLES31.glGetUniformBlockIndex(programHandle, "gabor_vals");
        GLES31.glUniformBlockBinding(programHandle, glsbI, 0);
        GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 0, ssbo.get(0));*/
        //==========================================================================

        super.initShaderHandles();
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
    }
}
