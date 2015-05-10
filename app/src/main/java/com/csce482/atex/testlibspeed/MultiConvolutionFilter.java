package com.csce482.atex.testlibspeed;

import java.nio.FloatBuffer;

import project.android.imageprocessing.filter.MultiPixelRenderer;


/**
 * Created by cloudburst on 4/19/15.
 */


/**
 * A basic convolution filter implementation of the MultiPixelRenderer.
 * This class works for convolution filterBuffer of any size; however, if the size is an even number,
 * the filter will favour the bottom right.
 * @author Chris Batt
 */
public class MultiConvolutionFilter extends MultiPixelRenderer {
    private FloatBuffer filterBuffer;
    private int filterSize;
    private int numFilters;
    private int width, height;

    /**
     * @param filters
     * The convolution filter that should be applied to each pixel in the input texture.
     * @param filterWidth
     * The width of the convolution filter.
     * @param filterHeight
     * The height of the convolution filter.
     */
    public MultiConvolutionFilter(FloatBuffer filters, int filterWidth, int filterHeight, int n) {
        super();
        this.filterBuffer = filters;
        filterSize = filterWidth*filterHeight;
        numFilters = n;
        this.width = filterWidth;
        this.height = filterHeight;
        setBackgroundColour(1.0f, 0.0f, 0.0f, 1.0f);
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

        program +=  "in vec2 "+VARYING_TEXCOORD+";\n";

        program +=  "float gray_vals["+getFilterSize()+"];\n";

        program +=  "void main(){\n";
        program +=  "   vec2 widthStep = vec2("+UNIFORM_TEXELWIDTH+", 0.0);\n";
        program +=  "   vec2 heightStep = vec2(0.0, "+UNIFORM_TEXELHEIGHT+");\n";

        program +=  "   float product = 0.0;\n";
        program +=  "   float energies["+numFilters+"];\n";


        // Get pixel values from texture
        for(int j = height-1; j >= 0; j--) {
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
                program += "   orientations[" + i + "] += energies[" + ( 6 * j + i)  + "];\n";
            }
        }

        program += "   float sgoed = 0.0;\n";
        for(int i = 1; i < 7; i++) {
            program += "   sgoed += abs(orientations[" + (i%6) + "] - orientations[" + (i-1) + "]);\n";
        }

        program +=  "   float orig_gray = gray_vals[41];\n";
        program +=  "   outcolor = vec4(orig_gray, orig_gray, orig_gray, 1.0);\n"+
                "   if(sgoed > 0.055) {\n"+
                "      outcolor = vec4(1.0, 0.0, 0.0, 1.0);\n"+
                "   }\n"+
                "   else if (sgoed > 0.030){\n"+
                "      outcolor = vec4(1.0, 0.2, 0.0, 1.0);\n"+
                "   }\n"+
                "   else if (sgoed > 0.025){\n"+
                "      outcolor = vec4(1.0, 0.4, 0.0, 1.0);\n"+
                "   }\n"+
                "   else if (sgoed > 0.018){\n"+
                "      outcolor = vec4(1.0, 0.6, 0.0, 1.0);\n"+
                "   }\n"+
                "   else if (sgoed > 0.012){\n"+
                "      outcolor = vec4(1.0, 0.8, 0.0, 1.0);\n"+
                "   }\n"+
                "   else if (sgoed > 0.006){\n"+
                "      outcolor = vec4(0.2, 0.7, 0.0, 1.0);\n"+
                "   }\n"+
                "   //else if (sgoed > 0.001){\n"+
                "   //   outcolor = vec4(0.2, 0.8, 0.0, 1.0);\n"+
                "   //}\n";

        program +=  "}\n";
        return program;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
    }
}
