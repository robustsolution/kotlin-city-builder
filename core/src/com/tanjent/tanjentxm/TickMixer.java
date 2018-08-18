/* Licensed under The MIT License (MIT), see license.txt*/
package com.tanjent.tanjentxm;

/** <p>
 * Renders a current {@link XMModule} state to the buffers leftSamples and rightSamples.
 * </p>
 * @author Jonas Murman */
public class TickMixer {

    /// Don't change this while playing. Create a new TickMixer instead.
    private int sampleRate;

    /// Rendered left samples in FP-format
    public int[] leftSamples;
    /// Rendered right samples in FP-format
    public int[] rightSamples;

    /**
     * Creates a new TickMixer and allocates the sample render buffers.
     * @param sampleRate the sample rate to use when mixing (usually 44100 samples/second)
     */
    public TickMixer(int sampleRate)
    {
        if (sampleRate <= 0) sampleRate = 44100;
        this.sampleRate = sampleRate;

        // create render buffers
        int size = this.getMaxTickSampleBufferSize();
        this.leftSamples = new int[size];
        this.rightSamples = new int[size];
        for (int i=0;i<size;i++)
        {
            this.leftSamples[i] = 0;
            this.rightSamples[i] = 0;
        }
    }

    /**
     * Calculates the maximum render buffer size for one module tick
     * @return the maximum render buffer size for one module tick
     */
    public int getMaxTickSampleBufferSize()
    {
        return (int)(XMModule.TICK_TO_SECONDS / XMModule.LOWEST_BPM * this.sampleRate);
    }

    /**
     * Renders a tick with nearest neighbor interpolation. This will alias badly, but it is very fast.
     * Use this for 2-32 channel modules on mobile and similar slower CPU-devices.
     * @param module the module with the current tick state to render
     * @return the number of samples rendered
     */
    public int renderTickNoInterpolation(XMModule module)
    {
        int channelsRendered = 0;
        int mixFactorFP = module.mixFactorFP;
        int sampleDataFP;
        int sampleDataLeftFP;
        int sampleDataRightFP;

        // pitch interpolation
        float samplePositionAdd;
        float samplePositionAddDelta;

        // volume fx interpolation
        int volFactorFP;
        int volFactorAddFP;

        // panning envelope interpolation
        int panLeftFP;
        int panRightFP;
        int panLeftAddFP;
        int panRightAddFP;

        XMChannel channel;
        XMSample sample;

        // global volume
        int globalVolumeFP = module.globalVolumeFP;

        for (int c=0;c<module.numberOfChannels;c++)
        {
            if (module.channels[c].playing == false) continue;

            channel = module.channels[c];
            sample = channel.sample;
            int i = 0;

            volFactorFP = FixedPoint.FLOAT_TO_FP(channel.volumeFactorTickStart);
            volFactorAddFP = FixedPoint.FLOAT_TO_FP((channel.volumeFactorTickEnd - channel.volumeFactorTickStart) / (float)(module.tickSamplesToRender - 1));

            samplePositionAdd = channel.samplePositionAddTickStart;
            samplePositionAddDelta = (channel.samplePositionAddTickEnd - channel.samplePositionAddTickStart) / (float)(module.tickSamplesToRender - 1);

            // is channel silent?
            if (channel.volumeFP == 0 && volFactorAddFP == 0)
            {
                // channel must be silent during the entire tick, just update the sample position
                channel.samplePosition += samplePositionAdd * module.tickSamplesToRender;
                if (channel.samplePosition >= sample.sampleLoopEndPosition)
                {
                    channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                    if (channel.samplePosition < XMSample.SAMPLE_START)
                    {
                        // reached end of sample, no more data to play
                        channel.playing = false;
                    }
                }
                // go to next channel
                continue;
            }

            panLeftFP = FixedPoint.FLOAT_TO_FP(channel.panningEnvelopeLeftValueTickStart);
            panRightFP = FixedPoint.FLOAT_TO_FP(channel.panningEnvelopeRightValueTickStart);
            panLeftAddFP = FixedPoint.FLOAT_TO_FP((channel.panningEnvelopeLeftValueTickEnd - channel.panningEnvelopeLeftValueTickStart) / (float)(module.tickSamplesToRender - 1));
            panRightAddFP = FixedPoint.FLOAT_TO_FP((channel.panningEnvelopeRightValueTickEnd - channel.panningEnvelopeRightValueTickStart) / (float)(module.tickSamplesToRender - 1));

            // mix channel WITHOUT panning envelope?
            if (panLeftFP == 0 && panRightFP == 0 && panLeftAddFP == 0 && panRightAddFP == 0)
            {
                if (channelsRendered == 0)
                {
                    // REPLACE leftSamples and rightSamples with new samples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;


                                // fill what's left in the buffer with zeroes
                                while (i < module.tickSamplesToRender)
                                {
                                    this.leftSamples[i] = 0;
                                    this.rightSamples[i] = 0;
                                    i++;
                                }
                                break;
                            }
                        }

                        // get sample data, no interpolation
                        sampleDataFP = sample.dataFP[(int)(channel.samplePosition)];

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * channel.panningLeftFP) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * channel.panningRightFP) >> FixedPoint.FP_SHIFT;

                        // replace with sample data
                        this.leftSamples[i] = (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] = (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                    }
                } else {
                    // ADD samples to leftSamples and rightSamples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;
                                break;
                            }
                        }

                        // get sample data, no interpolation
                        sampleDataFP = sample.dataFP[(int)(channel.samplePosition)];

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * channel.panningLeftFP) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * channel.panningRightFP) >> FixedPoint.FP_SHIFT;

                        // add sample data
                        this.leftSamples[i] += (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] += (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                    }
                }
            } else {
                // mix channel WITH panning envelope
                if (channelsRendered == 0)
                {
                    // REPLACE leftSamples and rightSamples with new samples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;

                                // fill what's left in the buffer with zeroes
                                while (i < module.tickSamplesToRender)
                                {
                                    this.leftSamples[i] = 0;
                                    this.rightSamples[i] = 0;
                                    i++;
                                }
                                break;
                            }
                        }

                        // get sample data, no interpolation
                        sampleDataFP = sample.dataFP[(int)(channel.samplePosition)];

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * (channel.panningLeftFP + panLeftFP)) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * (channel.panningRightFP + panRightFP)) >> FixedPoint.FP_SHIFT;

                        // replace with sample data
                        this.leftSamples[i] = (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] = (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                        panLeftFP += panLeftAddFP;
                        panRightFP += panRightAddFP;
                    }
                } else {
                    // ADD samples to leftSamples and rightSamples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;
                                break;
                            }
                        }

                        // get sample data, no interpolation
                        sampleDataFP = sample.dataFP[(int)(channel.samplePosition)];

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * (channel.panningLeftFP + panLeftFP)) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * (channel.panningRightFP + panRightFP)) >> FixedPoint.FP_SHIFT;

                        // add sample data
                        this.leftSamples[i] += (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] += (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                        panLeftFP += panLeftAddFP;
                        panRightFP += panRightAddFP;
                    }
                }
            }

            channelsRendered++;
        }

        if (channelsRendered == 0)
        {
            // zero buffer in case of totally silent tick
            for (int i=0;i<module.tickSamplesToRender;i++)
            {
                this.leftSamples[i] = 0;
                this.rightSamples[i] = 0;
            }
        } else if (globalVolumeFP != FixedPoint.FP_ONE) {
            // handle global volume
            for (int i=0;i<module.tickSamplesToRender;i++)
            {
                this.leftSamples[i] = (this.leftSamples[i] * globalVolumeFP) >> FixedPoint.FP_SHIFT;
                this.rightSamples[i] = (this.rightSamples[i] * globalVolumeFP) >> FixedPoint.FP_SHIFT;
            }
        }

        return module.tickSamplesToRender;
    }

    /**
     * Renders a tick with linear interpolation. This will alias less badly and is a good overall choice.
     * Use this for 2-16 channel modules on mobile and similar slower CPU-devices.
     * @param module the module with the current tick state to render
     * @return the number of samples rendered
     */
    public int renderTickLinearInterpolation(XMModule module)
    {

        int channelsRendered = 0;
        int mixFactorFP = module.mixFactorFP;
        int sampleDataFP;
        int sampleDataLeftFP;
        int sampleDataRightFP;
        int sampleDataPositionI;

        // linear interpolation
        int cy1;
        int cy2;
        int cx;

        // pitch interpolation
        float samplePositionAdd;
        float samplePositionAddDelta;

        // volume fx interpolation
        int volFactorFP;
        int volFactorAddFP;

        // panning envelope interpolation
        int panLeftFP;
        int panRightFP;
        int panLeftAddFP;
        int panRightAddFP;

        XMChannel channel;
        XMSample sample;

        // global volume
        int globalVolumeFP = module.globalVolumeFP;

        for (int c=0;c<module.numberOfChannels;c++)
        {
            if (module.channels[c].playing == false) continue;

            channel = module.channels[c];
            sample = channel.sample;
            int i = 0;

            volFactorFP = FixedPoint.FLOAT_TO_FP(channel.volumeFactorTickStart);
            volFactorAddFP = FixedPoint.FLOAT_TO_FP((channel.volumeFactorTickEnd - channel.volumeFactorTickStart) / (float)(module.tickSamplesToRender - 1));

            samplePositionAdd = channel.samplePositionAddTickStart;
            samplePositionAddDelta = (channel.samplePositionAddTickEnd - channel.samplePositionAddTickStart) / (float)(module.tickSamplesToRender - 1);

            // is channel silent?
            if (channel.volumeFP == 0 && volFactorAddFP == 0)
            {
                // channel must be silent during the entire tick, just update the sample position
                channel.samplePosition += samplePositionAdd * module.tickSamplesToRender;
                if (channel.samplePosition >= sample.sampleLoopEndPosition)
                {
                    channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                    if (channel.samplePosition < XMSample.SAMPLE_START)
                    {
                        // reached end of sample, no more data to play
                        channel.playing = false;
                    }
                }
                // go to next channel
                continue;
            }

            panLeftFP = FixedPoint.FLOAT_TO_FP(channel.panningEnvelopeLeftValueTickStart);
            panRightFP = FixedPoint.FLOAT_TO_FP(channel.panningEnvelopeRightValueTickStart);
            panLeftAddFP = FixedPoint.FLOAT_TO_FP((channel.panningEnvelopeLeftValueTickEnd - channel.panningEnvelopeLeftValueTickStart) / (float)(module.tickSamplesToRender - 1));
            panRightAddFP = FixedPoint.FLOAT_TO_FP((channel.panningEnvelopeRightValueTickEnd - channel.panningEnvelopeRightValueTickStart) / (float)(module.tickSamplesToRender - 1));

            // mix channel WITHOUT panning envelope?
            if (panLeftFP == 0 && panRightFP == 0 && panLeftAddFP == 0 && panRightAddFP == 0)
            {
                if (channelsRendered == 0)
                {
                    // REPLACE leftSamples and rightSamples with new samples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;

                                // fill what's left in the buffer with zeroes
                                while (i < module.tickSamplesToRender)
                                {
                                    this.leftSamples[i] = 0;
                                    this.rightSamples[i] = 0;
                                    i++;
                                }
                                break;
                            }
                        }

                        // get sample data and linear interpolate
                        sampleDataPositionI = (int)(channel.samplePosition);
                        cy1 = sample.dataFP[sampleDataPositionI];
                        cy2 = sample.dataFP[sampleDataPositionI + 1];
                        cx = FixedPoint.FLOAT_TO_FP(channel.samplePosition - sampleDataPositionI); // frac of channel.samplePosition
                        sampleDataFP = ((cy1 * (FixedPoint.FP_ONE - cx)) >> FixedPoint.FP_SHIFT) + ((cy2 * cx) >> FixedPoint.FP_SHIFT);

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * channel.panningLeftFP) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * channel.panningRightFP) >> FixedPoint.FP_SHIFT;

                        // replace with sample data
                        this.leftSamples[i] = (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] = (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                    }
                } else {
                    // ADD samples to leftSamples and rightSamples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;
                                break;
                            }
                        }

                        // get sample data and linear interpolate
                        sampleDataPositionI = (int)(channel.samplePosition);
                        cy1 = sample.dataFP[sampleDataPositionI];
                        cy2 = sample.dataFP[sampleDataPositionI + 1];
                        cx = FixedPoint.FLOAT_TO_FP(channel.samplePosition - sampleDataPositionI); // frac of channel.samplePosition
                        sampleDataFP = ((cy1 * (FixedPoint.FP_ONE - cx)) >> FixedPoint.FP_SHIFT) + ((cy2 * cx) >> FixedPoint.FP_SHIFT);

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * channel.panningLeftFP) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * channel.panningRightFP) >> FixedPoint.FP_SHIFT;

                        // add sample data
                        this.leftSamples[i] += (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] += (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                    }
                }
            } else {
                // mix channel WITH panning envelope
                if (channelsRendered == 0)
                {
                    // REPLACE leftSamples and rightSamples with new samples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;

                                // fill what's left in the buffer with zeroes
                                while (i < module.tickSamplesToRender)
                                {
                                    this.leftSamples[i] = 0;
                                    this.rightSamples[i] = 0;
                                    i++;
                                }
                                break;
                            }
                        }

                        // get sample data and linear interpolate
                        sampleDataPositionI = (int)(channel.samplePosition);
                        cy1 = sample.dataFP[sampleDataPositionI];
                        cy2 = sample.dataFP[sampleDataPositionI + 1];
                        cx = FixedPoint.FLOAT_TO_FP(channel.samplePosition - sampleDataPositionI); // frac of channel.samplePosition
                        sampleDataFP = ((cy1 * (FixedPoint.FP_ONE - cx)) >> FixedPoint.FP_SHIFT) + ((cy2 * cx) >> FixedPoint.FP_SHIFT);

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * (channel.panningLeftFP + panLeftFP)) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * (channel.panningRightFP + panRightFP)) >> FixedPoint.FP_SHIFT;

                        // replace with sample data
                        this.leftSamples[i] = (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] = (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                        panLeftFP += panLeftAddFP;
                        panRightFP += panRightAddFP;
                    }
                } else {
                    // ADD samples to leftSamples and rightSamples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;
                                break;
                            }
                        }

                        // get sample data and linear interpolate
                        sampleDataPositionI = (int)(channel.samplePosition);
                        cy1 = sample.dataFP[sampleDataPositionI];
                        cy2 = sample.dataFP[sampleDataPositionI + 1];
                        cx = FixedPoint.FLOAT_TO_FP(channel.samplePosition - sampleDataPositionI); // frac of channel.samplePosition
                        sampleDataFP = ((cy1 * (FixedPoint.FP_ONE - cx)) >> FixedPoint.FP_SHIFT) + ((cy2 * cx) >> FixedPoint.FP_SHIFT);

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * (channel.panningLeftFP + panLeftFP)) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * (channel.panningRightFP + panRightFP)) >> FixedPoint.FP_SHIFT;

                        // add sample data
                        this.leftSamples[i] += (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] += (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                        panLeftFP += panLeftAddFP;
                        panRightFP += panRightAddFP;
                    }
                }
            }

            channelsRendered++;
        }

        if (channelsRendered == 0)
        {
            // zero buffer in case of totally silent tick
            for (int i=0;i<module.tickSamplesToRender;i++)
            {
                this.leftSamples[i] = 0;
                this.rightSamples[i] = 0;
            }
        } else if (globalVolumeFP != FixedPoint.FP_ONE) {
            // handle global volume
            for (int i=0;i<module.tickSamplesToRender;i++)
            {
                this.leftSamples[i] = (this.leftSamples[i] * globalVolumeFP) >> FixedPoint.FP_SHIFT;
                this.rightSamples[i] = (this.rightSamples[i] * globalVolumeFP) >> FixedPoint.FP_SHIFT;
            }
        }

        return module.tickSamplesToRender;
    }

    /**
     * Renders a tick with cubic polynomial interpolation. Very little aliasing.
     * Use this for 2-8 channel modules on mobile and similar slower CPU-devices.
     * @param module the module with the current tick state to render
     * @return the number of samples rendered
     */
    public int renderTickCubicInterpolation(XMModule module)
    {

        int channelsRendered = 0;
        int mixFactorFP = module.mixFactorFP;
        int sampleDataFP;
        int sampleDataLeftFP;
        int sampleDataRightFP;
        int sampleDataPositionI;

        // cubic interpolation
        int cy0;
        int cy1;
        int cy2;
        int cy3;
        int cc;
        int cv;
        int cw;
        int ca;
        int cb;
        int cx;

        // pitch interpolation
        float samplePositionAdd;
        float samplePositionAddDelta;

        // volume fx interpolation
        int volFactorFP;
        int volFactorAddFP;

        // panning envelope interpolation
        int panLeftFP;
        int panRightFP;
        int panLeftAddFP;
        int panRightAddFP;

        XMChannel channel;
        XMSample sample;

        // global volume
        int globalVolumeFP = module.globalVolumeFP;

        for (int c=0;c<module.numberOfChannels;c++)
        {
            if (module.channels[c].playing == false) continue;

            channel = module.channels[c];
            sample = channel.sample;
            int i = 0;

            volFactorFP = FixedPoint.FLOAT_TO_FP(channel.volumeFactorTickStart);
            volFactorAddFP = FixedPoint.FLOAT_TO_FP((channel.volumeFactorTickEnd - channel.volumeFactorTickStart) / (float)(module.tickSamplesToRender - 1));

            samplePositionAdd = channel.samplePositionAddTickStart;
            samplePositionAddDelta = (channel.samplePositionAddTickEnd - channel.samplePositionAddTickStart) / (float)(module.tickSamplesToRender - 1);

            // is channel silent?
            if (channel.volumeFP == 0 && volFactorAddFP == 0)
            {
                // channel must be silent during the entire tick, just update the sample position
                channel.samplePosition += samplePositionAdd * module.tickSamplesToRender;
                if (channel.samplePosition >= sample.sampleLoopEndPosition)
                {
                    channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                    if (channel.samplePosition < XMSample.SAMPLE_START)
                    {
                        // reached end of sample, no more data to play
                        channel.playing = false;
                    }
                }
                // go to next channel
                continue;
            }

            panLeftFP = FixedPoint.FLOAT_TO_FP(channel.panningEnvelopeLeftValueTickStart);
            panRightFP = FixedPoint.FLOAT_TO_FP(channel.panningEnvelopeRightValueTickStart);
            panLeftAddFP = FixedPoint.FLOAT_TO_FP((channel.panningEnvelopeLeftValueTickEnd - channel.panningEnvelopeLeftValueTickStart) / (float)(module.tickSamplesToRender - 1));
            panRightAddFP = FixedPoint.FLOAT_TO_FP((channel.panningEnvelopeRightValueTickEnd - channel.panningEnvelopeRightValueTickStart) / (float)(module.tickSamplesToRender - 1));

            // mix channel WITHOUT panning envelope?
            if (panLeftFP == 0 && panRightFP == 0 && panLeftAddFP == 0 && panRightAddFP == 0)
            {
                if (channelsRendered == 0)
                {
                    // REPLACE leftSamples and rightSamples with new samples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;

                                // fill what's left in the buffer with zeroes
                                while (i < module.tickSamplesToRender)
                                {
                                    this.leftSamples[i] = 0;
                                    this.rightSamples[i] = 0;
                                    i++;
                                }
                                break;
                            }
                        }

                        // get sample data and interpolate
                        sampleDataPositionI = (int)(channel.samplePosition);

                        // cubic interpolation
                        cy0 = sample.dataFP[sampleDataPositionI - 1];
                        cy1 = sample.dataFP[sampleDataPositionI];
                        cy2 = sample.dataFP[sampleDataPositionI + 1];
                        cy3 = sample.dataFP[sampleDataPositionI + 2];
                        cc = (cy2 - cy0) >> 1;
                        cv = cy1 - cy2;
                        cw = cc + cv;
                        ca = cw + cv + ((cy3 - cy1) >> 1);
                        cb = cw + ca;
                        cx = FixedPoint.FLOAT_TO_FP(channel.samplePosition - sampleDataPositionI); // frac of channel.samplePosition
                        sampleDataFP = (((((((((ca * cx) >> FixedPoint.FP_SHIFT) - cb) * cx) >> FixedPoint.FP_SHIFT) + cc) * cx) >> FixedPoint.FP_SHIFT) + cy1);

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * channel.panningLeftFP) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * channel.panningRightFP) >> FixedPoint.FP_SHIFT;

                        // replace with sample data
                        this.leftSamples[i] = (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] = (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                    }
                } else {
                    // ADD samples to leftSamples and rightSamples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;
                                break;
                            }
                        }

                        // get sample data and interpolate
                        sampleDataPositionI = (int)(channel.samplePosition);

                        // cubic interpolation
                        cy0 = sample.dataFP[sampleDataPositionI - 1];
                        cy1 = sample.dataFP[sampleDataPositionI];
                        cy2 = sample.dataFP[sampleDataPositionI + 1];
                        cy3 = sample.dataFP[sampleDataPositionI + 2];
                        cc = (cy2 - cy0) >> 1;
                        cv = cy1 - cy2;
                        cw = cc + cv;
                        ca = cw + cv + ((cy3 - cy1) >> 1);
                        cb = cw + ca;
                        cx = FixedPoint.FLOAT_TO_FP(channel.samplePosition - sampleDataPositionI); // frac of channel.samplePosition
                        sampleDataFP = (((((((((ca * cx) >> FixedPoint.FP_SHIFT) - cb) * cx) >> FixedPoint.FP_SHIFT) + cc) * cx) >> FixedPoint.FP_SHIFT) + cy1);

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * channel.panningLeftFP) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * channel.panningRightFP) >> FixedPoint.FP_SHIFT;

                        // add sample data
                        this.leftSamples[i] += (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] += (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                    }
                }
            } else {
                // mix channel WITH panning envelope
                if (channelsRendered == 0)
                {
                    // REPLACE leftSamples and rightSamples with new samples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;

                                // fill what's left in the buffer with zeroes
                                while (i < module.tickSamplesToRender)
                                {
                                    this.leftSamples[i] = 0;
                                    this.rightSamples[i] = 0;
                                    i++;
                                }
                                break;
                            }
                        }

                        // get sample data and interpolate
                        sampleDataPositionI = (int)(channel.samplePosition);

                        // cubic interpolation
                        cy0 = sample.dataFP[sampleDataPositionI - 1];
                        cy1 = sample.dataFP[sampleDataPositionI];
                        cy2 = sample.dataFP[sampleDataPositionI + 1];
                        cy3 = sample.dataFP[sampleDataPositionI + 2];
                        cc = (cy2 - cy0) >> 1;
                        cv = cy1 - cy2;
                        cw = cc + cv;
                        ca = cw + cv + ((cy3 - cy1) >> 1);
                        cb = cw + ca;
                        cx = FixedPoint.FLOAT_TO_FP(channel.samplePosition - sampleDataPositionI); // frac of channel.samplePosition
                        sampleDataFP = (((((((((ca * cx) >> FixedPoint.FP_SHIFT) - cb) * cx) >> FixedPoint.FP_SHIFT) + cc) * cx) >> FixedPoint.FP_SHIFT) + cy1);

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * (channel.panningLeftFP + panLeftFP)) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * (channel.panningRightFP + panRightFP)) >> FixedPoint.FP_SHIFT;

                        // replace with sample data
                        this.leftSamples[i] = (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] = (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                        panLeftFP += panLeftAddFP;
                        panRightFP += panRightAddFP;
                    }
                } else {
                    // ADD samples to leftSamples and rightSamples
                    while (i < module.tickSamplesToRender)
                    {
                        // update sample pointer
                        if (channel.samplePosition >= sample.sampleLoopEndPosition)
                        {
                            channel.samplePosition = sample.getWrappedSamplePosition(channel.samplePosition);
                            if (channel.samplePosition < XMSample.SAMPLE_START)
                            {
                                // reached end of sample, no more data to play
                                channel.playing = false;
                                break;
                            }
                        }

                        // get sample data and interpolate
                        sampleDataPositionI = (int)(channel.samplePosition);

                        // cubic interpolation
                        cy0 = sample.dataFP[sampleDataPositionI - 1];
                        cy1 = sample.dataFP[sampleDataPositionI];
                        cy2 = sample.dataFP[sampleDataPositionI + 1];
                        cy3 = sample.dataFP[sampleDataPositionI + 2];
                        cc = (cy2 - cy0) >> 1;
                        cv = cy1 - cy2;
                        cw = cc + cv;
                        ca = cw + cv + ((cy3 - cy1) >> 1);
                        cb = cw + ca;
                        cx = FixedPoint.FLOAT_TO_FP(channel.samplePosition - sampleDataPositionI); // frac of channel.samplePosition
                        sampleDataFP = (((((((((ca * cx) >> FixedPoint.FP_SHIFT) - cb) * cx) >> FixedPoint.FP_SHIFT) + cc) * cx) >> FixedPoint.FP_SHIFT) + cy1);

                        // adjust for channel volume and volume delta effects (tremolo, envelope)
                        sampleDataFP = (sampleDataFP * ((channel.volumeFP_LPF * volFactorFP) >> FixedPoint.FP_SHIFT)) >> FixedPoint.FP_SHIFT;

                        // adjust for channel panning
                        sampleDataLeftFP = (sampleDataFP * (channel.panningLeftFP + panLeftFP)) >> FixedPoint.FP_SHIFT;
                        sampleDataRightFP = (sampleDataFP * (channel.panningRightFP + panRightFP)) >> FixedPoint.FP_SHIFT;

                        // add sample data
                        this.leftSamples[i] += (sampleDataLeftFP * mixFactorFP) >> FixedPoint.FP_SHIFT;
                        this.rightSamples[i] += (sampleDataRightFP * mixFactorFP) >> FixedPoint.FP_SHIFT;

                        // update
                        channel.samplePosition += samplePositionAdd;

                        // LPF volumeFP to prevent zipper noise
                        channel.volumeFP_LPF = ((channel.volumeFP * FixedPoint.FP_ZERO_POINT_05) >> FixedPoint.FP_SHIFT) + ((channel.volumeFP_LPF * FixedPoint.FP_ZERO_POINT_95) >> FixedPoint.FP_SHIFT);

                        i++;
                        samplePositionAdd += samplePositionAddDelta;
                        volFactorFP += volFactorAddFP;
                        panLeftFP += panLeftAddFP;
                        panRightFP += panRightAddFP;
                    }
                }
            }

            channelsRendered++;
        }

        if (channelsRendered == 0)
        {
            // zero buffer in case of totally silent tick
            for (int i=0;i<module.tickSamplesToRender;i++)
            {
                this.leftSamples[i] = 0;
                this.rightSamples[i] = 0;
            }
        } else if (globalVolumeFP != FixedPoint.FP_ONE) {
            // handle global volume
            for (int i=0;i<module.tickSamplesToRender;i++)
            {
                this.leftSamples[i] = (this.leftSamples[i] * globalVolumeFP) >> FixedPoint.FP_SHIFT;
                this.rightSamples[i] = (this.rightSamples[i] * globalVolumeFP) >> FixedPoint.FP_SHIFT;
            }
        }

        return module.tickSamplesToRender;
    }
}
