package com.tanjent.tanjentxm;

/** <p>
 * Holds an entire XM-module in memory (patterns, sample and instrument data).
 * </p>
 * <p>
 * Will load and "play" most up-to-date XM files. Try to resave old/unorthodox .XM-files with OpenMPT or MilkyTracker if a file won't load.
 * </p>
 * <p>
 * Limitations
 * <ul>
 * <li>Only linear frequency slides - even if this is old-school it is not THAT old-school
 * <li>No fancy XM extensions - files are assumed 2 ... 32 channel and to hold MONO samples
 * </ul>
 * </p>
 * <p>
 * Current EFFECT COLUMN effects not implemented:
 * <ul>
 * <li>E3x Set gliss control
 * <li>E6x Set loop begin/loop
 * </ul>
 * </p>
 * @author Jonas Murman */
public class XMModule {

    public static float TICK_TO_SECONDS = 2.5f;
    public static int LOWEST_BPM = 30;
    public static int NUMBER_OF_NOTES = 96;
    public static int NOTE_KEYOFF = 97;
    public static int MAX_PATTERNS = 256;
    public static int MIN_CHANNELS = 2;
    public static int MAX_CHANNELS = 32;

    public boolean playing;
    public boolean loopOnEnd;

    private float mixFactor;
    public int mixFactorFP;

    public int sampleRate;
    public int tickSamplesToRender;
    public int defaultTempo;
    public int defaultBPM;

    public boolean linearFrequencyTable;

    public int numberOfInstruments;
    public XMInstrument[] instruments;

    public int numberOfChannels;
    public XMChannel[] channels;
    private XMChannel channel;

    public int songLength;
    public int restartPosition;

    public boolean positionJump;
    public int positionJumpIndex;
    public int patternIndex;
    public int[] patternOrder;

    public int numberOfPatterns;
    public XMPattern[] patterns;

    private boolean firstTick;
    private int currentTick;
    public  int patternRow;
    private boolean patternBreak;
    private int patternBreakRow;
    private int patternDelay;
    public XMPattern pattern;

    private int[] PANNING_PARAMETER_TO_PANNING_LEFT_FP;
    private int[] PANNING_PARAMETER_TO_PANNING_RIGHT_FP;

    private int arpeggioNoteAdd;
    private static int[] ARPEGGIO_MASK = new int[] {0, 0xF0, 0x0F};
    private static int[] ARPEGGIO_SHIFT = new int [] {0, 4, 0};

    public int globalVolumeFP;
    private int globalVolumeSlideMemory;
    private int globalVolumeSlideTickDeltaFP;

    public XMNote[] queuedNotes;
    private XMNote[] queuedNoteCache;

    /**
     * <p>Creates a new XMModule.</p>
     * <p>
     * The mixFactor is used to level match modules with different number of channels.
     * The mixFactor tells the mixing engine how loud each channel should be when the module is rendered by a {@link TickMixer}.<br>
     * <ul>
     * <li>Pass -1 to auto calculate the mixing factor as 1/sqrt(n)
     * <li>Pass 0 to auto calculate the mixing factor as 1/n
     * <li>Pass >0 for anything else, i.e. 2.0 will play the module twice as loud (and will probably distort!)
     * </ul>
     * </p>
     * @param sampleRate the sample rate to use for calculating pitches
     * @param mixFactor the mixing factor to use for mixing channels together
     */
    public XMModule(int sampleRate, float mixFactor)
    {
        this.playing = false;
        this.mixFactor = mixFactor;
        this.mixFactorFP = 0;
        if (sampleRate < 0) sampleRate = 44100;
        this.sampleRate = sampleRate;
        this.tickSamplesToRender = 0;

        this.defaultTempo = 6;
        this.defaultBPM = 125;

        this.linearFrequencyTable = true;

        this.numberOfInstruments = 0;
        this.instruments = new XMInstrument[0];

        this.numberOfChannels = 0;
        this.channels = new XMChannel[0];

        this.songLength = 0;
        this.restartPosition = 0;
        this.patternOrder = new int[XMModule.MAX_PATTERNS];
        for (int i=0;i<XMModule.MAX_PATTERNS;i++)
        {
            this.patternOrder[i] = 0;
        }
        this.patterns = new XMPattern[1];
        this.patterns[0] = new XMPattern(1, 2);

        this.PANNING_PARAMETER_TO_PANNING_LEFT_FP = new int[256];
        this.PANNING_PARAMETER_TO_PANNING_RIGHT_FP = new int[256];
        for (int i=0;i<256;i++)
        {
            this.PANNING_PARAMETER_TO_PANNING_LEFT_FP[i] = (FixedPoint.FLOAT_TO_FP(1.0f - (i / 255.0f)));
            this.PANNING_PARAMETER_TO_PANNING_RIGHT_FP[i] = (FixedPoint.FLOAT_TO_FP((i / 255.0f)));
        }

        this.queuedNotes = new XMNote[0];
        this.queuedNoteCache = new XMNote[0];

        this.restart();
    }

    /**
     * Rewinds the module. Does not reset global volume, notes or channels. This is like jumping to pattern index 0 while the song
     * is playing (i.e current BPM, tick speed; volume and effect column memory is unaffected).
     */
    public void rewind()
    {
        this.patternIndex = 0;

        this.positionJump = false;
        this.positionJumpIndex = 0;

        this.firstTick = true;
        this.currentTick = 0;
        this.patternRow = 0;
        this.patternBreak = false;
        this.patternBreakRow = 0;
        this.patternDelay = 0;

        int pi = this.patternOrder[this.patternIndex];
        if (pi < 0) pi = 0;
        if (pi >= this.patterns.length) pi = 0;
        this.pattern = this.patterns[pi];
    }

    /**
     * Restarts the module. Resets global volume, notes and channels effects.
     */
    public void restart()
    {
        this.rewind();

        this.setBPM(this.defaultBPM);

        this.globalVolumeFP = FixedPoint.FP_ONE;
        this.globalVolumeSlideMemory = 0;
        this.globalVolumeSlideTickDeltaFP = 0;

        this.queuedNotes = new XMNote[this.numberOfChannels];
        this.queuedNoteCache = new XMNote[this.numberOfChannels];

        for (int c=0;c<this.numberOfChannels;c++) {
            this.queuedNotes[c] = null;
            this.queuedNoteCache[c] = new XMNote();
        }

        for (int c=0;c<this.numberOfChannels;c++) {
            this.channels[c].reset();
        }
    }

    /**
     * Plays the module.
     * @param loopOnEnd true = loop song when done playing, false = stop playing when done
     */
    public void play(boolean loopOnEnd)
    {
        this.playing = true;
        this.loopOnEnd = loopOnEnd;
    }

    /**
     * Pauses the module.
     */
    public void pause()
    {
        this.playing = false;
    }

    /**
     * Sets the BPM (Beats Per Minute).
     * @param beatsPerMinute the BPM to set, practical range is 30 ... 255.
     */
    public void setBPM(int beatsPerMinute)
    {
        if (beatsPerMinute < 30) beatsPerMinute = 30;
        if (beatsPerMinute > 255) beatsPerMinute = 255;
        this.tickSamplesToRender = (int)(XMModule.TICK_TO_SECONDS / (float)beatsPerMinute * this.sampleRate);
    }

    /**
     * Queues new note data to override when the next row is read. Parameters are the same as the parameters of a {@link XMNote}, but negative parameters are handled differently.
     * @param	channel channel to use
     * @param	note note data to use
     * @param	instrument instrument data to use
     * @param	volume volume data to use
     * @param	effectType effect type data to use
     * @param	effectParameter effect parameter data to use
     */
    public void queueNote(int channel, int note, int instrument, int volume, int effectType, int effectParameter)
    {
        // wrap channel
        if (channel < 0) channel = -channel;
        channel = channel % this.numberOfChannels;

        if (note > XMModule.NOTE_KEYOFF) note = 0;
        if (instrument >= this.numberOfInstruments) instrument = 0;
        if (volume > 0xFF) volume = 0xFF;
        if (effectType > 0xFF) effectType = 0xFF;
        if (effectParameter > 0xFF) effectParameter = 0xFF;

        this.queuedNoteCache[channel].note = note;
        this.queuedNoteCache[channel].instrument = instrument;
        this.queuedNoteCache[channel].volume = volume;
        this.queuedNoteCache[channel].effectType = effectType;
        this.queuedNoteCache[channel].effectParameter = effectParameter;
        this.queuedNotes[channel] = this.queuedNoteCache[channel];
    }

    /**
     * Jumps to the specified pattern index and pattern row.
     * @param	patternIndex index in the patternOrder to jump to
     * @param	patternRow row inside the pattern to jump to
     */
    public void jump(int patternIndex, int patternRow)
    {
        this.firstTick = true;
        this.currentTick = 0;
        this.patternDelay = 0;

        this.positionJump = true;
        this.positionJumpIndex = patternIndex;
        this.patternBreak = true;
        this.patternBreakRow = patternRow;
    }

    /**
     * Sends a key off the specified channel
     * @param	channel channel to send key off message to
     */
    public void keyOff(int channel)
    {
        if (channel >= 0 && channel < this.numberOfChannels) {
            if (this.channels[channel].playing == true) {
                this.channels[channel].inRelease = true;
            }
        }
    }

    /**
     * Sends a key cut to the specified channel
     * @param	channel	channel to send key cut message to
     */
    public void keyCut(int channel)
    {
        if (channel >= 0 && channel < this.numberOfChannels) {
            this.channels[channel].playing = false;
        }
    }

    /**
     * Advances the module by one tick. Updates channel, instrument and sample states.
     */
    public void advanceByOneTick()
    {
        if (this.playing == false) return;
        if (this.pattern == null) return;

        for (int c=0;c<this.numberOfChannels;c++) {
            // get channel
            this.channel = this.channels[c];
            this.channel.samplePositionAddTickStart = this.channel.samplePositionAddTickEnd;
            this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / this.sampleRate;

            // init envelopes
            this.channel.volumeFactorTickStart = this.channel.volumeFactorTickEnd;
            this.channel.volumeFactorTickEnd = 1.0f;
            this.channel.panningEnvelopeLeftValueTickStart = this.channel.panningEnvelopeLeftValueTickEnd;
            this.channel.panningEnvelopeRightValueTickStart = this.channel.panningEnvelopeRightValueTickEnd;
            this.channel.panningEnvelopeLeftValueTickEnd = 0;
            this.channel.panningEnvelopeRightValueTickEnd = 0;

            if (this.firstTick == true) {

                // fill from pattern
                this.pattern.fillNote(this.channel.note, c, this.patternRow);

                // override with queued note event?
                if (this.queuedNotes[c] != null) {
                    // fill from queue
                    if (this.queuedNotes[c].note >= 0) this.channel.note.note = this.queuedNotes[c].note;
                    if (this.queuedNotes[c].instrument >= 0) this.channel.note.instrument = this.queuedNotes[c].instrument;
                    if (this.queuedNotes[c].volume >= 0) this.channel.note.volume = this.queuedNotes[c].volume;
                    if (this.queuedNotes[c].effectType >= 0) this.channel.note.effectType = this.queuedNotes[c].effectType;
                    if (this.queuedNotes[c].effectParameter >= 0) this.channel.note.effectParameter = this.queuedNotes[c].effectParameter;
                    this.queuedNotes[c] = null;
                }

                this.parseFirstTick();
                this.advanceEnvelopes(this.channel, this.instruments[this.channel.instrumentColumnMemory]);
                this.doVibrato(this.channel, this.instruments[this.channel.instrumentColumnMemory]);

            } else {
                // volume slide down
                if (this.channel.note.volume >= 0x60 && this.channel.note.volume <= 0x6F) {
                    this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((this.channel.note.volume & 0x0F) / (float)XMChannel.MAX_VOLUME);
                    if (this.channel.volumeFP < 0) this.channel.volumeFP = 0;
                }
                // volume slide up
                if (this.channel.note.volume >= 0x70 && this.channel.note.volume <= 0x7F) {
                    this.channel.volumeFP += FixedPoint.FLOAT_TO_FP((this.channel.note.volume & 0x0F) / (float)XMChannel.MAX_VOLUME);
                    if (this.channel.volumeFP > FixedPoint.FP_ONE) this.channel.volumeFP = FixedPoint.FP_ONE;
                }

                // volume slide
                if (this.channel.note.effectType == 0x05 ||
                        this.channel.note.effectType == 0x06 ||
                        this.channel.note.effectType == 0x0A) {
                    this.channel.volumeFP += this.channel.volumeSlideTickDeltaFP;
                    if (this.channel.volumeFP < 0) this.channel.volumeFP = 0;
                    if (this.channel.volumeFP > FixedPoint.FP_ONE) this.channel.volumeFP = FixedPoint.FP_ONE;
                } else {
                    this.channel.volumeSlideTickDeltaFP = 0;
                }

                // panning slide
                if (this.channel.note.effectType == 0x19 ||
                        this.channel.note.volume >= 0xD0 && this.channel.note.volume <= 0xEF) {
                    this.channel.panningLeftFP -= this.channel.panningSlideTickDeltaFP;
                    this.channel.panningRightFP += this.channel.panningSlideTickDeltaFP;
                    if (this.channel.panningLeftFP < 0) this.channel.panningLeftFP = 0;
                    if (this.channel.panningLeftFP > FixedPoint.FP_ONE) this.channel.panningLeftFP = FixedPoint.FP_ONE;
                    if (this.channel.panningRightFP < 0) this.channel.panningRightFP = 0;
                    if (this.channel.panningRightFP > FixedPoint.FP_ONE) this.channel.panningRightFP = FixedPoint.FP_ONE;
                } else {
                    this.channel.panningSlideTickDeltaFP = 0;
                }

                this.advanceEnvelopes(this.channel, this.instruments[this.channel.instrumentColumnMemory]);
                this.doVibrato(this.channel, this.instruments[this.channel.instrumentColumnMemory]);

                // arpeggio
                if (this.channel.note.effectType == 0x00 && this.channel.note.effectParameter > 0) {
                    if (this.defaultTempo > 0) {
                        this.channel.arpeggioTick = this.defaultTempo - (currentTick % this.defaultTempo);
                    }
                    if (this.channel.arpeggioTick > 16) this.channel.arpeggioTick = 2;
                    else if (this.channel.arpeggioTick == 16) this.channel.arpeggioTick = 1;
                    else this.channel.arpeggioTick %= 3;
                    this.arpeggioNoteAdd = (this.channel.note.effectParameter & XMModule.ARPEGGIO_MASK[this.channel.arpeggioTick]) >> XMModule.ARPEGGIO_SHIFT[this.channel.arpeggioTick];
                    this.channel.samplePositionAddTickEnd = (amigaPeriodToFrequency(this.channel.amigaPeriod + this.channel.autoVibratoAmigaPeriodAdd + this.channel.vibratoAmigaPeriodAdd) * (float)Math.pow(2, this.arpeggioNoteAdd / 12.0)) / (float)this.sampleRate;
                    this.channel.samplePositionAddTickStart = this.channel.samplePositionAddTickEnd;
                }

                // portamento up
                if (this.channel.note.effectType == 0x01) {
                    if ((this.channel.portamentoUpDown & 0xFF) > 0) {
                        this.channel.amigaPeriod -= (this.channel.portamentoUpDown << 2);
                        if (this.channel.amigaPeriod < 0) this.channel.amigaPeriod = 0;
                        this.channel.samplePositionAddTickEnd = amigaPeriodToFrequency(this.channel.amigaPeriod + this.channel.autoVibratoAmigaPeriodAdd + this.channel.vibratoAmigaPeriodAdd) / (float)this.sampleRate;
                    }
                }

                // portamento down
                if (this.channel.note.effectType == 0x02) {
                    if ((this.channel.portamentoUpDown & 0xFF) > 0) {
                        this.channel.amigaPeriod += (this.channel.portamentoUpDown << 2);
                        if (this.channel.amigaPeriod > 65535) this.channel.amigaPeriod = 65535;
                        this.channel.samplePositionAddTickEnd = amigaPeriodToFrequency(this.channel.amigaPeriod + this.channel.autoVibratoAmigaPeriodAdd + this.channel.vibratoAmigaPeriodAdd) / (float)this.sampleRate;
                    }
                }

                // tone portamento
                if (this.channel.note.effectType == 0x03 ||
                        this.channel.note.effectType == 0x05 ||
                        (this.channel.note.volume >= 0xF0 && this.channel.note.volume <= 0xFF)) {
                    if (this.channel.tonePortamento > 0 && this.channel.tonePortamentoDestinationAmigaPeriod > 0) {
                        if (this.channel.amigaPeriod < this.channel.tonePortamentoDestinationAmigaPeriod) {
                            this.channel.amigaPeriod += (this.channel.tonePortamento << 2);
                            if (this.channel.amigaPeriod > this.channel.tonePortamentoDestinationAmigaPeriod) {
                                this.channel.amigaPeriod = this.channel.tonePortamentoDestinationAmigaPeriod;
                            }
                        }
                        if (this.channel.amigaPeriod > this.channel.tonePortamentoDestinationAmigaPeriod) {
                            this.channel.amigaPeriod -= (this.channel.tonePortamento << 2);
                            if (this.channel.amigaPeriod < this.channel.tonePortamentoDestinationAmigaPeriod) {
                                this.channel.amigaPeriod = this.channel.tonePortamentoDestinationAmigaPeriod;
                            }
                        }
                        this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod + this.channel.autoVibratoAmigaPeriodAdd + this.channel.vibratoAmigaPeriodAdd) / (float)this.sampleRate;
                    }
                }

                // tremolo
                if (this.channel.note.effectType == 0x07) {
                    this.channel.tremoloPhase += (this.channel.tremolo & 0xF0);
                    this.channel.volumeFactorTickEnd -= (this.channel.TREMOLO_WAVEFORMS.get(this.channel.tremoloWaveform)[this.channel.tremoloPhase & XMChannel.TREMOLO_WAVEFORM_SIZE_MASK] * this.channel.tremoloDepth);
                    if (this.channel.volumeFactorTickEnd < 0) this.channel.volumeFactorTickEnd = 0;
                    if (this.channel.volumeFactorTickEnd > 1) this.channel.volumeFactorTickEnd = 1;
                }

                // extended note effects
                if (this.channel.note.effectType == 0x0E) {
                    // retrigger note
                    if ((this.channel.note.effectParameter & 0xF0) == 0x90) {
                        int retriggerTick = this.channel.note.effectParameter & 0x0F;
                        if (retriggerTick > 0) {
                            if ((currentTick % (retriggerTick)) == 0)
                            {
                                this.channel.samplePosition = XMSample.SAMPLE_START;
                                this.channel.playing = true;
                            }
                        }
                    }
                    // note cut
                    if ((this.channel.note.effectParameter & 0xF0) == 0xC0) {
                        if (currentTick == (this.channel.note.effectParameter & 0x0F)) {
                            this.channel.volumeFP = 0;
                        }
                    }
                    // note delay
                    if ((this.channel.note.effectParameter & 0xF0) == 0xD0) {
                        if (this.channel.noteDelay == currentTick) {
                            this.channel.playing = true;
                        }
                    }
                }

                // global volume slide
                if (this.channel.note.effectType == 0x11) {
                    this.globalVolumeFP += this.globalVolumeSlideTickDeltaFP;
                    if (this.globalVolumeFP < 0) this.globalVolumeFP = 0;
                    if (this.globalVolumeFP > FixedPoint.FP_ONE) this.globalVolumeFP = FixedPoint.FP_ONE;
                }

                // retrigger
                if (this.channel.note.effectType == 0x1B) {
                    // multi retrig note
                    int retriggerTick = this.channel.note.effectParameter & 0x0F;
                    if (retriggerTick > 0) {
                        if ((currentTick % (retriggerTick)) == 0)
                        {
                            this.channel.samplePosition = XMSample.SAMPLE_START;
                            this.channel.playing = true;
                        }
                    }
                    // volume slide
                    switch(this.channel.multiRetrigVolumeSlideMemory) {
                        case 0x01:
                            this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((1 / (float)XMChannel.MAX_VOLUME));
                            break;
                        case 0x02:
                            this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((2 / (float)XMChannel.MAX_VOLUME));
                            break;
                        case 0x03:
                            this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((4 / (float)XMChannel.MAX_VOLUME));
                            break;
                        case 0x04:
                            this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((8 / (float)XMChannel.MAX_VOLUME));
                            break;
                        case 0x05:
                            this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((16 / (float)XMChannel.MAX_VOLUME));
                            break;
                        case 0x06:
                            this.channel.volumeFP = (this.channel.volumeFP * FixedPoint.FLOAT_TO_FP(2.0f/3.0f)) >> FixedPoint.FP_SHIFT;
                            break;
                        case 0x07:
                            this.channel.volumeFP >>= 1;
                            break;
                        case 0x09:
                            this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(1 / (float)XMChannel.MAX_VOLUME);
                            break;
                        case 0x0A:
                            this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(2 / (float)XMChannel.MAX_VOLUME);
                            break;
                        case 0x0B:
                            this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(4 / (float)XMChannel.MAX_VOLUME);
                            break;
                        case 0x0C:
                            this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(8 / (float)XMChannel.MAX_VOLUME);
                            break;
                        case 0x0D:
                            this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(16 / (float)XMChannel.MAX_VOLUME);
                            break;
                        case 0x0E:
                            this.channel.volumeFP = (this.channel.volumeFP * FixedPoint.FLOAT_TO_FP(3.0f/2.0f)) >> FixedPoint.FP_SHIFT;
                            break;
                        case 0x0F:
                            this.channel.volumeFP <<= 1;
                            break;
                    }
                    if (this.channel.volumeFP < 0) this.channel.volumeFP = 0;
                    if (this.channel.volumeFP > FixedPoint.FP_ONE) this.channel.volumeFP = FixedPoint.FP_ONE;
                }

                // tremor
                if (this.channel.note.effectType == 0x1D) {
                    if (this.channel.tremorState == XMChannel.TREMOR_STATE_ON) {
                        if (this.channel.tremorCountOn <= 0) {
                            this.channel.tremorCountOff = this.channel.tremorOffTicks - 1;
                            this.channel.tremorState = XMChannel.TREMOR_STATE_OFF;
                            this.channel.volumeFP = 0;
                        }
                        this.channel.tremorCountOn--;
                    } else if (this.channel.tremorState == XMChannel.TREMOR_STATE_OFF) {
                        if (this.channel.tremorCountOff <= 0) {
                            this.channel.tremorCountOn = this.channel.tremorOnTicks - 1;
                            this.channel.tremorState = XMChannel.TREMOR_STATE_ON;
                            this.channel.volumeFP = FixedPoint.FLOAT_TO_FP(this.channel.sample.volume / (float)XMChannel.MAX_VOLUME);
                        }
                        this.channel.tremorCountOff--;
                    }
                }

            } // firstTick == false

            // volume envelope active?
            if (this.instruments[this.channel.instrumentColumnMemory].volumeEnvelopeEnabled == true) {
                this.channel.volumeFactorTickEnd *= this.channel.volumeEnvelopeValue;
            }

            // fadeout active?
            if (this.channel.fadeOut != XMInstrument.FADEOUT_MAX) {
                this.channel.volumeFactorTickEnd *= (this.channel.fadeOut / (float)XMInstrument.FADEOUT_MAX);
            }

            // panning envelope active?
            if (this.instruments[this.channel.instrumentColumnMemory].panningEnvelopeEnabled == true) {

                // get current panning envelope offset
                float panningEnvelopeOffset = (this.channel.panningEnvelopeValue - 32.0f) / 64.0f;

                this.channel.panningEnvelopeLeftValueTickEnd = -panningEnvelopeOffset;
                this.channel.panningEnvelopeRightValueTickEnd = panningEnvelopeOffset;

                if (this.channel.panningEnvelopeLeftValueTickEnd + FixedPoint.FP_TO_FLOAT(this.channel.panningLeftFP) > 1) {
                    this.channel.panningEnvelopeLeftValueTickEnd -= ((this.channel.panningEnvelopeLeftValueTickEnd + FixedPoint.FP_TO_FLOAT(this.channel.panningLeftFP)) - 1);
                    this.channel.panningEnvelopeRightValueTickEnd = -FixedPoint.FP_TO_FLOAT(this.channel.panningRightFP);
                }
                if (this.channel.panningEnvelopeRightValueTickEnd + FixedPoint.FP_TO_FLOAT(this.channel.panningRightFP) > 1) {
                    this.channel.panningEnvelopeLeftValueTickEnd = -FixedPoint.FP_TO_FLOAT(this.channel.panningLeftFP);
                    this.channel.panningEnvelopeRightValueTickEnd -= ((this.channel.panningEnvelopeRightValueTickEnd + FixedPoint.FP_TO_FLOAT(this.channel.panningRightFP)) - 1);
                }
            }

        } // for 0 ... numberOfChannels

        this.firstTick = false;
        currentTick++;
        if (currentTick == this.defaultTempo) {
            currentTick = 0;
            if (this.positionJump == true)
            {
                // jump to pattern specified by index, in pattern order
                this.firstTick = true;
                this.patternIndex = this.positionJumpIndex;
                if (this.patternIndex < 0) this.patternIndex = 0;
                if (this.patternIndex >= this.songLength)
                {
                    this.patternIndex = this.restartPosition;
                }
                this.pattern = this.patterns[this.patternOrder[this.patternIndex]];
                this.validatePattern();
                this.patternRow = 0;
                this.positionJump = false;

                if (this.patternBreak == true) {
                    this.patternRow = this.patternBreakRow;
                    if (this.patternRow >= this.pattern.numberOfRows)
                    {
                        this.patternRow = 0;
                    }
                    this.patternBreak = false;
                }
            }
            else if (this.patternBreak == true)
            {
                // break to next pattern and go to specified pattern row
                this.firstTick = true;
                this.patternIndex++;
                if (this.patternIndex < 0) this.patternIndex = 0;
                if (this.patternIndex >= this.songLength)
                {
                    this.patternIndex = this.restartPosition;
                }
                this.pattern = this.patterns[this.patternOrder[this.patternIndex]];
                this.validatePattern();
                this.patternRow = this.patternBreakRow;
                if (this.patternRow >= this.pattern.numberOfRows)
                {
                    this.patternRow = 0;
                }
                this.patternBreak = false;
            }
            else
            {
                // go to next row (unless we have a pattern delay)
                if (this.patternDelay == 0)
                {
                    int lastPatternRow = this.patternRow;
                    // go to next row
                    this.patternRow++;
                    this.firstTick = true;
                    if (this.patternRow >= pattern.numberOfRows)
                    {
                        // go to next pattern
                        this.patternRow = 0;
                        int lastPatternIndex = this.patternIndex;
                        this.patternIndex++;
                        if (this.patternIndex < 0) this.patternIndex = 0;
                        if (this.patternIndex >= this.songLength)
                        {
                            // stop if we should play this once
                            this.playing = loopOnEnd;
                            if (this.playing == false) {
                                this.patternIndex = lastPatternIndex;
                                this.patternRow = lastPatternRow;
                            } else {
                                this.patternIndex = this.restartPosition;
                            }
                        }
                        this.pattern = this.patterns[this.patternOrder[this.patternIndex]];
                        this.validatePattern();
                    }
                }
                else
                {
                    // delay for one (less) row
                    this.patternDelay--;
                }
            }
        }
    }

    /**
     * Checks that the current pattern is valid.
     * Whenever we jump to another pattern we must check that it is existing. If not, go to next - or ultimately stop the song.
     */
    private void validatePattern()
    {
        int lastPatternIndex = this.patternIndex;
        // find next available pattern
        while (this.pattern == null)
        {
            this.patternIndex++;
            if (this.patternIndex < 0) this.patternIndex = 0;
            if (this.patternIndex >= this.songLength) {
                if (this.patterns[this.patternOrder[this.restartPosition]] == null)
                {
                    this.patternIndex = 0;
                    if (this.patterns[this.patternOrder[0]] == null) {
                        // stop
                        this.playing = false;
                        return;
                    }
                } else {
                    // stop if we should play this once
                    this.playing = loopOnEnd;
                    if (this.playing == false) {
                        this.patternIndex = lastPatternIndex;
                    } else {
                        this.patternIndex = this.restartPosition;
                    }
                }
            }
            this.pattern = this.patterns[this.patternOrder[this.patternIndex]];
        }
    }


    /**
     * Parses the first tick of a pattern row. The first tick must be treated a bit differently than other ticks.
     * Current EFFECT COLUMN effects not implemented:
     * E3x - Set gliss control
     * E6x - Set loop begin/loop
     */
    private void parseFirstTick()
    {
        boolean portamento = false;

        this.channel.volumeSlideTickDeltaFP = 0;

        if (this.channel.note.effectType == 0x03 ||
                this.channel.note.effectType == 0x05 ||
                (this.channel.note.volume >= 0xF0 && this.channel.note.volume <= 0xFF))
        {
            // tone portamento
            portamento = true;

            if (this.channel.note.instrument == this.channel.instrumentColumnMemory)
            {
                // oldvol - don't play sample, set old default volume
                this.channel.volumeFP = FixedPoint.FLOAT_TO_FP(this.channel.defaultVolumeMemory / (float)XMChannel.MAX_VOLUME);
                this.channel.volumeFactorTickStart = this.channel.defaultVolumeMemory / (float)XMChannel.MAX_VOLUME;
                this.resetEnvelopes(this.channel, this.instruments[this.channel.instrumentColumnMemory]);
            }
            else if (this.channel.note.instrument > 0)
            {
                // can't porta slide to another instrument
                portamento = false;
            }

            if (portamento == true)
            {
                if (this.channel.note.note == XMModule.NOTE_KEYOFF)
                {
                    // release
                    this.channel.inRelease = true;
                }
                if (this.channel.note.note > 0 && this.channel.note.note != XMModule.NOTE_KEYOFF)
                {
                    // handle E5x - extra finetune override
                    int finetune = this.channel.sample.finetune;
                    if (this.channel.note.effectType == 0x0E && ((this.channel.note.effectParameter & 0xF0) == 0x50)) {
                        finetune = ((this.channel.note.effectParameter & 0x0F) - 0x08) << 4;
                    }

                    // set new destination period
                    this.channel.tonePortamentoDestinationAmigaPeriod = XMModule.noteAndFineTuneToAmigaPeriod(this.channel.sample.relativeNote, this.channel.note.note, finetune);
                }
                if (this.channel.note.volume >= 0xF0 && this.channel.note.volume <= 0xFF)
                {
                    if ((this.channel.note.volume & 0x0F) > 0)
                    {
                        this.channel.tonePortamento = (this.channel.note.volume & 0x0F) << 4;
                    }
                }
                else if (this.channel.note.effectType == 0x3)
                {
                    if ((this.channel.note.effectParameter & 0xFF) > 0)
                    {
                        this.channel.tonePortamento = this.channel.note.effectParameter;
                    }
                }
            } // portamento == true

        } // portamento, portamento slide, volume slide portamento

        // new instrument - no note
        if (this.channel.note.note == 0 && this.channel.note.instrument > 0 && portamento == false)
        {
            this.channel.instrumentColumnMemory = this.channel.note.instrument;
            if (this.channel.instrumentColumnMemory >= this.numberOfInstruments) this.channel.instrumentColumnMemory = 0;
            if (this.channel.instrumentColumnMemory < 0) this.channel.instrumentColumnMemory = 0;

            if (this.channel.instrumentColumnMemory > 0)
            {
                // oldvol - don't play sample, set old default volume
                this.channel.volumeFP = FixedPoint.FLOAT_TO_FP(this.channel.defaultVolumeMemory / (float)XMChannel.MAX_VOLUME);
                this.channel.volumeFactorTickStart = this.channel.defaultVolumeMemory / (float)XMChannel.MAX_VOLUME;
                this.resetEnvelopes(this.channel, this.instruments[this.channel.instrumentColumnMemory]);

                if ((this.channel.note.effectType == 0x0E) && ((this.channel.note.effectParameter & 0xF0) == 0xD0))
                {
                    // note delay
                    if ((this.channel.note.effectParameter & 0x0F) > 0)
                    {
                        this.channel.playing = false;
                        this.channel.noteDelay = this.channel.note.effectParameter & 0x0F;
                    }
                }
            }
        }

        // regular note on
        if (this.channel.note.note > 0 && portamento == false)
        {
            if (this.channel.note.note == XMModule.NOTE_KEYOFF)
            {
                this.channel.inRelease = true;
            }
            else if (this.channel.note.instrument == 0)
            {
                // switch - play new note with current volume
                if (this.channel.instrumentColumnMemory > 0 && this.channel.instrumentColumnMemory < this.numberOfInstruments)
                {
                    this.channel.playing = true;
                    this.channel.sample = this.instruments[this.channel.instrumentColumnMemory].samples[this.instruments[this.channel.instrumentColumnMemory].noteToSampleIndex[this.channel.note.note - 1]];

                    // handle E5x - extra finetune override
                    int finetune = this.channel.sample.finetune;
                    if (this.channel.note.effectType == 0x0E && ((this.channel.note.effectParameter & 0xF0) == 0x50)) {
                        finetune = ((this.channel.note.effectParameter & 0x0F) - 0x08) << 4;
                    }
                    this.channel.amigaPeriod = XMModule.noteAndFineTuneToAmigaPeriod(this.channel.sample.relativeNote, this.channel.note.note, finetune);

                    this.channel.samplePositionAddTickStart = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
                    this.channel.samplePositionAddTickEnd = this.channel.samplePositionAddTickStart;
                    this.channel.samplePosition = XMSample.SAMPLE_START;
                }

                if (this.channel.note.effectType == 0x09)
                {
                    // sample offset
                    if (this.channel.note.effectParameter > 0)
                    {
                        this.channel.sampleOffsetMemory = this.channel.note.effectParameter;
                    }
                    this.channel.playing = true;
                    this.channel.samplePosition = (((this.channel.sampleOffsetMemory & 0xFF) << 8) + XMSample.SAMPLE_START);
                }

                if ((this.channel.note.effectType == 0x0E) && ((this.channel.note.effectParameter & 0xF0) == 0xD0))
                {
                    // note delay
                    if ((this.channel.note.effectParameter & 0x0F) > 0)
                    {
                        this.channel.playing = false;
                        this.channel.noteDelay = this.channel.note.effectParameter & 0x0F;
                    }
                }
            }

            if (this.channel.note.instrument > 0 && this.channel.note.instrument < this.numberOfInstruments && this.channel.note.note != XMModule.NOTE_KEYOFF)
            {

                // play - play new note with new default volume
                this.channel.playing = true;
                this.channel.instrumentColumnMemory = this.channel.note.instrument;
                this.channel.sample = this.instruments[this.channel.instrumentColumnMemory].samples[this.instruments[this.channel.instrumentColumnMemory].noteToSampleIndex[this.channel.note.note - 1]];
                this.channel.volumeFP = FixedPoint.FLOAT_TO_FP(this.channel.sample.volume / (float)XMChannel.MAX_VOLUME);
                this.channel.volumeFP_LPF = 0;
                this.channel.defaultVolumeMemory = this.channel.sample.volume;
                this.channel.volumeFactorTickStart = this.channel.defaultVolumeMemory / (float)XMChannel.MAX_VOLUME;
                this.resetEnvelopes(this.channel, this.instruments[this.channel.instrumentColumnMemory]);
                this.channel.autoVibratoEnabled = (this.instruments[this.channel.instrumentColumnMemory].vibratoDepth > 0);
                this.channel.samplePosition = XMSample.SAMPLE_START;

                // handle E5x - extra finetune override
                int finetune = this.channel.sample.finetune;
                if (this.channel.note.effectType == 0x0E && ((this.channel.note.effectParameter & 0xF0) == 0x50)) {
                    finetune = ((this.channel.note.effectParameter & 0x0F) - 0x08) << 4;
                }
                this.channel.amigaPeriod = XMModule.noteAndFineTuneToAmigaPeriod(this.channel.sample.relativeNote, this.channel.note.note, finetune);

                this.channel.samplePositionAddTickStart = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
                this.channel.samplePositionAddTickEnd = this.channel.samplePositionAddTickStart;
                this.channel.panningLeftFP = this.PANNING_PARAMETER_TO_PANNING_LEFT_FP[this.channel.sample.panning & 0xFF];
                this.channel.panningRightFP = this.PANNING_PARAMETER_TO_PANNING_RIGHT_FP[this.channel.sample.panning & 0xFF];

                if (this.channel.note.effectType == 0x09)
                {
                    // sample offset
                    if (this.channel.note.effectParameter > 0)
                    {
                        this.channel.sampleOffsetMemory = this.channel.note.effectParameter;
                    }
                    this.channel.samplePosition = (((this.channel.sampleOffsetMemory & 0xFF) << 8) + XMSample.SAMPLE_START);
                }

                if ((this.channel.note.effectType == 0x0E) && ((this.channel.note.effectParameter & 0xF0) == 0xD0))
                {
                    // note delay
                    if ((this.channel.note.effectParameter & 0x0F) > 0)
                    {
                        this.channel.playing = false;
                        this.channel.noteDelay = this.channel.note.effectParameter & 0x0F;
                    }
                }
            }

            if (this.channel.note.instrument >= this.numberOfInstruments && this.channel.note.note != XMModule.NOTE_KEYOFF)
            {
                // cut - stop playing sample
                this.channel.playing = false;
            }

            // reset vibrato phase
            this.channel.vibratoPhase = 0;
            // reset autovibrato
            this.channel.autoVibratoTicks = 0;
            this.channel.autoVibratoPhase = 0;
            // reset tremor counts
            this.channel.tremorCountOn = this.channel.tremorOnTicks - 1;
            this.channel.tremorCountOff = this.channel.tremorOffTicks - 1;

        } //  regular note on

        // VOLUME COLUMN EFFECTS

        // set volume
        if (this.channel.note.volume >= 0x10 && this.channel.note.volume <= 0x50)
        {
            this.channel.volumeFP = FixedPoint.FLOAT_TO_FP((this.channel.note.volume - 0x10) / (float)XMChannel.MAX_VOLUME);
        }

        // volume slide down
        if (this.channel.note.volume >= 0x60 && this.channel.note.volume <= 0x6F)
        {
            this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((this.channel.note.volume & 0x0F) / (float)XMChannel.MAX_VOLUME);
            if (this.channel.volumeFP < 0) this.channel.volumeFP = 0;
        }

        // volume slide up
        if (this.channel.note.volume >= 0x70 && this.channel.note.volume <= 0x7F)
        {
            this.channel.volumeFP += FixedPoint.FLOAT_TO_FP((this.channel.note.volume & 0x0F) / (float)XMChannel.MAX_VOLUME);
            if (this.channel.volumeFP > FixedPoint.FP_ONE) this.channel.volumeFP = FixedPoint.FP_ONE;
        }

        // fine volume slide down
        if (this.channel.note.volume >= 0x80 && this.channel.note.volume <= 0x8F)
        {
            this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((this.channel.note.volume & 0x0F) / (float)XMChannel.MAX_VOLUME);
            if (this.channel.volumeFP < 0) this.channel.volumeFP = 0;
        }

        // fine volume slide up
        if (this.channel.note.volume >= 0x90 && this.channel.note.volume <= 0x9F)
        {
            this.channel.volumeFP += FixedPoint.FLOAT_TO_FP((this.channel.note.volume & 0x0F) / (float)XMChannel.MAX_VOLUME);
            if (this.channel.volumeFP > FixedPoint.FP_ONE) this.channel.volumeFP = FixedPoint.FP_ONE;
        }

        // set vibrato speed
        if (this.channel.note.volume >= 0xA0 && this.channel.note.volume <= 0xAF)
        {
            this.channel.vibrato = ((this.channel.note.volume & 0x0F) << 4) | (this.channel.vibrato & 0x0F);
        }

        // vibrato + set vibrato depth
        if (this.channel.note.volume >= 0xB0 && this.channel.note.volume <= 0xBF)
        {
            if ((this.channel.note.volume & 0x0F) > 0)
            {
                this.channel.vibratoDepth = (this.channel.note.volume & 0x0F);
                if ((this.channel.vibratoWaveform & 0x0F) > 0x03)
                {
                    // reset vibrato phase
                    this.channel.vibratoPhase = 0;
                }
            }
        }

        // set panning
        if (this.channel.note.volume >= 0xC0 && this.channel.note.volume <= 0xCF)
        {
            this.channel.panningLeftFP = this.PANNING_PARAMETER_TO_PANNING_LEFT_FP[((this.channel.note.volume & 0x0F) * 17) & 0xFF];
            this.channel.panningRightFP = this.PANNING_PARAMETER_TO_PANNING_RIGHT_FP[((this.channel.note.volume & 0x0F) * 17) & 0xFF];
        }

        // panning slide left
        if (this.channel.note.volume >= 0xD0 && this.channel.note.volume <= 0xDF)
        {
            this.channel.panningSlideTickDeltaFP = FixedPoint.FLOAT_TO_FP(-((this.channel.note.volume & 0x0F)) / 255.0f);
        }

        // panning slide right
        if (this.channel.note.volume >= 0xE0 && this.channel.note.volume <= 0xEF)
        {
            this.channel.panningSlideTickDeltaFP = FixedPoint.FLOAT_TO_FP((this.channel.note.volume & 0x0F) / 255.0f);
        }

        // EFFECT COLUMN EFFECTS

        // arpeggio
        if (this.channel.note.effectType == 0x00 && this.channel.note.effectParameter > 0)
        {
            this.channel.arpeggioTick = 0;
            if (!(this.channel.note.volume >= 0xB0 && this.channel.note.volume <= 0xBF))
            {
                this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
                this.channel.samplePositionAddTickStart = this.channel.samplePositionAddTickEnd;
            }
            return;
        }

        // portamento up
        if (this.channel.note.effectType == 0x01)
        {
            if ((this.channel.note.effectParameter & 0xFF) > 0)
            {
                this.channel.portamentoUpDown = this.channel.note.effectParameter & 0xFF;
                this.channel.amigaPeriod -= (this.channel.portamentoUpDown << 2);
                if (this.channel.amigaPeriod < 0) this.channel.amigaPeriod = 0;
                this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
            }
            return;
        }

        // portamento down
        if (this.channel.note.effectType == 0x02)
        {
            if ((this.channel.note.effectParameter & 0xFF) > 0)
            {
                this.channel.portamentoUpDown = this.channel.note.effectParameter & 0xFF;
                this.channel.amigaPeriod += (this.channel.portamentoUpDown << 2);
                if (this.channel.amigaPeriod > 65535) this.channel.amigaPeriod = 65535;
                this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
            }
            return;
        }

        // vibrato
        if (this.channel.note.effectType == 0x04)
        {
            if (this.channel.note.effectParameter > 0)
            {
                this.channel.vibrato = this.channel.note.effectParameter & 0xFF;
                if ((this.channel.vibratoWaveform & 0x0F) > 0x03)
                {
                    // reset vibrato phase
                    this.channel.vibratoPhase = 0;
                }
                this.channel.vibratoDepth = (this.channel.note.effectParameter & 0x0F);
            }
            return;
        }

        // volume slide
        if (this.channel.note.effectType == 0x05 ||
                this.channel.note.effectType == 0x06 ||
                this.channel.note.effectType == 0x0A)
        {
            if ((this.channel.note.effectParameter & 0x0F) == 0 || (this.channel.note.effectParameter & 0xF0) == 0)
            {
                int volumeSlide = this.channel.note.effectParameter;
                if (volumeSlide == 0) volumeSlide = this.channel.volumeSlideMemory;
                this.channel.volumeSlideMemory = volumeSlide & 0xFF;
                if ((volumeSlide & 0x0F) == 0)
                {
                    // fade up
                    this.channel.volumeSlideTickDeltaFP = FixedPoint.FLOAT_TO_FP(((volumeSlide & 0xF0) >> 4) / (float)XMChannel.MAX_VOLUME);
                }
                else
                {
                    // fade down
                    this.channel.volumeSlideTickDeltaFP = FixedPoint.FLOAT_TO_FP(-volumeSlide / (float)XMChannel.MAX_VOLUME);
                }
            }
            return;
        }

        // tremolo
        if (this.channel.note.effectType == 0x07)
        {
            if (this.channel.note.effectParameter > 0)
            {
                this.channel.tremolo = this.channel.note.effectParameter & 0xFF;
                if ((this.channel.tremoloWaveform & 0x0F) > 0x03)
                {
                    // reset tremolo phase
                    this.channel.tremoloPhase = 0;
                }
                this.channel.tremoloDepth = ((this.channel.note.effectParameter & 0x0F) / (float)0x0F);
            }
            this.channel.volumeFactorTickEnd -= (this.channel.TREMOLO_WAVEFORMS.get(this.channel.tremoloWaveform)[this.channel.tremoloPhase & XMChannel.TREMOLO_WAVEFORM_SIZE_MASK] * this.channel.tremoloDepth);
            if (this.channel.volumeFactorTickEnd < 0) this.channel.volumeFactorTickEnd = 0;
            if (this.channel.volumeFactorTickEnd > 1) this.channel.volumeFactorTickEnd = 1;
            return;
        }

        // set panning
        if (this.channel.note.effectType == 0x08)
        {
            this.channel.panningLeftFP = this.PANNING_PARAMETER_TO_PANNING_LEFT_FP[this.channel.note.effectParameter & 0xFF];
            this.channel.panningRightFP = this.PANNING_PARAMETER_TO_PANNING_RIGHT_FP[this.channel.note.effectParameter & 0xFF];
            return;
        }

        // position jump
        if (this.channel.note.effectType == 0x0B)
        {
            this.positionJump = true;
            this.positionJumpIndex = this.channel.note.effectParameter & 0xFF;
            return;
        }

        // set volume
        if (this.channel.note.effectType == 0x0C)
        {
            int newVolume = this.channel.note.effectParameter & 0xFF;
            if (newVolume > XMChannel.MAX_VOLUME)
            {
                this.channel.volumeFP = FixedPoint.FLOAT_TO_FP(1.0f);
            }
            else
            {
                this.channel.volumeFP = FixedPoint.FLOAT_TO_FP(newVolume / (float)XMChannel.MAX_VOLUME);
            }
            return;
        }

        // pattern break
        if (this.channel.note.effectType == 0x0D)
        {
            this.patternBreak = true;
            this.patternBreakRow = ((this.channel.note.effectParameter & 0xF0) >> 4) * 10 + this.channel.note.effectParameter & 0x0F;
            return;
        }

        // extended effect (E5x has been moved because it must be coupled with a note on)
        if (this.channel.note.effectType == 0x0E)
        {
            // fine portamento up
            if ((this.channel.note.effectParameter & 0xF0) == 0x10)
            {
                if ((this.channel.note.effectParameter & 0x0F) > 0)
                {
                    this.channel.finePortamentoUp = this.channel.note.effectParameter & 0x0F;
                }
                if (this.channel.finePortamentoUp > 0)
                {
                    this.channel.amigaPeriod -= (this.channel.finePortamentoUp << 2);
                    if (this.channel.amigaPeriod < 0) this.channel.amigaPeriod = 0;
                    this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
                }
            }

            // fine portamento down
            if ((this.channel.note.effectParameter & 0xF0) == 0x20)
            {
                if ((this.channel.note.effectParameter & 0x0F) > 0)
                {
                    this.channel.finePortamentoDown = this.channel.note.effectParameter & 0x0F;
                }
                if (this.channel.finePortamentoDown > 0)
                {
                    this.channel.amigaPeriod += (this.channel.finePortamentoDown << 2);
                    if (this.channel.amigaPeriod > 65535) this.channel.amigaPeriod = 65535;
                    this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
                }
            }

            // set vibrato waveform
            if ((this.channel.note.effectParameter & 0xF0) == 0x40)
            {
                if ((this.channel.note.effectParameter & 0x0F) < 8)
                {
                    this.channel.vibratoWaveform = this.channel.note.effectParameter & 0x0F;
                }
            }

            // set tremolo waveform
            if ((this.channel.note.effectParameter & 0xF0) == 0x70)
            {
                if ((this.channel.note.effectParameter & 0x0F) < 8)
                {
                    this.channel.tremoloWaveform = this.channel.note.effectParameter & 0x0F;
                }
            }

            // fine volume slide up
            if ((this.channel.note.effectParameter & 0xF0) == 0xA0)
            {
                if ((this.channel.note.effectParameter & 0x0F) != 0)
                {
                    this.channel.volumeSlideUpFineMemory = this.channel.note.effectParameter & 0x0F;
                }
                this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(this.channel.volumeSlideUpFineMemory / (float)XMChannel.MAX_VOLUME);
                if (this.channel.volumeFP > FixedPoint.FP_ONE) this.channel.volumeFP = FixedPoint.FP_ONE;
            }

            // fine volume slide down
            if ((this.channel.note.effectParameter & 0xF0) == 0xB0)
            {
                if ((this.channel.note.effectParameter & 0x0F) != 0)
                {
                    this.channel.volumeSlideDownFineMemory = this.channel.note.effectParameter & 0x0F;
                }
                this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP(this.channel.volumeSlideDownFineMemory / (float)XMChannel.MAX_VOLUME);
                if (this.channel.volumeFP < 0) this.channel.volumeFP = 0;
            }

            // note cut
            if (this.channel.note.effectParameter == 0xC0)
            {
                this.channel.volumeFP = 0;
            }

            // pattern delay
            if ((this.channel.note.effectParameter & 0xF0) == 0xE0)
            {
                if ((this.channel.note.effectParameter & 0x0F) > 0)
                {
                    this.patternDelay = this.channel.note.effectParameter & 0x0F;
                }
            }
            return;
        }

        // set tempo
        if (this.channel.note.effectType == 0x0F)
        {
            if (this.channel.note.effectParameter > 0)
            {
                if (this.channel.note.effectParameter < 0x20)
                {
                    this.defaultTempo = this.channel.note.effectParameter & 0xFF;
                }
                else
                {
                    this.setBPM(this.channel.note.effectParameter & 0xFF);
                }
            }
            return;
        }

        // set global volume
        if (this.channel.note.effectType == 0x10)
        {
            if ((this.channel.note.effectParameter & 0xFF) <= XMChannel.MAX_VOLUME)
            {
                this.globalVolumeFP = FixedPoint.FLOAT_TO_FP((this.channel.note.effectParameter & 0xFF) / (float)XMChannel.MAX_VOLUME);
            }
            return;
        }

        // global volume slide
        if (this.channel.note.effectType == 0x11)
        {
            if ((this.channel.note.effectParameter & 0x0F) == 0 || (this.channel.note.effectParameter & 0xF0) == 0)
            {
                int volumeSlide = this.channel.note.effectParameter;
                if (volumeSlide == 0) volumeSlide = this.globalVolumeSlideMemory;
                this.globalVolumeSlideMemory = volumeSlide & 0xFF;
                if ((volumeSlide & 0x0F) == 0)
                {
                    // fade up
                    volumeSlide = (volumeSlide & 0xF0) >> 4;
                }
                else
                {
                    // fade down
                    volumeSlide = -volumeSlide;
                }
                this.globalVolumeSlideTickDeltaFP = FixedPoint.FLOAT_TO_FP(volumeSlide / (float)XMChannel.MAX_VOLUME);
            }
            return;
        }

        // set envelope position
        if (this.channel.note.effectType == 0x15)
        {
            this.gotoEnvelopeTick(this.channel.note.effectParameter & 0xFF, this.channel, this.instruments[this.channel.instrumentColumnMemory]);
            return;
        }

        // panning slide
        if (this.channel.note.effectType == 0x19)
        {
            if ((this.channel.note.effectParameter & 0x0F) == 0 || (this.channel.note.effectParameter & 0xF0) == 0)
            {
                int panningSlide = this.channel.note.effectParameter;
                if (panningSlide == 0) panningSlide = this.channel.panningSlideMemory;
                this.channel.panningSlideMemory = panningSlide & 0xFF;
                if ((panningSlide & 0x0F) == 0)
                {
                    // pan right
                    this.channel.panningSlideTickDeltaFP = FixedPoint.FLOAT_TO_FP(((this.channel.panningSlideMemory & 0xF0) >> 4) / 255.0f);
                }
                else
                {
                    // pan left
                    this.channel.panningSlideTickDeltaFP = FixedPoint.FLOAT_TO_FP(-(this.channel.panningSlideMemory & 0x0F) / 255.0f);
                }
            }
            return;
        }

        // multi retrig note
        if (this.channel.note.effectType == 0x1B)
        {
            if ((this.channel.note.effectParameter & 0xF0) != 0)
            {
                this.channel.multiRetrigVolumeSlideMemory = (this.channel.note.effectParameter & 0xF0) >> 4;
            }
            switch (this.channel.multiRetrigVolumeSlideMemory)
            {
                case 0x01:
                    this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((1 / (float)XMChannel.MAX_VOLUME));
                    break;
                case 0x02:
                    this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((2 / (float)XMChannel.MAX_VOLUME));
                    break;
                case 0x03:
                    this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((4 / (float)XMChannel.MAX_VOLUME));
                    break;
                case 0x04:
                    this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((8 / (float)XMChannel.MAX_VOLUME));
                    break;
                case 0x05:
                    this.channel.volumeFP -= FixedPoint.FLOAT_TO_FP((16 / (float)XMChannel.MAX_VOLUME));
                    break;
                case 0x06:
                    this.channel.volumeFP = (this.channel.volumeFP * FixedPoint.FLOAT_TO_FP(2.0f / 3.0f)) >> FixedPoint.FP_SHIFT;
                    break;
                case 0x07:
                    this.channel.volumeFP >>= 1;
                    break;
                case 0x09:
                    this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(1 / (float)XMChannel.MAX_VOLUME);
                    break;
                case 0x0A:
                    this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(2 / (float)XMChannel.MAX_VOLUME);
                    break;
                case 0x0B:
                    this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(4 / (float)XMChannel.MAX_VOLUME);
                    break;
                case 0x0C:
                    this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(8 / (float)XMChannel.MAX_VOLUME);
                    break;
                case 0x0D:
                    this.channel.volumeFP += FixedPoint.FLOAT_TO_FP(16 / (float)XMChannel.MAX_VOLUME);
                    break;
                case 0x0E:
                    this.channel.volumeFP = (this.channel.volumeFP * FixedPoint.FLOAT_TO_FP(3.0f / 2.0f)) >> FixedPoint.FP_SHIFT;
                    break;
                case 0x0F:
                    this.channel.volumeFP <<= 1;
                    break;
            }
            if (this.channel.volumeFP < 0) this.channel.volumeFP = 0;
            if (this.channel.volumeFP > FixedPoint.FP_ONE) this.channel.volumeFP = FixedPoint.FP_ONE;
            return;
        }

        // tremor
        if (this.channel.note.effectType == 0x1D)
        {
            if (this.channel.note.effectParameter > 0)
            {
                this.channel.tremorMemory = this.channel.note.effectParameter & 0xFF;
            }
            this.channel.tremorOnTicks = (this.channel.tremorMemory & 0xF0) >> 4;
            this.channel.tremorOffTicks = (this.channel.tremorMemory & 0x0F);
            if (this.channel.tremorOnTicks == 0) this.channel.tremorOnTicks++;
            if (this.channel.tremorOffTicks == 0) this.channel.tremorOffTicks++;

            if (this.channel.tremorState == XMChannel.TREMOR_STATE_ON)
            {
                if (this.channel.tremorCountOn <= 0)
                {
                    this.channel.tremorCountOff = this.channel.tremorOffTicks - 1;
                    this.channel.tremorState = XMChannel.TREMOR_STATE_OFF;
                    this.channel.volumeFP = 0;
                }
                this.channel.tremorCountOn--;
            }
            else if (this.channel.tremorState == XMChannel.TREMOR_STATE_OFF)
            {
                if (this.channel.tremorCountOff <= 0)
                {
                    this.channel.tremorCountOn = this.channel.tremorOnTicks - 1;
                    this.channel.tremorState = XMChannel.TREMOR_STATE_ON;
                    this.channel.volumeFP = FixedPoint.FLOAT_TO_FP(this.channel.sample.volume / (float)XMChannel.MAX_VOLUME);
                }
                this.channel.tremorCountOff--;
            }
            return;
        }

        // extra fine porta
        if (this.channel.note.effectType == 0x21)
        {
            if ((this.channel.note.effectParameter & 0xF0) == 0x10)
            {
                // extra fine porta up
                if ((this.channel.note.effectParameter & 0x0F) > 0)
                {
                    this.channel.extraFinePortamentoUp = this.channel.note.effectParameter & 0x0F;
                }
                if (this.channel.extraFinePortamentoUp > 0)
                {
                    this.channel.amigaPeriod -= this.channel.extraFinePortamentoUp;
                    if (this.channel.amigaPeriod < 0) this.channel.amigaPeriod = 0;
                    this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
                }
            }
            if ((this.channel.note.effectParameter & 0xF0) == 0x20)
            {
                // extra fina porta down
                if ((this.channel.note.effectParameter & 0x0F) > 0)
                {
                    this.channel.extraFinePortamentoDown = this.channel.note.effectParameter & 0x0F;
                }
                if (this.channel.extraFinePortamentoDown > 0)
                {
                    this.channel.amigaPeriod += this.channel.extraFinePortamentoDown;
                    if (this.channel.amigaPeriod > 65535) this.channel.amigaPeriod = 65535;
                    this.channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(this.channel.amigaPeriod) / (float)this.sampleRate;
                }
            }
            return;
        }
    }

    private void resetEnvelopes(XMChannel channel, XMInstrument instrument)
    {
        channel.fadeOut = XMInstrument.FADEOUT_MAX;
        channel.inRelease = false;

        if (instrument.volumeEnvelopeEnabled == true)
        {
            channel.volumeEnvelopeDone = false;
            channel.volumeEnvelopeTick = 0;
            channel.volumeEnvelopePoint = 0;

            channel.volumeEnvelopeValue = instrument.volumeEnvelopeValues[0] / (float)XMChannel.MAX_VOLUME;
            channel.volumeFactorTickStart = channel.volumeEnvelopeValue;
            channel.volumeEnvelopeValueTickAdd = ((instrument.volumeEnvelopeValues[1] - instrument.volumeEnvelopeValues[0]) / (float)XMChannel.MAX_VOLUME) / (float)instrument.volumeEnvelopeTicks[1];
        }

        if (instrument.panningEnvelopeEnabled == true)
        {
            channel.panningEnvelopeDone = false;
            channel.panningEnvelopeTick = 0;
            channel.panningEnvelopePoint = 0;

            channel.panningEnvelopeValue = instrument.panningEnvelopeValues[0];
            channel.panningEnvelopeValueTickAdd = (instrument.panningEnvelopeValues[1] - instrument.panningEnvelopeValues[0]) / (float)instrument.panningEnvelopeTicks[1];
        }
    }

    private void advanceEnvelopes(XMChannel channel, XMInstrument instrument)
    {
        // advance fadeout
        if (channel.inRelease == true)
        {
            if (instrument.volumeEnvelopeEnabled == true)
            {
                // fade out
                channel.fadeOut -= instrument.fadeout;

                if (channel.fadeOut <= 0)
                {
                    // done
                    channel.fadeOut = 0;
                    channel.volumeEnvelopeDone = true;
                    return;
                }
            }
            else
            {
                // done
                channel.fadeOut = 0;
            }
        }

        // advance panning envelope
        this.advancePanningEnvelope(channel, instrument);

        // advance volume envelope
        if (instrument.volumeEnvelopeEnabled == true && channel.volumeEnvelopeDone == false)
        {
            if (instrument.volumeEnvelopeTicks[channel.volumeEnvelopePoint] == channel.volumeEnvelopeTick)
            {

                if (instrument.volumeEnvelopeSustainEnabled == true &&
                        instrument.volumeEnvelopeSustainPointIndex == (channel.volumeEnvelopePoint) &&
                        channel.inRelease == false)
                {
                    channel.volumeEnvelopeValue = instrument.volumeEnvelopeValues[instrument.volumeEnvelopeSustainPointIndex] / (float)XMChannel.MAX_VOLUME;
                }

                if (instrument.volumeEnvelopeLoopEnabled == true &&
                        instrument.volumeEnvelopeLoopEndPointIndex == (channel.volumeEnvelopePoint))
                {
                    // loop
                    channel.volumeEnvelopePoint = instrument.volumeEnvelopeLoopStartPointIndex;
                    channel.volumeEnvelopeTick = instrument.volumeEnvelopeTicks[channel.volumeEnvelopePoint];
                }

                if (instrument.volumeEnvelopeNumberOfPoints == (channel.volumeEnvelopePoint + 1))
                {
                    // done
                    channel.volumeEnvelopeValue = instrument.volumeEnvelopeValues[channel.volumeEnvelopePoint] / (float)XMChannel.MAX_VOLUME;
                    channel.volumeEnvelopeDone = true;
                    return;
                }

                int nextPoint = channel.volumeEnvelopePoint + 1;
                channel.volumeEnvelopeValue = instrument.volumeEnvelopeValues[channel.volumeEnvelopePoint] / (float)XMChannel.MAX_VOLUME;
                channel.volumeEnvelopeValueTickAdd = ((instrument.volumeEnvelopeValues[nextPoint] - instrument.volumeEnvelopeValues[channel.volumeEnvelopePoint]) / (float)XMChannel.MAX_VOLUME) / (float)(instrument.volumeEnvelopeTicks[nextPoint] - instrument.volumeEnvelopeTicks[channel.volumeEnvelopePoint]);
                channel.volumeEnvelopePoint++;
            }

            channel.volumeEnvelopeValue += channel.volumeEnvelopeValueTickAdd;
            channel.volumeEnvelopeTick++;
        }
    }

    private void advancePanningEnvelope(XMChannel channel, XMInstrument instrument)
    {
        // advance panning envelope
        if (instrument.panningEnvelopeEnabled == true && channel.panningEnvelopeDone == false)
        {
            if (instrument.panningEnvelopeTicks[channel.panningEnvelopePoint] == channel.panningEnvelopeTick)
            {

                if (instrument.panningEnvelopeSustainEnabled == true &&
                        instrument.panningEnvelopeSustainPointIndex == (channel.panningEnvelopePoint) &&
                        channel.inRelease == false)
                {
                    channel.panningEnvelopeValue = instrument.panningEnvelopeValues[instrument.panningEnvelopeSustainPointIndex];
                }

                if (instrument.panningEnvelopeLoopEnabled == true &&
                        instrument.panningEnvelopeLoopEndPointIndex == (channel.panningEnvelopePoint))
                {
                    // loop
                    channel.panningEnvelopePoint = instrument.panningEnvelopeLoopStartPointIndex;
                    channel.panningEnvelopeTick = instrument.panningEnvelopeTicks[channel.panningEnvelopePoint];
                }

                if (instrument.panningEnvelopeNumberOfPoints == (channel.panningEnvelopePoint + 1))
                {
                    // done
                    channel.panningEnvelopeValue = instrument.panningEnvelopeValues[channel.panningEnvelopePoint];
                    channel.panningEnvelopeDone = true;
                    return;
                }

                int nextPoint = channel.panningEnvelopePoint + 1;
                channel.panningEnvelopeValue = instrument.panningEnvelopeValues[channel.panningEnvelopePoint];
                channel.panningEnvelopeValueTickAdd = (instrument.panningEnvelopeValues[nextPoint] - instrument.panningEnvelopeValues[channel.panningEnvelopePoint]) / (float)(instrument.panningEnvelopeTicks[nextPoint] - instrument.panningEnvelopeTicks[channel.panningEnvelopePoint]);
                channel.panningEnvelopePoint++;
            }

            channel.panningEnvelopeValue += channel.panningEnvelopeValueTickAdd;
            channel.panningEnvelopeTick++;
        }
    }

    private void gotoEnvelopeTick(int tick, XMChannel channel, XMInstrument instrument)
    {
        if (instrument.volumeEnvelopeEnabled == true)
        {
            // find tick in volume envelope
            int envelopePoint = 0;
            for(int i=0;i<instrument.volumeEnvelopeNumberOfPoints;i++)
            {
                if (tick < instrument.volumeEnvelopeTicks[i])
                {
                    envelopePoint = i;
                    break;
                }
            }

            if (envelopePoint > 0)
            {
                channel.volumeEnvelopePoint = envelopePoint;
                channel.volumeEnvelopeTick = tick;
            }
        }

        if (instrument.panningEnvelopeEnabled == true)
        {
            // find tick in panning envelope
            int envelopePoint = 0;
            for(int i=0;i<instrument.panningEnvelopeNumberOfPoints;i++)
            {
                if (tick < instrument.panningEnvelopeTicks[i])
                {
                    envelopePoint = i;
                    break;
                }
            }

            if (envelopePoint > 0)
            {
                channel.panningEnvelopePoint = envelopePoint;
                channel.panningEnvelopeTick = tick;
            }
        }
    }

    private void doVibrato(XMChannel channel, XMInstrument instrument)
    {

        if (channel.autoVibratoEnabled == true)
        {
            // calculate sweep factor
            float sweepFactor = 1.0f;
            if (instrument.vibratoSweep > 0)
            {
                sweepFactor = channel.autoVibratoTicks / (float)instrument.vibratoSweep;
                if (sweepFactor > 1.0f) sweepFactor = 1.0f;
            }

            channel.autoVibratoAmigaPeriodAdd = (channel.AUTOVIBRATO_WAVEFORMS.get(instrument.vibratoType)[channel.autoVibratoPhase & XMChannel.AUTOVIBRATO_WAVEFORM_SIZE_MASK] * instrument.vibratoDepth) * sweepFactor;
            channel.autoVibratoPhase += (instrument.vibratoRate);
            channel.autoVibratoTicks++;
            if (channel.autoVibratoTicks > instrument.vibratoSweep) channel.autoVibratoTicks = instrument.vibratoSweep;
        } else {
            channel.autoVibratoAmigaPeriodAdd = 0;
        }

        // vibrato
        if (channel.note.effectType == 0x04 ||
                channel.note.effectType == 0x06 ||
                (channel.note.volume >= 0xB0 && channel.note.volume <= 0xBF)) {
            channel.vibratoAmigaPeriodAdd = channel.VIBRATO_WAVEFORMS.get(channel.vibratoWaveform)[channel.vibratoPhase & XMChannel.VIBRATO_WAVEFORM_SIZE_MASK] * (channel.vibratoDepth << 3);
            channel.vibratoPhase += (channel.vibrato & 0xF0);
        } else {
            channel.vibratoAmigaPeriodAdd = 0;
        }

        // calculate tick end
        channel.samplePositionAddTickEnd = XMModule.amigaPeriodToFrequency(channel.amigaPeriod + channel.autoVibratoAmigaPeriodAdd + channel.vibratoAmigaPeriodAdd) / (float)this.sampleRate;
    }

    /**
     * Helper function to convert an Amiga-period number to Hz frequency
     * @param period the amige period
     * @return the calculated Hz, SI samples/second
     */
    public static float amigaPeriodToFrequency(float period)
    {
        return (float)(8363.0 * Math.pow(2, 6.0 - period / 768.0));
    }

    /**
     *  Helper function to convert note+finetune values to an Amiga-period
     * @param relativeNote the relative note offset (-12 = one octave lower)
     * @param note the note number
     * @param fineTune the fine tune
     * @return the amiga period
     */
    public static int noteAndFineTuneToAmigaPeriod(int relativeNote, int note, int fineTune)
    {
        return 7680 - (relativeNote + note - 1) * 64 - (fineTune >> 1);
    }

    private static byte[] xmHeader = new byte[] {
            0x45, 0x78, 0x74, 0x65, 0x6e, 0x64, 0x65, 0x64, 0x20, 0x4d, 0x6f, 0x64, 0x75, 0x6c, 0x65, 0x3a, 0x20
    };

    private static int readUnsignedShort(int position, byte[] data)
    {
        int firstByte = (data[position++] & 0xFF);
        int secondByte = (data[position] & 0xFF);
        return ((secondByte << 8) | (firstByte));
    }

    private static short readShort(int position, byte[] data)
    {
        int firstByte = (data[position++] & 0xFF);
        int secondByte = (data[position]);
        return (short)((secondByte << 8) | (firstByte));
    }

    private static int readUnsignedInt(int position, byte[] data)
    {
        int firstByte = (data[position++] & 0xFF);
        int secondByte = (data[position++] & 0xFF);
        int thirdByte = (data[position++] & 0xFF);
        int fourthByte = (data[position] & 0xFF);
        return ((fourthByte << 24) | (thirdByte << 16) | (secondByte << 8) | (firstByte));
    }

    /**
     * Loads a 1.04 FT2 XM file from the given array. Returns false if the module could not be loaded/parsed.
     * @param data the binary data representation of the .XM file to load
     * @return true = module was succesfully parsed and loaded, false = an error occurred
     */
    public boolean loadXM(byte[] data)
    {
        // check header
        int dataPosition = 0;
        for (int i=0;i<17;i++) {
            if (data[dataPosition++] != xmHeader[i]) return false;
        }

        // check version
        dataPosition = 58;
        if (XMModule.readUnsignedShort(dataPosition, data) != 0x104) return false;
        dataPosition += 2;

        // get header size
        int headerStartPosition = dataPosition;
        int headerSize = XMModule.readUnsignedInt(dataPosition, data);
        dataPosition += 4;

        // get song length
        this.songLength = XMModule.readUnsignedShort(dataPosition, data);
        if (this.songLength > 255) return false;
        dataPosition += 2;

        // get restart position
        this.restartPosition = XMModule.readUnsignedShort(dataPosition, data);
        dataPosition += 2;

        // get number of channels and create channels
        this.numberOfChannels = XMModule.readUnsignedShort(dataPosition, data);
        if (this.numberOfChannels > XMModule.MAX_CHANNELS) return false;
        dataPosition += 2;

        this.channels = new XMChannel[this.numberOfChannels];
        for (int c=0;c<this.numberOfChannels;c++) {
            this.channels[c] = new XMChannel();
        }

        // get number of patterns and reset patterns array
        this.numberOfPatterns = XMModule.readUnsignedShort(dataPosition, data);
        if (this.numberOfPatterns > 255) return false;
        dataPosition += 2;
        this.patterns = new XMPattern[this.numberOfPatterns];

        // get number of instruments, create instruments array and add dummy instrument
        int numberOfInstruments = XMModule.readUnsignedShort(dataPosition, data);
        dataPosition += 2;
        this.numberOfInstruments = numberOfInstruments + 1;
        this.instruments = new XMInstrument[this.numberOfInstruments];
        this.instruments[0] = new XMInstrument();

        // get song flags and tempo
        int flags = XMModule.readUnsignedShort(dataPosition, data);
        dataPosition += 2;
        this.linearFrequencyTable = ((flags & 0x01) == 0x01);
        this.defaultTempo = XMModule.readUnsignedShort(dataPosition, data);
        dataPosition += 2;
        this.defaultBPM = XMModule.readUnsignedShort(dataPosition, data);
        dataPosition += 2;

        // get pattern order
        this.patternOrder = new int[this.songLength];
        for (int i=0;i<this.songLength;i++) {
            this.patternOrder[i] = (data[dataPosition++] & 0xFF);
        }

        // skip rest of header
        dataPosition = headerStartPosition + headerSize;

        // read patterns
        XMNote newNote = new XMNote();
        for (int i=0;i<this.numberOfPatterns;i++)
        {
            int patternHeaderLength = XMModule.readUnsignedInt(dataPosition, data);
            dataPosition += 4;
            int patternPackingType = (data[dataPosition++] & 0xFF);
            int numberOfRows = XMModule.readUnsignedShort(dataPosition, data);
            dataPosition += 2;
            int patternPackedSize = XMModule.readUnsignedShort(dataPosition, data);
            dataPosition += 2;

            XMPattern newPattern;
            if (patternPackedSize == 0) {
                newPattern = new XMPattern(64, this.numberOfChannels);
            } else {
                newPattern = new XMPattern(numberOfRows, this.numberOfChannels);

                // read packed pattern data
                int currentChannel = 0;
                int currentRow = 0;

                while (patternPackedSize > 0) {
                    newNote.reset();
                    int flag = (data[dataPosition++] & 0xFF);
                    patternPackedSize--;

                    if ((flag & 0x80) == 0x80) {
                        if ((flag & 0x01) == 0x01) {
                            newNote.note = (data[dataPosition++] & 0xFF);
                            patternPackedSize--;
                        }
                        if ((flag & 0x02) == 0x02) {
                            newNote.instrument = (data[dataPosition++] & 0xFF);
                            patternPackedSize--;
                        }
                        if ((flag & 0x04) == 0x04) {
                            newNote.volume = (data[dataPosition++] & 0xFF);
                            patternPackedSize--;
                        }
                        if ((flag & 0x08) == 0x08) {
                            newNote.effectType = (data[dataPosition++] & 0xFF);
                            patternPackedSize--;
                        }
                        if ((flag & 0x10) == 0x10) {
                            newNote.effectParameter = (data[dataPosition++] & 0xFF);
                            patternPackedSize--;
                        }
                    } else {
                        newNote.note = flag;
                        newNote.instrument = (data[dataPosition++] & 0xFF);
                        newNote.volume = (data[dataPosition++] & 0xFF);
                        newNote.effectType = (data[dataPosition++] & 0xFF);
                        newNote.effectParameter = (data[dataPosition++] & 0xFF);
                        patternPackedSize -= 4;
                    }

                    newPattern.setNote(newNote, currentChannel, currentRow);

                    currentChannel++;
                    if (currentChannel >= this.numberOfChannels) {
                        currentChannel = 0;
                        currentRow++;
                    }
                } // while (patternPackedSize > 0)
            }

            // add new pattern
            this.patterns[i] = newPattern;
        }

        // read instruments
        for (int i=0;i<numberOfInstruments;i++)
        {
            XMInstrument newInstrument = new XMInstrument();

            int instrumentHeaderStart = dataPosition;
            int instrumentSize = XMModule.readUnsignedInt(dataPosition, data);
            dataPosition += 4;
            dataPosition += 23;
            int instrumentNumberOfSamples = XMModule.readUnsignedShort(dataPosition, data);
            dataPosition += 2;

            if (instrumentNumberOfSamples <= 0) {
                // this instrument doesn't have any samples, create a silent dummy sample and add it to the instrument
                XMSample newSample = new XMSample();
                newSample.fixForInterpolation(false);
                newInstrument.numberOfSamples = 1;
                newInstrument.samples = new XMSample[1];

                // skip to next sample
                dataPosition = instrumentHeaderStart + instrumentSize;

                // save sample
                newInstrument.samples[0] = newSample;
            } else {
                // this instrument has samples
                int sampleHeaderSize = XMModule.readUnsignedInt(dataPosition, data);
                dataPosition += 4;

                // read key mappings
                //newInstrument.noteToSampleIndex = new int[XMModule.NUMBER_OF_NOTES];
                for(int k=0;k<XMModule.NUMBER_OF_NOTES;k++) {
                    newInstrument.noteToSampleIndex[k] = (data[dataPosition++] & 0xFF);
                }

                // read instrument volume point pairs
                //newInstrument.volumeEnvelopeTicks = new int[XMInstrument.ENVELOPE_MAXIMUM_POINTS];
                //newInstrument.volumeEnvelopeValues = new int[XMInstrument.ENVELOPE_MAXIMUM_POINTS];
                for (int v=0;v<XMInstrument.ENVELOPE_MAXIMUM_POINTS;v++) {
                    newInstrument.volumeEnvelopeTicks[v] = XMModule.readUnsignedShort(dataPosition, data);
                    dataPosition += 2;
                    newInstrument.volumeEnvelopeValues[v] = XMModule.readUnsignedShort(dataPosition, data);
                    dataPosition += 2;
                }
                // read instrument panning point pairs
                //newInstrument.panningEnvelopeTicks = new int[XMInstrument.ENVELOPE_MAXIMUM_POINTS];
                //newInstrument.panningEnvelopeValues = new int[XMInstrument.ENVELOPE_MAXIMUM_POINTS];
                for (int v=0;v<XMInstrument.ENVELOPE_MAXIMUM_POINTS;v++) {
                    newInstrument.panningEnvelopeTicks[v] = XMModule.readUnsignedShort(dataPosition, data);
                    dataPosition += 2;
                    newInstrument.panningEnvelopeValues[v] = XMModule.readUnsignedShort(dataPosition, data);
                    dataPosition += 2;
                }

                // get instrument envelope loop and sustain point data
                newInstrument.volumeEnvelopeNumberOfPoints = (data[dataPosition++] & 0xFF);
                newInstrument.panningEnvelopeNumberOfPoints = (data[dataPosition++] & 0xFF);
                newInstrument.volumeEnvelopeSustainPointIndex = (data[dataPosition++] & 0xFF);
                newInstrument.volumeEnvelopeLoopStartPointIndex = (data[dataPosition++] & 0xFF);
                newInstrument.volumeEnvelopeLoopEndPointIndex = (data[dataPosition++] & 0xFF);
                newInstrument.panningEnvelopeSustainPointIndex = (data[dataPosition++] & 0xFF);
                newInstrument.panningEnvelopeLoopStartPointIndex = (data[dataPosition++] & 0xFF);
                newInstrument.panningEnvelopeLoopEndPointIndex = (data[dataPosition++] & 0xFF);

                // get instrument volume envelope settings
                int volumeEnvelopeFlags = (data[dataPosition++] & 0xFF);
                newInstrument.volumeEnvelopeEnabled = ((volumeEnvelopeFlags & 0x01) == 0x01);
                newInstrument.volumeEnvelopeSustainEnabled = ((volumeEnvelopeFlags & 0x02) == 0x02);
                newInstrument.volumeEnvelopeLoopEnabled = ((volumeEnvelopeFlags & 0x04) == 0x04);
                if (newInstrument.volumeEnvelopeLoopStartPointIndex >= newInstrument.volumeEnvelopeLoopEndPointIndex) {
                    newInstrument.volumeEnvelopeLoopEnabled = false;
                }

                // get instrument panning envelope settings
                int panningEnvelopeFlags = (data[dataPosition++] & 0xFF);
                newInstrument.panningEnvelopeEnabled = ((panningEnvelopeFlags & 0x01) == 0x01);
                newInstrument.panningEnvelopeSustainEnabled = ((panningEnvelopeFlags & 0x02) == 0x02);
                newInstrument.panningEnvelopeLoopEnabled = ((panningEnvelopeFlags & 0x04) == 0x04);
                if (newInstrument.panningEnvelopeLoopStartPointIndex >= newInstrument.panningEnvelopeLoopEndPointIndex) {
                    newInstrument.panningEnvelopeLoopEnabled = false;
                }

                // get instrument vibrato data
                newInstrument.vibratoType = (data[dataPosition++] & 0xFF);
                newInstrument.vibratoSweep = (data[dataPosition++] & 0xFF);
                newInstrument.vibratoDepth = (data[dataPosition++] & 0xFF);
                newInstrument.vibratoRate = (data[dataPosition++] & 0xFF);

                // get instrument fadeout
                newInstrument.fadeout = XMModule.readUnsignedShort(dataPosition, data) << 1;
                dataPosition += 2;

                // skip reserved bytes
                dataPosition += 22;

                // allocate samples
                newInstrument.numberOfSamples = instrumentNumberOfSamples;
                newInstrument.samples = new XMSample[newInstrument.numberOfSamples];

                // read instrument sample headers
                int[] sampleNumberOfBytes = new int[instrumentNumberOfSamples];
                int[] sampleLoopStart = new int[instrumentNumberOfSamples];
                int[] sampleLoopLength = new int[instrumentNumberOfSamples];
                int[] sampleFlags = new int[instrumentNumberOfSamples];
                for (int s=0;s<instrumentNumberOfSamples;s++) {
                    XMSample newSample = new XMSample();

                    sampleNumberOfBytes[s] = XMModule.readUnsignedInt(dataPosition, data);
                    dataPosition += 4;
                    sampleLoopStart[s] = XMModule.readUnsignedInt(dataPosition, data);
                    dataPosition += 4;
                    sampleLoopLength[s] = XMModule.readUnsignedInt(dataPosition, data);
                    dataPosition += 4;

                    newSample.volume = (data[dataPosition++] & 0xFF);
                    newSample.finetune = (data[dataPosition++]); // signed byte
                    sampleFlags[s] = (data[dataPosition++] & 0xFF);
                    newSample.panning = (data[dataPosition++] & 0xFF);
                    newSample.relativeNote = (data[dataPosition++]); // signed byte

                    // skip name and reserved
                    dataPosition += 23;

                    // save sample
                    newInstrument.samples[s] = newSample;
                }

                // read sample data
                for (int s=0;s<instrumentNumberOfSamples;s++) {
                    // read delta sample data
                    if ((sampleFlags[s] & 0x10) == 0x10)
                    {
                        // 16-bit sample delta
                        newInstrument.samples[s].sampleLoopStartPosition = sampleLoopStart[s] >> 1;
                        newInstrument.samples[s].sampleLoopEndPosition = ((sampleLoopStart[s] + sampleLoopLength[s]) >> 1);
                        newInstrument.samples[s].dataFPLength = sampleNumberOfBytes[s] >> 1;
                        newInstrument.samples[s].dataFP = new int[newInstrument.samples[s].dataFPLength];
                        int oldS = 0;
                        int si = 0;
                        while (sampleNumberOfBytes[s] > 0)
                        {
                            int newS = ((XMModule.readShort(dataPosition, data) + oldS));
                            dataPosition += 2;
                            if (newS < -32768) newS += 65536;
                            if (newS > 32767) newS -= 65536;
                            oldS = newS;
                            newInstrument.samples[s].dataFP[si] = (FixedPoint.FLOAT_TO_FP((newS << 1) / 32768.0f));
                            sampleNumberOfBytes[s] -= 2;
                            si++;
                        }
                    }
                    else
                    {
                        // 8-bit sample delta
                        newInstrument.samples[s].sampleLoopStartPosition = sampleLoopStart[s];
                        newInstrument.samples[s].sampleLoopEndPosition = (sampleLoopStart[s] + sampleLoopLength[s]);
                        newInstrument.samples[s].dataFPLength = sampleNumberOfBytes[s];
                        newInstrument.samples[s].dataFP = new int[newInstrument.samples[s].dataFPLength];
                        int oldS = 0;
                        int si = 0;
                        while (sampleNumberOfBytes[s] > 0)
                        {
                            int newS = ((data[dataPosition++] + oldS));
                            if (newS < -128) newS += 256;
                            if (newS > 127) newS -= 256;
                            oldS = newS;
                            newInstrument.samples[s].dataFP[si] = (FixedPoint.FLOAT_TO_FP(((newS << 9) / 32768.0f)));
                            sampleNumberOfBytes[s] -= 1;
                            si++;
                        }
                    }

                    // fix loop points and interpolation
                    if (((sampleFlags[s] & 0x0F) == 0x00) || (sampleLoopLength[s] == 0x00)) {
                        // this sample should not loop
                        newInstrument.samples[s].sampleLoopStartPosition = newInstrument.samples[s].dataFPLength;
                        newInstrument.samples[s].sampleLoopEndPosition = newInstrument.samples[s].sampleLoopStartPosition;
                    }
                    newInstrument.samples[s].fixForInterpolation(((sampleFlags[s] & 0x03) == 0x02));
                }

            } // instrumentNumberOfSamples > 0

            // finally, add instrument
            this.instruments[i + 1] = newInstrument;
        }

        this.restart();

        // calculate mix factor
        if (this.mixFactor < 0) {
            this.mixFactorFP = FixedPoint.FLOAT_TO_FP((float)Math.sqrt((1.0 / (double)this.channels.length)));
        } else if (this.mixFactor == 0) {
            this.mixFactorFP = FixedPoint.FLOAT_TO_FP((float)(1.0 / (double)this.channels.length));
        } else {
            this.mixFactorFP = FixedPoint.FLOAT_TO_FP(this.mixFactor);
        }

        return true;
    }
}
