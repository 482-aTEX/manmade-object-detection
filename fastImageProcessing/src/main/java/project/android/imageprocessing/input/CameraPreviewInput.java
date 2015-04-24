package project.android.imageprocessing.input;import android.annotation.TargetApi;import android.graphics.SurfaceTexture;import android.graphics.SurfaceTexture.OnFrameAvailableListener;import android.hardware.Camera;import android.hardware.Camera.Parameters;import android.hardware.Camera.Size;import android.opengl.GLES11Ext;import android.opengl.GLES20;import android.opengl.GLSurfaceView;import android.util.Log;import java.io.PrintWriter;import java.io.StringWriter;import javax.microedition.khronos.opengles.GL10;/** * A Camera input extension of CameraPreviewInput.   * This class takes advantage of the android camera preview to produce new textures for processing. <p> * Note: This class requires an API level of at least 14.  To change camera parameters or get access to the * camera directly before it is used by this class, createCamera() can be override. * @author Chris Batt */@TargetApi(value = 14)public class CameraPreviewInput extends GLTextureOutputRenderer implements OnFrameAvailableListener {	private static final String UNIFORM_CAM_MATRIX = "u_Matrix";	private Camera camera;	private SurfaceTexture camTex;	private int matrixHandle;	private float[] matrix = new float[16];	private GLSurfaceView view;	/**	 * Creates a CameraPreviewInput which captures the camera preview with all the default camera parameters and settings.	 */	public CameraPreviewInput(GLSurfaceView view) {		super();		this.camera = createCamera();		this.view = view;	}	private void bindTexture() {		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);	    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture_in);	}	protected Camera createCamera() {		return Camera.open();	}	/* (non-Javadoc)	 * @see project.android.imageprocessing.input.GLTextureOutputRenderer#destroy()	 */	@Override	public void destroy() {		super.destroy();		if(camera != null) {			camera.stopPreview();			camera.release();			camera = null;		}		if(camTex != null) {			camTex.release();			camTex = null;		}		if(texture_in != 0) {			int[] tex = new int[1];			tex[0] = texture_in;			GLES20.glDeleteTextures(1, tex, 0);			texture_in = 0;		}	}	@Override	protected void drawFrame() {		camTex.updateTexImage(); 		super.drawFrame();	}	@Override	protected String getFragmentShader() {		return 					 "#extension GL_OES_EGL_image_external : require\n"					+"precision mediump float;\n"                         					+"uniform samplerExternalOES "+UNIFORM_TEXTURE0+";\n"  					+"varying vec2 "+VARYING_TEXCOORD+";\n"						 			+ "void main() {\n"		 			+ "   gl_FragColor = texture2D("+UNIFORM_TEXTURE0+", "+VARYING_TEXCOORD+");\n"		 			+ "}\n";	}	@Override	protected String getVertexShader() {		return 					"uniform mat4 "+UNIFORM_CAM_MATRIX+";\n"				  + "attribute vec4 "+ATTRIBUTE_POSITION+";\n"				  + "attribute vec2 "+ATTRIBUTE_TEXCOORD+";\n"					  + "varying vec2 "+VARYING_TEXCOORD+";\n"					  + "void main() {\n"					  + "   vec4 texPos = "+UNIFORM_CAM_MATRIX+" * vec4("+ATTRIBUTE_TEXCOORD+", 1, 1);\n"				  + "   "+VARYING_TEXCOORD+" = texPos.xy;\n"				  + "   gl_Position = "+ATTRIBUTE_POSITION+";\n"		                                            			 				  + "}\n";			}	@Override	protected void initShaderHandles() {		super.initShaderHandles();        matrixHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_CAM_MATRIX);    	}	@Override	protected void initWithGLContext() {		super.initWithGLContext();		int[] textures = new int[1];		GLES20.glGenTextures(1, textures, 0);		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);        		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);		texture_in = textures[0];		camTex = new SurfaceTexture(texture_in);		camTex.setOnFrameAvailableListener(this);		boolean failed = true;		while(failed) {				        try {	        	if(camera != null) {	        		camera.stopPreview();	        		camera.release();	        		camera = null;	        	}				camera = createCamera();                Parameters params = camera.getParameters();                int width = params.getSupportedPreviewSizes().get(4).width, height = params.getSupportedPreviewSizes().get(4).height;                params.setPreviewSize(width,height);                camera.setParameters(params);                camera.setPreviewTexture(camTex);		        camera.startPreview();				setRenderSizeToCameraSize();		        failed = false;			} catch (Exception e) {				StringWriter sw = new StringWriter();				PrintWriter pw = new PrintWriter(sw);				e.printStackTrace(pw);				Log.e("CameraInput", sw.toString());				if(camera != null) {					camera.release();					camera = null;				}			}		}	}	/* (non-Javadoc)	 * @see android.graphics.SurfaceTexture.OnFrameAvailableListener#onFrameAvailable(android.graphics.SurfaceTexture)	 */	@Override	public void onFrameAvailable(SurfaceTexture arg0) {		markAsDirty();		view.requestRender();	}	/**	 * Closes and releases the camera for other applications to use.	 * Should be called when the pause is called in the activity. 	 */	public void onPause() {		if(camera != null) {			camera.stopPreview();			camera.release();			camera = null;		}	}	/**	 * Re-initializes the camera and starts the preview again.	 * Should be called when resume is called in the activity.	 */	public void onResume() {		reInitialize();	}		@Override	protected void passShaderValues() {		renderVertices.position(0);		GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, renderVertices);  		GLES20.glEnableVertexAttribArray(positionHandle); 		textureVertices[curRotation].position(0);		GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureVertices[curRotation]);  		GLES20.glEnableVertexAttribArray(texCoordHandle); 		bindTexture();	    GLES20.glUniform1i(textureHandle, 0);	    camTex.getTransformMatrix(matrix);		GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0);	}		private void setRenderSizeToCameraSize() {		Parameters params = camera.getParameters();		Size previewSize = params.getPreviewSize();//        for (int i = 0; i < params.getSupportedPreviewFpsRange().size(); i++) {//            int[] fpsArr = params.getSupportedPreviewFpsRange().get(i);//            Log.d("previewFPS", fpsArr[0]+" - "+fpsArr[1]);//        }        for (int i = 0; i < params.getSupportedPreviewSizes().size(); i++) {            Log.d("previewSize", "Width: "+params.getSupportedPreviewSizes().get(i).width + " Height: " + params.getSupportedPreviewSizes().get(i).height);        }        camera.setDisplayOrientation(90);//        int[] fpsArr= {0,0};//        params.getPreviewFpsRange(fpsArr);//        Log.d("MYpreviewFPS", fpsArr[0]+" - "+fpsArr[1]);        setRenderSize(previewSize.width, previewSize.height);	}    public Size getPreviewSize() {        return camera.getParameters().getPreviewSize();    }}