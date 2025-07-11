package be.tarsos.dsp.effects;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

public class DistortionEffect implements AudioProcessor {
    public enum Type {
        SOFT_CLIP, HARD_CLIP, TUBE, FUZZ, OVERDRIVE, METAL
    }

    private Type type;
    private float gain;
    private float mix;

    public DistortionEffect(Type type, float gain, float mix) {
        this.type = type;
        this.gain = gain;
        this.mix = mix;
    }

    public void setType(Type type) { this.type = type; }
    public void setGain(float gain) { this.gain = gain; }
    public void setMix(float mix) { this.mix = mix; }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();
        for (int i = 0; i < buffer.length; i++) {
            float dry = buffer[i];
            float wet = 0;
            float x = dry * gain;
            switch (type) {
                case SOFT_CLIP:
                    wet = (float) (Math.tanh(x));
                    break;
                case HARD_CLIP:
                    wet = Math.max(-0.5f, Math.min(0.5f, x));
                    break;
                case TUBE:
                    wet = (float) (Math.tanh(2 * x) + 0.3 * Math.tanh(x));
                    break;
                case FUZZ:
                    wet = (float) (Math.signum(x) * (1 - Math.exp(-Math.abs(x))));
                    break;
                case OVERDRIVE:
                    wet = (float) (Math.atan(x));
                    break;
                case METAL:
                    wet = (float) (Math.sin(x * 2.0));
                    break;
            }
            buffer[i] = dry * (1 - mix) + wet * mix;
        }
        return true;
    }

    @Override
    public void processingFinished() {}
}
