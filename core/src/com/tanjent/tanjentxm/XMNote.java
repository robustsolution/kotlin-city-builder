/* Licensed under The MIT License (MIT), see license.txt*/
package com.tanjent.tanjentxm;

/** <p>
 * Holds a Note entry of an {@link XMPattern}
 * </p>
 * @author Jonas Murman */
public class XMNote {
    public int note;
    public int instrument;
    public int volume;
    public int effectType;
    public int effectParameter;

    public XMNote()
    {
        this.reset();
    }

    /** Resets note to default values. */
    public void reset()
    {
        this.note = 0;
        this.instrument = 0;
        this.volume = 0;
        this.effectType = 0;
        this.effectParameter = 0;
    }
}
