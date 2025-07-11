package be.tarsos.dsp.effects;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import java.util.Arrays;

public class ReverbEffect implements AudioProcessor {
    private float[] delayBuffer1, delayBuffer2, delayBuffer3, delayBuffer4;
    private int delayPos1, delayPos2, delayPos3, delayPos4;
    private int delayTime1, delayTime2, delayTime3, delayTime4;
    private float feedback1, feedback2, feedback3, feedback4;
    private float damping1, damping2, damping3, damping4;
    private float wetLevel, dryLevel;
    private float roomSizeScale;
    private float sampleRate;

    public static class Params {
        public float roomSize = 0.5f;
        public float damping = 0.2f;
        public float wet = 0.3f;
        public float dry = 0.7f;
    }
    private Params params;

    public ReverbEffect(float sampleRate, Params params) {
        this.sampleRate = sampleRate;
        this.params = params;
        initialize();
    }

    public void setParams(Params params) {
        this.params = params;
        initialize();
    }

    private void initialize() {
        roomSizeScale = params.roomSize;
        delayTime1 = (int) (sampleRate * 0.0297 * roomSizeScale);
        delayTime2 = (int) (sampleRate * 0.0371 * roomSizeScale);
        delayTime3 = (int) (sampleRate * 0.0411 * roomSizeScale);
        delayTime4 = (int) (sampleRate * 0.0437 * roomSizeScale);
        delayBuffer1 = new float[delayTime1 > 0 ? delayTime1 : 1];
        delayBuffer2 = new float[delayTime2 > 0 ? delayTime2 : 1];
        delayBuffer3 = new float[delayTime3 > 0 ? delayTime3 : 1];
        delayBuffer4 = new float[delayTime4 > 0 ? delayTime4 : 1];
        delayPos1 = delayPos2 = delayPos3 = delayPos4 = 0;
        feedback1 = feedback2 = feedback3 = feedback4 = 0.5f * roomSizeScale;
        damping1 = damping2 = damping3 = damping4 = params.damping;
        wetLevel = params.wet;
        dryLevel = params.dry;
        float totalLevel = wetLevel + dryLevel;
        if (totalLevel > 0) {
            wetLevel /= totalLevel;
            dryLevel /= totalLevel;
        }
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();
        float[] outputBuffer = new float[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            float input = buffer[i];
            float delayed1 = delayBuffer1[delayPos1] * (1.0f - damping1);
            float delayed2 = delayBuffer2[delayPos2] * (1.0f - damping2);
            float delayed3 = delayBuffer3[delayPos3] * (1.0f - damping3);
            float delayed4 = delayBuffer4[delayPos4] * (1.0f - damping4);
            float reverbSignal = (delayed1 + delayed2 + delayed3 + delayed4) * 0.25f;
            float feedbackSignal = reverbSignal * feedback1;
            delayBuffer1[delayPos1] = input + feedbackSignal;
            delayBuffer2[delayPos2] = input + feedbackSignal * 0.8f;
            delayBuffer3[delayPos3] = input + feedbackSignal * 0.6f;
            delayBuffer4[delayPos4] = input + feedbackSignal * 0.4f;
            delayPos1 = (delayPos1 + 1) % delayBuffer1.length;
            delayPos2 = (delayPos2 + 1) % delayBuffer2.length;
            delayPos3 = (delayPos3 + 1) % delayBuffer3.length;
            delayPos4 = (delayPos4 + 1) % delayBuffer4.length;
            outputBuffer[i] = input * dryLevel + reverbSignal * wetLevel;
        }
        System.arraycopy(outputBuffer, 0, buffer, 0, buffer.length);
        return true;
    }

    @Override
    public void processingFinished() {
        if (delayBuffer1 != null) Arrays.fill(delayBuffer1, 0.0f);
        if (delayBuffer2 != null) Arrays.fill(delayBuffer2, 0.0f);
        if (delayBuffer3 != null) Arrays.fill(delayBuffer3, 0.0f);
        if (delayBuffer4 != null) Arrays.fill(delayBuffer4, 0.0f);
        delayPos1 = delayPos2 = delayPos3 = delayPos4 = 0;
    }
}
