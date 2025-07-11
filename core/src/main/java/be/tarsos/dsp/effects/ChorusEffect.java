package be.tarsos.dsp.effects;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

public class ChorusEffect implements AudioProcessor {
    private float rate; // Hz
    private float depth; // ms
    private float mix;
    private float sampleRate;
    private float[] delayBuffer;
    private int delayBufferPos;
    private int maxDelaySamples;
    private float lfoPhase;

    public ChorusEffect(float sampleRate, float rate, float depth, float mix) {
        this.sampleRate = sampleRate;
        this.rate = rate;
        this.depth = depth;
        this.mix = mix;
        this.maxDelaySamples = (int)(depth * sampleRate / 1000.0f) + 2;
        this.delayBuffer = new float[maxDelaySamples * 2];
        this.delayBufferPos = 0;
        this.lfoPhase = 0;
    }

    public void setParams(float rate, float depth, float mix) {
        this.rate = rate;
        this.depth = depth;
        this.mix = mix;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();
        for (int i = 0; i < buffer.length; i++) {
            float dry = buffer[i];
            // LFO for modulated delay
            float lfo = (float)Math.sin(2 * Math.PI * lfoPhase);
            float delayMs = depth * (0.5f * (lfo + 1));
            float delaySamples = delayMs * sampleRate / 1000.0f;
            int readPos = (int)(delayBufferPos - delaySamples + delayBuffer.length) % delayBuffer.length;
            float delayed = delayBuffer[readPos];
            float wet = delayed;
            buffer[i] = dry * (1 - mix) + wet * mix;
            delayBuffer[delayBufferPos] = dry;
            delayBufferPos = (delayBufferPos + 1) % delayBuffer.length;
            lfoPhase += rate / sampleRate;
            if (lfoPhase > 1) lfoPhase -= 1;
        }
        return true;
    }

    @Override
    public void processingFinished() {
        for (int i = 0; i < delayBuffer.length; i++) delayBuffer[i] = 0;
        delayBufferPos = 0;
        lfoPhase = 0;
    }
} 