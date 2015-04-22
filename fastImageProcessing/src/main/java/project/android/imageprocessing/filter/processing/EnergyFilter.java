package project.android.imageprocessing.filter.processing;

import project.android.imageprocessing.filter.MultiPixelRenderer;

/**
 * Created by Kyle on 4/20/2015.
 */
public class EnergyFilter extends MultiPixelRenderer {
    private int image_width, image_height;
    private int window_width, window_height;

    /**
     * @param imageWidth
     * The width of the convolution filter.
     * @param imageHeight
     * The height of the convolution filter.
     */
    public EnergyFilter(int imageWidth, int imageHeight, int windowWidth, int windowHeight) {
        super();
        this.image_width = imageWidth;
        this.image_height = imageHeight;
        this.window_width = windowWidth;
        this.window_height = windowHeight;
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

        String program = "";

        program += "buffer energy_map {\n" +
                "\n" +
                "    float characteristics[24];\n" +
                "};";

        program += "buffer gabor_vals {\n" +
                "\n" +
                "    float gabor[24 * " + image_width * image_height + "];\n" +
                "};";

        program += "for ( int i = 0; i < 24; i++) {\n" +
                "\tfor ( int n = 0; n < " + image_height / window_height + "; n++) {\n" +
                "\t\tfor (int j = 0; j < " + image_width / window_width + "; j++) {\n" +
                "\t\t\tfor (int m = 0; m < " + window_height + "; m++ ) {\n" +
                "\t\t\t\tfor (int k = 0; k < " + window_width + "; k++ ) {\n" +
                "\t\t\t\t\tcharacteristics[ i + (24 * j + (n * " + image_width / window_width + ")) ] =\n" +
                "\t\t\t\t\t\tgabor[\n" +
                "\t\t\t\t\t\t\ti * " + image_width * image_height  + "\n" +
                "\t\t\t\t\t\t\t+ k + j * " + window_width + "\n" +
                "\t\t\t\t\t\t\t+ m * " + image_width + "\n" +
                "\t\t\t\t\t\t\t+ n * " + window_height * image_width + "\n" +
                "\t\t\t\t\t\t];\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        return program;
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();

        /*FloatBuffer energyBuf = FloatBuffer.allocate(24*image_width*image_height*4);
        IntBuffer ssbo = IntBuffer.allocate(1);

        GLES31.glGenBuffers(1, ssbo);
        GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, ssbo.get(0));
        GLES31.glBufferData(GLES31.GL_SHADER_STORAGE_BUFFER, 24*image_width*image_height*4, energyBuf, GLES31.GL_DYNAMIC_COPY);

        int glsbI = GLES31.glGetUniformBlockIndex(programHandle, "gabor_vals");
        GLES31.glUniformBlockBinding(programHandle, glsbI, 0);
        GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 0, ssbo.get(0));*/
    }

    @Override
    protected void passShaderValues() {
        super.passShaderValues();

    }
}
