// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.core.model.implementation;

import de.mossgrabers.convertwithmoss.core.model.IAudioMetadata;
import de.mossgrabers.convertwithmoss.core.model.ISampleData;

import java.io.IOException;


/**
 * Sample data base class.
 *
 * @author Jürgen Moßgraber
 */
public abstract class AbstractSampleData implements ISampleData
{
    protected IAudioMetadata audioMetadata;


    /** {@inheritDoc} */
    @Override
    public IAudioMetadata getAudioMetadata () throws IOException
    {
        if (this.audioMetadata == null)
            this.createAudioMetadata ();
        return this.audioMetadata;
    }


    /**
     * Create the audio metadata object.
     *
     * @throws IOException Could not read the audio metadata
     */
    protected abstract void createAudioMetadata () throws IOException;
}
