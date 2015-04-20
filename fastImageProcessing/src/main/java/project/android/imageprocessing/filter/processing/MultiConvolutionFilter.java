package project.android.imageprocessing.filter.processing;

/**
 * Created by cloudburst on 4/19/15.
 */

import project.android.imageprocessing.filter.MultiPixelRenderer;
import android.opengl.GLES31;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * A basic convolution filter implementation of the MultiPixelRenderer.
 * This class works for convolution filters of any size; however, if the size is an even number,
 * the filter will favour the bottom right.
 * @author Chris Batt
 */
public class MultiConvolutionFilter extends MultiPixelRenderer {
    protected static final String UNIFORM_FILTER = "kern";
    private FloatBuffer filters;
    private ArrayList<Integer> filterHandles;
    private String filterBody;
    private int filterSize;
    private int numFilters;

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
        this.filters = filters;
        filterSize = filterWidth*filterHeight;
        filterBody = createFilterBody(filterWidth, filterHeight);
        filterHandles = new ArrayList<>();
        numFilters = n;
    }

    private String createFilterBody(int width, int height) {
        String filterBody = "   float gray_vals["+getFilterSize()+"];\n";
        filterBody += "    float product = 0.0;\n";
//		String filterBody = "   vec3 color = ";
        int middleWidth = (width-1)/2;
        int middleHeight = (height-1)/2;
        int grayindex = 0;
        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                filterBody += "   gray_vals["+grayindex+"] = texture("+UNIFORM_TEXTURE0+","+VARYING_TEXCOORD+" + widthStep * " + (i-middleWidth) + ".0 + heightStep * " + (j-middleHeight) + ".0).r;\n";
                grayindex++;
            }
        }
        int kernindex = 0;
        for (int i = 0 ; i < numFilters ; i++) {
            grayindex = 0;
            for(int j = 0; j < height; j++) {
                for(int k = 0; k < width; k++) {
                    filterBody += "   product += gray_vals["+grayindex+"] * kern["+kernindex+"];\n";
                    grayindex++;
                    kernindex++;
                }
            }
        }
        filterBody += "   gl_FragColor = vec4(product*100.0, product*100.0, product, 1.0);\n";//texture2D("+UNIFORM_TEXTURE0+","+VARYING_TEXCOORD+").a);\n";
        return filterBody;
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
        String program =
                "#version 300 es\n" +
                        "precision highp float;\n"
                        +"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"
                        +"uniform float "+UNIFORM_TEXELWIDTH+";\n"
                        +"uniform float "+UNIFORM_TEXELHEIGHT+";\n";
//        for (int i = 0 ; i < filters.size() ; ++i ) {
//            program +="uniform float "+UNIFORM_FILTER+i+"["+getFilterSize()+"];\n";
//        }
        program += "const int ARRAY_SIZE = "+(169*numFilters)+";\n" +
                    "uniform kern_array {\n" +
                    "    float kern[ARRAY_SIZE];\n" +
                    "};\n";
        program +="in vec2 "+VARYING_TEXCOORD+";\n";

        program +="void main(){\n";
        program +="   vec2 widthStep = vec2("+UNIFORM_TEXELWIDTH+", 0);\n";
        program +="   vec2 heightStep = vec2(0, "+UNIFORM_TEXELHEIGHT+");\n";
        program += filterBody;
        program +="}\n";
        return program;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
//        filterHandles.clear();
//        for (int i = 0 ; i < filters.size() ; ++i ) {
//            filterHandles.add(GLES31.glGetUniformLocation(programHandle, UNIFORM_FILTER+i));
//        }
        IntBuffer bufferObj = IntBuffer.allocate(1);
        GLES31.glGenBuffers(1, bufferObj);
        GLES31.glBindBuffer(GLES31.GL_UNIFORM_BUFFER, bufferObj.get(0));
        GLES31.glBufferData(GLES31.GL_UNIFORM_BUFFER, 169*numFilters*4, filters, GLES31.GL_DYNAMIC_DRAW);
        int glubI = GLES31.glGetUniformBlockIndex(programHandle, "kern_array");
        GLES31.glUniformBlockBinding(programHandle, glubI, 0);
        GLES31.glBindBufferBase(GLES31.GL_UNIFORM_BUFFER, 0, bufferObj.get(0));
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();
//        for (int i = 0 ; i < filterHandles.size() ; ++i ) {
//            GLES31.glUniform1fv(filterHandles.get(i), filterSize, filters.get(i), 0);
//        }
    }
}
