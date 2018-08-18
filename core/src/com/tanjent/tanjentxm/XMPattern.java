/* Licensed under The MIT License (MIT), see license.txt*/
package com.tanjent.tanjentxm;

/** <p>
 * Holds uncompressed pattern data of an {@link XMModule}. 
 * </p>
 * @author Jonas Murman */
public class XMPattern {
    public int numberOfRows;
    public int numberOfChannels;

    /// pattern data is stored in rows. All rows are 2 ... 32 channels of note data.
    /// patternData[0] ... patternData[4] = 5 bytes of note data, channel0, row0
    /// patternData[5] ... patternData[9] = 5 bytes of note data, channel1, row0
    /// ...
    /// patternData[5*numberOfChannels] ... patternData[5*numberOfChannels + 4] = 5 bytes of note data, channel0, row1
    public int[] patternData;

    /**
     * Creates a pattern with numberOfRows x numberOfChannels note entries.
     * @param numberOfRows the number of rows (1 ... 255)
     * @param numberOfChannels the number of channels, see {@link XMModule} MIN_CHANNELS and MAX_CHANNELS for current channel limits
     */
    public XMPattern(int numberOfRows, int numberOfChannels)
    {
        this.numberOfRows = numberOfRows;
        this.numberOfChannels = numberOfChannels;

        if (this.numberOfRows < 0) this.numberOfRows = 1;
        if (this.numberOfRows > 255) this.numberOfRows = 255;
        if (this.numberOfChannels < XMModule.MIN_CHANNELS) this.numberOfChannels = XMModule.MIN_CHANNELS;
        if (this.numberOfChannels > XMModule.MAX_CHANNELS) this.numberOfChannels = XMModule.MAX_CHANNELS;

        this.patternData = new int[numberOfChannels * numberOfRows * 5];
        for (int i=0;i<this.patternData.length;i++) {
            this.patternData[i] = 0;
        }
    }

    /**
     * Calculates the index of a note in the patternData array.
     * @param channel the channel number
     * @param row the row number
     * @return index of the note in the patternData array
     */
    private int indexOfNote(int channel, int row)
    {
        return (5 * this.numberOfChannels * row) + (channel * 5);
    }

    /**
     * Sets the pattern data at the channel and row to the {@link XMNote}.
     * @param note the note data to set
     * @param channel the channel number
     * @param row the row number
     */
    public void setNote(XMNote note, int channel, int row)
    {
        int index = indexOfNote(channel, row);

        this.patternData[index] = note.note;
        this.patternData[index + 1] = note.instrument;
        this.patternData[index + 2] = note.volume;
        this.patternData[index + 3] = note.effectType;
        this.patternData[index + 4] = note.effectParameter;
    }

    /**
     * Fills a {@link XMNote} from the pattern data at the channel and row
     * @param note the note to fill
     * @param channel the channel number
     * @param row the row number
     */
    public void fillNote(XMNote note, int channel, int row)
    {
        int index = indexOfNote(channel, row);

        note.note = this.patternData[index];
        note.instrument = this.patternData[index + 1];
        note.volume = this.patternData[index + 2];
        note.effectType = this.patternData[index + 3];
        note.effectParameter = this.patternData[index + 4];
    }
}
