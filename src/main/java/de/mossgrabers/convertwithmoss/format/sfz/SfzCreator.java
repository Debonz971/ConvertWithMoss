// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.sfz;

import de.mossgrabers.convertwithmoss.core.IMultisampleSource;
import de.mossgrabers.convertwithmoss.core.INotifier;
import de.mossgrabers.convertwithmoss.core.Utils;
import de.mossgrabers.convertwithmoss.core.creator.AbstractCreator;
import de.mossgrabers.convertwithmoss.core.model.IEnvelope;
import de.mossgrabers.convertwithmoss.core.model.IFilter;
import de.mossgrabers.convertwithmoss.core.model.IGroup;
import de.mossgrabers.convertwithmoss.core.model.IMetadata;
import de.mossgrabers.convertwithmoss.core.model.IModulator;
import de.mossgrabers.convertwithmoss.core.model.ISampleLoop;
import de.mossgrabers.convertwithmoss.core.model.ISampleZone;
import de.mossgrabers.convertwithmoss.core.model.enumeration.FilterType;
import de.mossgrabers.convertwithmoss.core.model.enumeration.LoopType;
import de.mossgrabers.convertwithmoss.core.model.enumeration.PlayLogic;
import de.mossgrabers.convertwithmoss.core.model.enumeration.TriggerType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


/**
 * Creator for SFZ multi-sample files. SFZ has a description file and all related samples in a
 * separate folder.
 *
 * @author Jürgen Moßgraber
 */
public class SfzCreator extends AbstractCreator
{
    private static final char                    LINE_FEED       = '\n';
    private static final String                  FOLDER_POSTFIX  = " Samples";
    private static final String                  SFZ_HEADER      = """
            /////////////////////////////////////////////////////////////////////////////
            ////
            """;
    private static final String                  COMMENT_PREFIX  = "//// ";

    private static final Map<FilterType, String> FILTER_TYPE_MAP = new EnumMap<> (FilterType.class);
    private static final Map<LoopType, String>   LOOP_TYPE_MAP   = new EnumMap<> (LoopType.class);

    static
    {
        FILTER_TYPE_MAP.put (FilterType.LOW_PASS, "lpf");
        FILTER_TYPE_MAP.put (FilterType.HIGH_PASS, "hpf");
        FILTER_TYPE_MAP.put (FilterType.BAND_PASS, "bpf");
        FILTER_TYPE_MAP.put (FilterType.BAND_REJECTION, "brf");

        LOOP_TYPE_MAP.put (LoopType.FORWARD, "forward");
        LOOP_TYPE_MAP.put (LoopType.BACKWARDS, "backward");
        LOOP_TYPE_MAP.put (LoopType.ALTERNATING, "alternate");
    }


    /**
     * Constructor.
     *
     * @param notifier The notifier
     */
    public SfzCreator (final INotifier notifier)
    {
        super ("SFZ", notifier);
    }


    /** {@inheritDoc} */
    @Override
    public void create (final File destinationFolder, final IMultisampleSource multisampleSource) throws IOException
    {
        final String sampleName = createSafeFilename (multisampleSource.getName ());
        final File multiFile = new File (destinationFolder, sampleName + ".sfz");
        if (multiFile.exists ())
        {
            this.notifier.logError ("IDS_NOTIFY_ALREADY_EXISTS", multiFile.getAbsolutePath ());
            return;
        }

        final String safeSampleFolderName = sampleName + FOLDER_POSTFIX;
        final String metadata = this.createMetadata (safeSampleFolderName, multisampleSource);

        this.notifier.log ("IDS_NOTIFY_STORING", multiFile.getAbsolutePath ());

        try (final FileWriter writer = new FileWriter (multiFile, StandardCharsets.UTF_8))
        {
            writer.write (metadata);
        }

        // Store all samples
        final File sampleFolder = new File (destinationFolder, safeSampleFolderName);
        safeCreateDirectory (sampleFolder);
        this.writeSamples (sampleFolder, multisampleSource);

        this.notifier.log ("IDS_NOTIFY_PROGRESS_DONE");
    }


    /**
     * Create the text of the description file.
     *
     * @param safeSampleFolderName The safe sample folder name (removed illegal characters)
     * @param multisampleSource The multi-sample
     * @return The XML structure
     */
    private String createMetadata (final String safeSampleFolderName, final IMultisampleSource multisampleSource)
    {
        final StringBuilder sb = new StringBuilder (SFZ_HEADER);

        // Metadata (category, creator, keywords) is currently not available in the
        // specification but has a suggestion: https://github.com/sfz/opcode-suggestions/issues/19
        // until then add it as a comment
        final IMetadata metadata = multisampleSource.getMetadata ();
        final String creator = metadata.getCreator ();
        if (creator != null && !creator.isBlank ())
            sb.append (COMMENT_PREFIX).append ("Creator : ").append (creator).append (LINE_FEED);
        final String category = metadata.getCategory ();
        if (category != null && !category.isBlank ())
            sb.append (COMMENT_PREFIX).append ("Category: ").append (category).append (LINE_FEED);
        final String description = metadata.getDescription ();
        if (description != null && !description.isBlank ())
            sb.append (COMMENT_PREFIX).append (description.replace ("\n", "\n" + COMMENT_PREFIX)).append (LINE_FEED);
        sb.append (LINE_FEED);

        final String name = multisampleSource.getName ();

        sb.append ('<').append (SfzHeader.GLOBAL).append (">").append (LINE_FEED);
        if (name != null && !name.isBlank ())
            addAttribute (sb, SfzOpcode.GLOBAL_LABEL, name, true);

        for (final IGroup group: multisampleSource.getGroups ())
        {
            final List<ISampleZone> zones = group.getSampleZones ();
            if (zones.isEmpty ())
                continue;

            // Check for any sample which play round-robin
            int sequence = 0;
            for (final ISampleZone zone: zones)
            {
                if (zone.getPlayLogic () == PlayLogic.ROUND_ROBIN)
                    sequence++;
            }

            sb.append (LINE_FEED).append ('<').append (SfzHeader.GROUP).append (">").append (LINE_FEED);
            final String groupName = group.getName ();
            if (groupName != null && !groupName.isBlank ())
                addAttribute (sb, SfzOpcode.GROUP_LABEL, groupName, true);
            if (sequence > 0)
                addIntegerAttribute (sb, SfzOpcode.SEQ_LENGTH, sequence, true);

            final TriggerType trigger = group.getTrigger ();
            if (trigger != null && trigger != TriggerType.ATTACK)
                addAttribute (sb, SfzOpcode.TRIGGER, trigger.name ().toLowerCase (Locale.ENGLISH), true);

            sequence = 1;
            for (final ISampleZone zone: zones)
            {
                this.createSample (safeSampleFolderName, sb, zone, sequence);
                if (zone.getPlayLogic () == PlayLogic.ROUND_ROBIN)
                    sequence++;
            }
        }

        return sb.toString ();
    }


    /**
     * Creates the metadata for one sample.
     *
     * @param safeSampleFolderName The safe sample folder name
     * @param buffer Where to add the XML code
     * @param zone The sample zone
     * @param sequenceNumber The number in the sequence for round-robin playback
     */
    private void createSample (final String safeSampleFolderName, final StringBuilder buffer, final ISampleZone zone, final int sequenceNumber)
    {
        buffer.append ("\n<").append (SfzHeader.REGION).append (">\n");
        addAttribute (buffer, SfzOpcode.SAMPLE, AbstractCreator.formatFileName (safeSampleFolderName, zone.getName () + ".wav"), true);

        // Default is 'attack' and does not need to be added
        final TriggerType trigger = zone.getTrigger ();
        if (trigger != TriggerType.ATTACK)
            addAttribute (buffer, SfzOpcode.TRIGGER, trigger.name ().toLowerCase (Locale.ENGLISH), true);

        if (zone.isReversed ())
            addAttribute (buffer, SfzOpcode.DIRECTION, "reverse", true);
        if (zone.getPlayLogic () == PlayLogic.ROUND_ROBIN)
            addIntegerAttribute (buffer, SfzOpcode.SEQ_POSITION, sequenceNumber, true);

        ////////////////////////////////////////////////////////////
        // Key range

        final int keyRoot = zone.getKeyRoot ();
        final int keyLow = zone.getKeyLow ();
        final int keyHigh = zone.getKeyHigh ();
        if (keyRoot == keyLow && keyLow == keyHigh)
        {
            // Pitch and range are the same, use single key attribute
            addIntegerAttribute (buffer, SfzOpcode.KEY, keyRoot, true);
        }
        else
        {
            addIntegerAttribute (buffer, SfzOpcode.PITCH_KEY_CENTER, keyRoot, true);
            addIntegerAttribute (buffer, SfzOpcode.LO_KEY, check (keyLow, 0), false);
            addIntegerAttribute (buffer, SfzOpcode.HI_KEY, check (keyHigh, 127), true);
        }

        final int crossfadeLow = zone.getNoteCrossfadeLow ();
        if (crossfadeLow > 0)
        {
            addIntegerAttribute (buffer, SfzOpcode.XF_IN_LO_KEY, Math.max (0, keyLow - crossfadeLow), false);
            addIntegerAttribute (buffer, SfzOpcode.XF_IN_HI_KEY, keyLow, true);
        }
        final int crossfadeHigh = zone.getNoteCrossfadeHigh ();
        if (crossfadeHigh > 0)
        {
            addIntegerAttribute (buffer, SfzOpcode.XF_OUT_LO_KEY, keyHigh, false);
            addIntegerAttribute (buffer, SfzOpcode.XF_OUT_HI_KEY, Math.min (127, keyHigh + crossfadeHigh), true);
        }

        ////////////////////////////////////////////////////////////
        // Velocity

        final int velocityLow = zone.getVelocityLow ();
        final int velocityHigh = zone.getVelocityHigh ();
        if (velocityLow > 1)
            addIntegerAttribute (buffer, SfzOpcode.LO_VEL, velocityLow, velocityHigh == 127);
        if (velocityHigh > 0 && velocityHigh < 127)
            addIntegerAttribute (buffer, SfzOpcode.HI_VEL, velocityHigh, true);

        final int crossfadeVelocityLow = zone.getVelocityCrossfadeLow ();
        if (crossfadeVelocityLow > 0)
        {
            addIntegerAttribute (buffer, SfzOpcode.XF_IN_LO_VEL, Math.max (0, velocityLow - crossfadeVelocityLow), false);
            addIntegerAttribute (buffer, SfzOpcode.XF_IN_HI_VEL, velocityLow, true);
        }

        final int crossfadeVelocityHigh = zone.getVelocityCrossfadeHigh ();
        if (crossfadeVelocityHigh > 0)
        {
            addIntegerAttribute (buffer, SfzOpcode.XF_OUT_LO_VEL, velocityHigh, false);
            addIntegerAttribute (buffer, SfzOpcode.XF_OUT_HI_VEL, Math.min (127, velocityHigh + crossfadeVelocityHigh), true);
        }

        ////////////////////////////////////////////////////////////
        // Start, end, tune, volume

        final int start = zone.getStart ();
        if (start >= 0)

            addIntegerAttribute (buffer, SfzOpcode.OFFSET, start, false);
        final int end = zone.getStop ();
        if (end >= 0)
            addIntegerAttribute (buffer, SfzOpcode.END, end, true);

        final double tune = zone.getTune ();
        if (tune != 0)
            addIntegerAttribute (buffer, SfzOpcode.TUNE, (int) Math.round (tune * 100), true);

        final int keyTracking = (int) Math.round (zone.getKeyTracking () * 100.0);
        if (keyTracking != 100)
            addIntegerAttribute (buffer, SfzOpcode.PITCH_KEYTRACK, keyTracking, true);

        createVolume (buffer, zone);

        ////////////////////////////////////////////////////////////
        // Pitch Bend / Envelope

        final int bendUp = zone.getBendUp ();
        if (bendUp != 0)
            addIntegerAttribute (buffer, SfzOpcode.BEND_UP, bendUp, true);
        final int bendDown = zone.getBendDown ();
        if (bendDown != 0)
            addIntegerAttribute (buffer, SfzOpcode.BEND_DOWN, bendDown, true);

        final StringBuilder envelopeStr = new StringBuilder ();

        final IModulator pitchModulator = zone.getPitchModulator ();
        final double envelopeDepth = pitchModulator.getDepth ();
        if (envelopeDepth > 0)
        {
            buffer.append (SfzOpcode.PITCHEG_DEPTH).append ('=').append ((int) envelopeDepth).append (LINE_FEED);

            final IEnvelope pitchEnvelope = pitchModulator.getSource ();

            addEnvelopeAttribute (envelopeStr, SfzOpcode.PITCHEG_DELAY, pitchEnvelope.getDelay ());
            addEnvelopeAttribute (envelopeStr, SfzOpcode.PITCHEG_ATTACK, pitchEnvelope.getAttack ());
            addEnvelopeAttribute (envelopeStr, SfzOpcode.PITCHEG_HOLD, pitchEnvelope.getHold ());
            addEnvelopeAttribute (envelopeStr, SfzOpcode.PITCHEG_DECAY, pitchEnvelope.getDecay ());
            addEnvelopeAttribute (envelopeStr, SfzOpcode.PITCHEG_RELEASE, pitchEnvelope.getRelease ());

            addEnvelopeAttribute (envelopeStr, SfzOpcode.PITCHEG_START, pitchEnvelope.getStart () * 100.0);
            addEnvelopeAttribute (envelopeStr, SfzOpcode.PITCHEG_SUSTAIN, pitchEnvelope.getSustain () * 100.0);

            if (envelopeStr.length () > 0)
                buffer.append (envelopeStr).append (LINE_FEED);
        }

        ////////////////////////////////////////////////////////////
        // Sample Loop

        this.createLoops (buffer, zone);

        ////////////////////////////////////////////////////////////
        // Filter

        createFilter (buffer, zone);
    }


    /**
     * Create the loop info.
     *
     * @param buffer Where to add the XML code
     * @param zone The sample zone
     */
    private void createLoops (final StringBuilder buffer, final ISampleZone zone)
    {
        final List<ISampleLoop> loops = zone.getLoops ();
        if (loops.isEmpty ())
        {
            addAttribute (buffer, SfzOpcode.LOOP_MODE, "no_loop", false);
        }
        else
        {
            final ISampleLoop sampleLoop = loops.get (0);
            // SFZ currently only supports forward looping
            addAttribute (buffer, SfzOpcode.LOOP_MODE, "loop_continuous", false);
            final String type = LOOP_TYPE_MAP.get (sampleLoop.getType ());
            // No need to write the default value
            if (!"forward".equals (type))
                addAttribute (buffer, SfzOpcode.LOOP_TYPE, type, false);
            addIntegerAttribute (buffer, SfzOpcode.LOOP_START, sampleLoop.getStart (), false);
            buffer.append (SfzOpcode.LOOP_END).append ('=').append (sampleLoop.getEnd ());

            // Calculate the crossfade in seconds from a percentage of the loop length
            final double crossfade = sampleLoop.getCrossfade ();
            if (crossfade > 0)
            {
                final int loopLength = sampleLoop.getStart () - sampleLoop.getEnd ();
                if (loopLength > 0)
                {
                    double loopLengthInSeconds;
                    try
                    {
                        loopLengthInSeconds = loopLength / (double) zone.getSampleData ().getAudioMetadata ().getSampleRate ();
                        final double crossfadeInSeconds = crossfade * loopLengthInSeconds;
                        buffer.append (' ').append (SfzOpcode.LOOP_CROSSFADE).append ('=').append (Math.round (crossfadeInSeconds));
                    }
                    catch (final IOException ex)
                    {
                        this.notifier.logError (ex);
                    }
                }
            }
        }
        buffer.append (LINE_FEED);
    }


    /**
     * Create the volume and amplitude envelope parameters.
     *
     * @param buffer Where to add the created text
     * @param zone The sample zone
     */
    private static void createVolume (final StringBuilder buffer, final ISampleZone zone)
    {
        final double volume = zone.getGain ();
        if (volume != 0)
            addAttribute (buffer, SfzOpcode.VOLUME, formatDouble (volume, 2), true);
        final double pan = zone.getPanorama ();
        if (pan != 0)
            addAttribute (buffer, SfzOpcode.PANORAMA, Integer.toString ((int) Math.round (pan * 100)), true);

        final StringBuilder envelopeStr = new StringBuilder ();

        final IEnvelope amplitudeEnvelope = zone.getAmplitudeModulator ().getSource ();

        addEnvelopeAttribute (envelopeStr, SfzOpcode.AMPEG_DELAY, amplitudeEnvelope.getDelay ());
        addEnvelopeAttribute (envelopeStr, SfzOpcode.AMPEG_ATTACK, amplitudeEnvelope.getAttack ());
        addEnvelopeAttribute (envelopeStr, SfzOpcode.AMPEG_HOLD, amplitudeEnvelope.getHold ());
        addEnvelopeAttribute (envelopeStr, SfzOpcode.AMPEG_DECAY, amplitudeEnvelope.getDecay ());
        addEnvelopeAttribute (envelopeStr, SfzOpcode.AMPEG_RELEASE, amplitudeEnvelope.getRelease ());

        addEnvelopeAttribute (envelopeStr, SfzOpcode.AMPEG_START, amplitudeEnvelope.getStart () * 100.0);
        addEnvelopeAttribute (envelopeStr, SfzOpcode.AMPEG_SUSTAIN, amplitudeEnvelope.getSustain () * 100.0);

        if (envelopeStr.length () > 0)
            buffer.append (envelopeStr).append (LINE_FEED);
    }


    /**
     * Create the filter info.
     *
     * @param buffer Where to add the XML code
     * @param zone The sample zone
     */
    private static void createFilter (final StringBuilder buffer, final ISampleZone zone)
    {
        final Optional<IFilter> optFilter = zone.getFilter ();
        if (optFilter.isEmpty ())
            return;

        final IFilter filter = optFilter.get ();
        final String type = FILTER_TYPE_MAP.get (filter.getType ());
        addAttribute (buffer, SfzOpcode.FILTER_TYPE, type + "_" + Utils.clamp (filter.getPoles (), 1, 4) + "p", false);
        addAttribute (buffer, SfzOpcode.CUTOFF, formatDouble (filter.getCutoff (), 2), false);
        addAttribute (buffer, SfzOpcode.RESONANCE, formatDouble (Math.min (40, filter.getResonance ()), 2), true);

        final StringBuilder envelopeStr = new StringBuilder ();

        final IModulator cutoffModulator = filter.getCutoffModulator ();
        final double envelopeDepth = cutoffModulator.getDepth ();
        if (envelopeDepth > 0)
        {
            buffer.append (SfzOpcode.FILEG_DEPTH).append ('=').append ((int) envelopeDepth).append (LINE_FEED);

            final IEnvelope filterEnvelope = cutoffModulator.getSource ();

            addEnvelopeAttribute (envelopeStr, SfzOpcode.FILEG_DELAY, filterEnvelope.getDelay ());
            addEnvelopeAttribute (envelopeStr, SfzOpcode.FILEG_ATTACK, filterEnvelope.getAttack ());
            addEnvelopeAttribute (envelopeStr, SfzOpcode.FILEG_HOLD, filterEnvelope.getHold ());
            addEnvelopeAttribute (envelopeStr, SfzOpcode.FILEG_DECAY, filterEnvelope.getDecay ());
            addEnvelopeAttribute (envelopeStr, SfzOpcode.FILEG_RELEASE, filterEnvelope.getRelease ());

            addEnvelopeAttribute (envelopeStr, SfzOpcode.FILEG_START, filterEnvelope.getStart () * 100.0);
            addEnvelopeAttribute (envelopeStr, SfzOpcode.FILEG_SUSTAIN, filterEnvelope.getSustain () * 100.0);

            if (envelopeStr.length () > 0)
                buffer.append (envelopeStr).append (LINE_FEED);
        }
    }


    private static void addAttribute (final StringBuilder sb, final String opcode, final String value, final boolean addLineFeed)
    {
        sb.append (opcode).append ('=').append (value).append (addLineFeed ? LINE_FEED : ' ');
    }


    private static void addIntegerAttribute (final StringBuilder sb, final String opcode, final int value, final boolean addLineFeed)
    {
        addAttribute (sb, opcode, Integer.toString (value), addLineFeed);
    }


    private static void addEnvelopeAttribute (final StringBuilder sb, final String opcode, final double value)
    {
        if (value < 0)
            return;
        if (sb.length () > 0)
            sb.append (' ');
        sb.append (opcode).append ('=').append (Utils.clamp (value, 0.0, 100.0));
    }
}