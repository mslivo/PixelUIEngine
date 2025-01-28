package net.mslivo.core.engine.tools.misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class ComputeShader {

    public static final String DATA_IN = "dataIn";
    public static final String DATA_OUT = "dataOut";

    private static final String COMPUTE_SHADER = """
            #version 320 es
            precision highp float;
            
            // Define the workgroup size
            layout(local_size_x = 256) in;
            
            // Input and output buffers
            layout(std430, binding = 0) buffer InputBuffer {
                float %s[];
            };
            
            layout(std430, binding = 1) buffer OutputBuffer {
                float %s[];
            };
            
            %s
            
            %s
            """;

    private int currentProgram, programs;
    private int inputBufferHandle = -1, outputBufferHandle = -1;
    private int computeShaderIDs[] = null, computeProgramIDs[] = null;

    private int outputLength;
    private float[] inputData;
    private FloatBuffer inputBuffer;
    private ByteBuffer outputBuffer;

    private ArrayList<Uniform> uniforms;
    private String uniFormSource;
    private ObjectIntMap<Uniform> iUniformValues;
    private ObjectFloatMap<Uniform> fUniformValues;

    public enum UniformType {
        Integer, Float
    }

    public static class Uniform {

        public final UniformType type;
        public final String name;
        public boolean locationCached;
        public int location;

        public Uniform(String name, UniformType type) {
            this.name = name;
            this.type = type;
            this.locationCached = false;
            this.location = -1;
        }

        public Uniform(Uniform uniform) {
            this(uniform.name, uniform.type);
        }
    }

    private void initUniforms(Uniform[] uniforms) {
        this.iUniformValues = new ObjectIntMap<>();
        this.fUniformValues = new ObjectFloatMap<>();
        this.uniforms = new ArrayList<>();
        StringBuilder uniformSourceBuilder = new StringBuilder();

        if (uniforms != null) {
            for (int i = 0; i < uniforms.length; i++) {
                switch (uniforms[i].type) {
                    case Float -> {
                        fUniformValues.put(uniforms[i], 0f);
                        uniformSourceBuilder.append("uniform float " + uniforms[i].name + ";");
                    }
                    case Integer -> {
                        iUniformValues.put(uniforms[i], 0);
                        uniformSourceBuilder.append("uniform int " + uniforms[i].name + ";");
                    }
                }
                uniformSourceBuilder.append(System.lineSeparator());
                this.uniforms.add(new Uniform(uniforms[i]));
            }
        }
        this.uniFormSource = uniformSourceBuilder.toString();
    }

    public ComputeShader(int inputBufferSize, Uniform[] uniforms, String[] mainMethods) {
        this.initUniforms(uniforms);
        this.initShaders(mainMethods);
        this.resizeInputBuffer(inputBufferSize);
        this.initGLBuffers();
        this.setCurrentProgram(0);
    }

    public void initShaders(String[] mainMethods) {
        if (mainMethods == null)
            mainMethods = new String[]{};

        this.programs = mainMethods.length;
        this.computeShaderIDs = new int[this.programs];
        this.computeProgramIDs = new int[this.programs];

        for (int i = 0; i < this.programs; i++) {
            // Create and compile compute shader
            String shaderSource = String.format(COMPUTE_SHADER, DATA_IN, DATA_OUT, this.uniFormSource, mainMethods[i]);
            this.computeShaderIDs[i] = Gdx.gl32.glCreateShader(Gdx.gl32.GL_COMPUTE_SHADER);
            Gdx.gl32.glShaderSource(this.computeShaderIDs[i], shaderSource);
            Gdx.gl32.glCompileShader(this.computeShaderIDs[i]);


            // Check shader compile status
            IntBuffer compileStatus = BufferUtils.newIntBuffer(1);
            Gdx.gl32.glGetShaderiv(computeShaderIDs[i], Gdx.gl32.GL_COMPILE_STATUS, compileStatus);
            if (compileStatus.get(0) == 0) {
                String log = Gdx.gl32.glGetShaderInfoLog(computeShaderIDs[i]);
                throw new RuntimeException("Compute shader compilation failed:\n" + log);
            }

            // Create and link program
            this.computeProgramIDs[i] = Gdx.gl32.glCreateProgram();
            Gdx.gl32.glAttachShader(this.computeProgramIDs[i], computeShaderIDs[i]);
            Gdx.gl32.glLinkProgram(this.computeProgramIDs[i]);

            // Check program link status
            IntBuffer linkStatus = BufferUtils.newIntBuffer(1);
            Gdx.gl32.glGetProgramiv(this.computeProgramIDs[i], Gdx.gl32.GL_LINK_STATUS, linkStatus);
            if (linkStatus.get(0) == 0) {
                String log = Gdx.gl32.glGetProgramInfoLog(this.computeProgramIDs[i]);
                throw new RuntimeException("Program linking failed:\n" + log);
            }

        }


    }

    private void initGLBuffers() {
        // Generate buffer handles
        this.inputBufferHandle = Gdx.gl.glGenBuffer();
        this.outputBufferHandle = Gdx.gl.glGenBuffer();

        // Bind buffers to storage bindings
        Gdx.gl32.glBindBufferBase(Gdx.gl32.GL_SHADER_STORAGE_BUFFER, 0, this.inputBufferHandle);
        Gdx.gl32.glBindBufferBase(Gdx.gl32.GL_SHADER_STORAGE_BUFFER, 1, this.outputBufferHandle);



    }


    public void runCompute() {


        // Ensure the input buffer is updated with new data
        this.inputBuffer.put(inputData).flip();



        Gdx.gl32.glBindBuffer(Gdx.gl32.GL_SHADER_STORAGE_BUFFER, this.inputBufferHandle);
        Gdx.gl32.glBufferData(Gdx.gl32.GL_SHADER_STORAGE_BUFFER, inputData.length * Float.BYTES, this.inputBuffer, Gdx.gl32.GL_DYNAMIC_DRAW);

        Gdx.gl32.glBindBuffer(Gdx.gl32.GL_SHADER_STORAGE_BUFFER, this.outputBufferHandle);
        Gdx.gl32.glBufferData(Gdx.gl32.GL_SHADER_STORAGE_BUFFER, inputData.length * Float.BYTES, null, Gdx.gl32.GL_DYNAMIC_READ);



        // Bind and set the compute program
        Gdx.gl32.glUseProgram(computeProgramIDs[this.currentProgram]);

        for (Uniform uniform : uniforms) {
            if (!uniform.locationCached) {
                uniform.location = Gdx.gl32.glGetUniformLocation(computeProgramIDs[this.currentProgram], uniform.name);
                uniform.locationCached = true;
            }
            switch (uniform.type) {
                case Float -> Gdx.gl32.glUniform1f(uniform.location, fUniformValues.get(uniform, 0f));
                case Integer -> Gdx.gl32.glUniform1i(uniform.location, iUniformValues.get(uniform, 0));
            }
        }

        // Dispatch compute shader with optimal workgroup size
        int numGroups = (inputData.length + 255) / 256;
        Gdx.gl32.glDispatchCompute(numGroups, 1, 1);


        // Synchronize to ensure shader execution is complete
        Gdx.gl32.glMemoryBarrier(Gdx.gl32.GL_SHADER_STORAGE_BARRIER_BIT);

        // Read back the output buffer safely
        Gdx.gl32.glBindBuffer(Gdx.gl32.GL_SHADER_STORAGE_BUFFER, this.outputBufferHandle);


        // Use glMapBufferRange with proper flags for read-only access
        this.outputBuffer = (ByteBuffer) Gdx.gl32.glMapBufferRange(
                Gdx.gl32.GL_SHADER_STORAGE_BUFFER,
                0,
                inputData.length * Float.BYTES,
                Gdx.gl32.GL_MAP_READ_BIT
        );



    }

    public void setUniform(Uniform uniform, int value) {
        if (iUniformValues.containsKey(uniform))
            iUniformValues.put(uniform, value);
    }

    public void setUniform(Uniform uniform, float value) {
        if (fUniformValues.containsKey(uniform))
            fUniformValues.put(uniform, value);
    }

    public void resizeInputBuffer(int inputBufferSize) {
        this.inputBuffer = BufferUtils.newFloatBuffer(inputBufferSize);
        this.inputData = new float[inputBufferSize];
    }

    public float[] getInputData() {
        return inputData;
    }

    public void setInputData(int index, float value) {
        this.inputData[index] = value;
    }


    public float getOutput(int offset) {
        return outputBuffer.getFloat(offset * Float.BYTES);
    }

    public int getOutputLength() {
        return outputLength;
    }

    public ByteBuffer getOutputBuffer() {
        return outputBuffer;
    }

    public void setCurrentProgram(int currentProgram) {
        this.currentProgram = Math.clamp(currentProgram, 0, this.programs - 1);
    }

    public void dispose() {
        Gdx.gl32.glDeleteBuffer(inputBufferHandle);
        Gdx.gl32.glDeleteBuffer(outputBufferHandle);

        for (int i = 0; i < this.programs; i++) {
            Gdx.gl32.glDeleteProgram(computeProgramIDs[i]);
            Gdx.gl32.glDeleteShader(computeShaderIDs[i]);
        }

    }

}
