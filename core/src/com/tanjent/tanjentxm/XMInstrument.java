/* Licensed under The MIT License (MIT), see license.txt*/
package com.tanjent.tanjentxm;

/** <p>
 * Holds a complete XM-instrument (envelopes, key mappings, samples).
 * </p>
 * @author Jonas Murman */
public class XMInstrument {

    public int numberOfSamples;
    public XMSample[] samples;
    public int[] noteToSampleIndex;

    public static int ENVELOPE_MAXIMUM_POINTS = 12;

    public boolean volumeEnvelopeEnabled;
    public boolean volumeEnvelopeSustainEnabled;
    public boolean volumeEnvelopeLoopEnabled;
    public int volumeEnvelopeNumberOfPoints;
    public int volumeEnvelopeSustainPointIndex;
    public int[] volumeEnvelopeTicks;
    public int[] volumeEnvelopeValues;
    public int volumeEnvelopeLoopStartPointIndex;
    public int volumeEnvelopeLoopEndPointIndex;

    public boolean panningEnvelopeEnabled;
    public boolean panningEnvelopeSustainEnabled;
    public boolean panningEnvelopeLoopEnabled;
    public int panningEnvelopeNumberOfPoints;
    public int panningEnvelopeSustainPointIndex;
    public int[] panningEnvelopeTicks;
    public int[] panningEnvelopeValues;
    public int panningEnvelopeLoopStartPointIndex;
    public int panningEnvelopeLoopEndPointIndex;

    public int vibratoType;
    public int vibratoSweep;
    public int vibratoDepth;
    public int vibratoRate;

    public static int FADEOUT_MAX = 65535;
    public int fadeout;

    public XMInstrument()
    {

        this.numberOfSamples = 0;
        this.samples = new XMSample[0];
        this.noteToSampleIndex = new int[XMModule.NUMBER_OF_NOTES];
        for (int i=0;i<XMModule.NUMBER_OF_NOTES;i++)
        {
            this.noteToSampleIndex[i] = 0;
        }

        this.volumeEnvelopeEnabled = false;
        this.volumeEnvelopeSustainEnabled = false;
        this.volumeEnvelopeLoopEnabled = false;
        this.volumeEnvelopeNumberOfPoints = 0;
        this.volumeEnvelopeSustainPointIndex = 1;
        this.volumeEnvelopeLoopStartPointIndex = 1;
        this.volumeEnvelopeLoopEndPointIndex = 1;
        this.volumeEnvelopeTicks = new int[XMInstrument.ENVELOPE_MAXIMUM_POINTS];
        this.volumeEnvelopeValues = new int[XMInstrument.ENVELOPE_MAXIMUM_POINTS];

        this.panningEnvelopeEnabled = false;
        this.panningEnvelopeSustainEnabled = false;
        this.panningEnvelopeLoopEnabled = false;
        this.panningEnvelopeNumberOfPoints = 0;
        this.panningEnvelopeSustainPointIndex = 1;
        this.panningEnvelopeLoopStartPointIndex = 1;
        this.panningEnvelopeLoopEndPointIndex = 1;
        this.panningEnvelopeTicks = new int[XMInstrument.ENVELOPE_MAXIMUM_POINTS];
        this.panningEnvelopeValues = new int[XMInstrument.ENVELOPE_MAXIMUM_POINTS];

        for (int i=0;i<XMInstrument.ENVELOPE_MAXIMUM_POINTS;i++)
        {
            this.volumeEnvelopeTicks[i] = i;
            this.volumeEnvelopeValues[i] = 0;
            this.panningEnvelopeTicks[i] = i;
            this.panningEnvelopeValues[i] = 0;
        }

        this.vibratoType = 0;
        this.vibratoSweep = 0;
        this.vibratoDepth = 0;
        this.vibratoRate = 0;

        this.fadeout = XMInstrument.FADEOUT_MAX;
    }
}
