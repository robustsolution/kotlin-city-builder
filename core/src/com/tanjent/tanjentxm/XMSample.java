/* Licensed under The MIT License (MIT), see license.txt*/
package com.tanjent.tanjentxm;

/** <p>
 * Holds a MONO sample in FixedPoint format. Loops are always forward which means that bidirectional
 * looping samples will double in size by the loop length.
 * </p>
 * @author Jonas Murman */
public class XMSample {
    /// Cubic interpolation requires a prior sample, offset all sample data by this offset
    public static int SAMPLE_START = 1;

    /// Number of samples
    public int dataFPLength;
    /// Sample data stored in FP format
    public int[] dataFP;

    public int sampleLoopStartPosition;
    public float sampleLoopEndPosition;
    public float sampleLoopLength;

    public int volume;
    public int finetune;
    public int panning;
    public int relativeNote;

    public XMSample()
    {
        this.dataFPLength = 0;
        this.dataFP = new int[0];

        this.volume = 0;
        this.finetune = 0;
        this.panning = 0;
        this.relativeNote = 0;

        this.sampleLoopStartPosition = 0;
        this.sampleLoopEndPosition = 0.0f;
        this.sampleLoopLength = 0.0f;
    }

    /**
     * Fixes the sample data for interpolation and bidirectional looping. This must be called before
     * the sample is used.
     * @param biDirectionalLoop indicates if loop is a bi-directional loop (ping-pong loop)
     */
    public void fixForInterpolation(boolean biDirectionalLoop)
    {
        // no sample data, or invalid sample length?
        if (this.dataFPLength <= 0 || this.dataFP == null) {
            this.dataFP = new int[5];

            // create a silent sample
            this.dataFP[0] = 0;
            this.dataFP[1] = 0;
            this.dataFP[2] = 0;
            this.dataFP[3] = 0;
            this.dataFP[4] = 0;
            this.dataFPLength = 5;

            // set loop points
            this.sampleLoopStartPosition = 0;
            this.sampleLoopEndPosition = 0.0f;
            this.sampleLoopLength = 0.0f;

            // done
            return;
        }

        // sanity check loop points
        if (this.sampleLoopStartPosition < 0) this.sampleLoopStartPosition = 0;
        if (this.sampleLoopEndPosition < 0) this.sampleLoopEndPosition = 0;
        if (this.sampleLoopStartPosition > this.sampleLoopEndPosition)
        {
            this.sampleLoopStartPosition = this.dataFPLength;
            this.sampleLoopEndPosition = this.dataFPLength;
        }
        if (this.sampleLoopStartPosition > this.dataFPLength) this.sampleLoopStartPosition = this.dataFPLength;
        if (this.sampleLoopEndPosition > this.dataFPLength) this.sampleLoopEndPosition = this.dataFPLength;

        // calculate loop length
        this.sampleLoopLength = this.sampleLoopEndPosition - this.sampleLoopStartPosition;

        // no loop or forward loop
        if (biDirectionalLoop == false || this.sampleLoopLength == 0)
        {
            int newDataFPLength = XMSample.SAMPLE_START + this.dataFPLength + 5;
            int[] newDataFP = new int[newDataFPLength];

            // fill start with first sample
            int p = 0;
            for (int i=0;i<XMSample.SAMPLE_START;i++)
            {
                newDataFP[p] = this.dataFP[0];
                p++;
            }

            // copy old data to new
            for (int i=0;i<this.dataFPLength;i++)
            {
                newDataFP[p] = this.dataFP[i];
                p++;
            }

            // fill end with last sample
            for (int i=0;i<5;i++)
            {
                newDataFP[p] = this.dataFP[this.dataFPLength - 1];
                p++;
            }

            // replace data
            this.dataFPLength = newDataFPLength;
            this.dataFP = newDataFP;

            // move loop points
            this.sampleLoopStartPosition += XMSample.SAMPLE_START;
            this.sampleLoopEndPosition += XMSample.SAMPLE_START;

            return;
        }

        // bidirectional loop
        int newDataFPLength = (int)(XMSample.SAMPLE_START + this.dataFPLength + this.sampleLoopLength + 5);
        int[] newDataFP = new int[newDataFPLength];

        // fill start with first sample
        int p = 0;
        for (int i=0;i<XMSample.SAMPLE_START;i++)
        {
            newDataFP[p] = this.dataFP[0];
            p++;
        }

        // copy old data to new, up to loop end position
        for (int i=0;i<(int)(this.sampleLoopEndPosition);i++)
        {
            newDataFP[p] = this.dataFP[i];
            p++;
        }

        // fill loop backwards
        int sp = (int)(this.sampleLoopEndPosition) - 1;
        for (int i=0;i<(int)(this.sampleLoopLength) - 1;i++)
        {
            newDataFP[p] = this.dataFP[sp];
            p++;
            sp--;
        }

        // fill loop forwards again
        sp = (int)(this.sampleLoopStartPosition);
        for (int i=0;i<(int)(this.dataFPLength - this.sampleLoopEndPosition) + 1;i++)
        {
            newDataFP[p] = this.dataFP[sp];
            p++;
            sp++;
        }

        // fill end with last sample
        for (int i=0;i<5;i++)
        {
            newDataFP[p] = this.dataFP[sp];
            p++;
        }

        // replace data
        this.dataFPLength = newDataFPLength;
        this.dataFP = newDataFP;

        // move loop points
        this.sampleLoopStartPosition += XMSample.SAMPLE_START;
        this.sampleLoopEndPosition += XMSample.SAMPLE_START;
        this.sampleLoopEndPosition += this.sampleLoopLength;

        // update loop length
        this.sampleLoopLength += this.sampleLoopLength;
    }

    /**
     * Wraps an (out of bounds) samplePosition within loop bounds
     * @param samplePosition the current sample position
     * @return the wrapped sample position or -1 if there's no more data available (there's no loop defined)
     */
    public float getWrappedSamplePosition(float samplePosition)
    {
        // assume no more samples
        float retValue = -1.0f;

        // wrap loop
        if (this.sampleLoopStartPosition < this.sampleLoopEndPosition)
        {
            samplePosition -= this.sampleLoopStartPosition;
            retValue = this.sampleLoopStartPosition + (samplePosition % this.sampleLoopLength);
        }

        return retValue;
    }
}
