package be.tarsos.dsp.effects;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

public class PolyphonicPhaseShifterEffect implements AudioProcessor {
    private int stages;
    private float rate;
    private float depth;
    private float mix;
    private float sampleRate;
    private float[] lfoPhases;
    private float[][] apfBuffers;
    private float[] apfCoeffs;

    public PolyphonicPhaseShifterEffect(float sampleRate, int stages, float rate, float depth, float mix) {
        this.sampleRate = sampleRate;
        this.stages = stages;
        this.rate = rate;
        this.depth = depth;
        this.mix = mix;
        this.lfoPhases = new float[stages];
        this.apfBuffers = new float[stages][2]; // [][0]=in, [][1]=out
        this.apfCoeffs = new float[stages];
        for (int i = 0; i < stages; i++) {
            lfoPhases[i] = (float)i / stages;
            apfBuffers[i][0] = apfBuffers[i][1] = 0;
            apfCoeffs[i] = 0;
        }
    }

    public void setParams(int stages, float rate, float depth, float mix) {
        this.stages = stages;
        this.rate = rate;
        this.depth = depth;
        this.mix = mix;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();
        for (int i = 0; i < buffer.length; i++) {
            float dry = buffer[i];
            float x = dry;
            for (int s = 0; s < stages; s++) {
                float lfo = (float)Math.sin(2 * Math.PI * lfoPhases[s]);
                float coeff = 0.5f * (1 + lfo) * depth;
                apfCoeffs[s] = coeff;
                float y = coeff * (x - apfBuffers[s][1]) + apfBuffers[s][0];
                apfBuffers[s][0] = x;
                apfBuffers[s][1] = y;
                x = y;
                lfoPhases[s] += rate / sampleRate;
                if (lfoPhases[s] > 1) lfoPhases[s] -= 1;
            }
            float wet = x;
            buffer[i] = dry * (1 - mix) + wet * mix;
        }
        return true;
    }

    @Override
    public void processingFinished() {
        for (int s = 0; s < stages; s++) {
            apfBuffers[s][0] = apfBuffers[s][1] = 0;
            lfoPhases[s] = (float)s / stages;
            apfCoeffs[s] = 0;
        }
    }
} 