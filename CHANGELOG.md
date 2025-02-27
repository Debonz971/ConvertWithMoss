# Changes

## 7.3.1 (unreleased)

* Fixed: Tab labels were not visible.

## 7.3.0

* Added support for TAL Sampler format (reading + writing).
* Improved user interface.
* Sf2 - Reading
  * Fixed: 24 and 16 bit detection were flipped and produces an exception.
* SFZ - Reading
  * New: AIFF files can be used as input.
* Kontakt - Reading
  * Fixed: Zone tuning was not set correctly.
  * Fixed: If a file was referenced more than once in a monolith, all of them had the same zone settings.
* Korg KMP - Reading
  * Fixed: Pitch tracking was inverted.

## 7.2.1

* DecentSampler - Writing
  * Fixed: Tuning was not set correctly
* Kontakt - Reading
  * New: Support for Kontakt 7.6.
  * Fixed: Kontakt 5-7: Sample zones from monolith files did miss all settings.
  * Fixed: Kontakt 5-7: Pitch was not handled correctly.

## 7.2.0

* Kontakt - Reading
  * New: Support for Kontakt 4.2 and 5-7 NKMs.
  * Improved: Detection of encryption.
  * Fixed: Improved Kontakt 5-7 file path reading and handling.

## 7.1.1

* Kontakt - Reading
  * Fixed: Regression from 7.1.0 - Kontakt 5-7 files could not be read at all.
  * Fixed: Kontakt 5-7 relative paths can contain redirections to parent directories which were not added.
  * Fixed: Support for Kontakt 2 files which contain an XML document with a leading UTF-BOM.

## 7.1.0

* Fixed: Loops could be incorrect if sample rate was not 44.1kHz and audio file metadata could be wrong as well in that case.
* Korg KMP/KSF
  * New: Convert source samples to support bit resolutions (8, 16) and maximum sample rate of 48kHz.
  * Fixed: Improved check for duplicated DOS file names and unique ones are now created.
* Kontakt - Reading
  * New: Kontakt 2-4 monoliths in big-endian encoding are now supported.
  * New: Added support for alternative Kontakt 1 file-ex sample path reference.
  * New: Added support for Kontakt 1.5 files.
  * Improved: Finding samples when absolute sample file paths are used.
  * Fixed: Fixed several issues with Kontakt 2-4 monoliths.
  * Fixed: NCW files with mid/side encoding were not handled correctly.

## 7.0.0

* '(Velocity) Layers' have been renamed to 'Groups' in the user interface.
* Fixed: Some issues with reading WAV files.
* MPC keygroups
  * Improved: Loop information is written to the WAV file which seems to be used by the MPC.
* Native Instruments NKI files - Reading
  * New: Conversion of Kontakt 4.2 - 7 files: metadata, zones, loops, NCW and monoliths files work but no support for envelopes and filters.
* Native Instruments NKI files - Writing
  * Fixed: Created Kontakt 1 files could be opened with Kontakt but not saved again due to the use of forward slashes for sample paths. Backward slashes are used now.
* Sf2 - Reading
  * New: Use filename (without ending) for instruments named 'NewInstr'.
  * Fixed: Panorama setting was not corrected when mono files were combined to stereo.
  * Fixed: If left and right sample had different lengths, the shorter sample had data from the following sample added.

## 6.3.0

* Default volume envelopes are applied based on the detected category if none is present.
* Decent Sampler
  * Fixed: Read: Wrong velocity range (0-0) when velocity settings were missing.
* MPC keygroups
  * Fixed: Read/Write: Improved mapping of envelopes.
  * Fixed: Write: Pitch was not correct.
* SFZ
  * Fixed: Increased allowed range of pitch values.
  * Fixed: Panorama was not read / written.

## 6.2.1

* Decent Sampler - Reading
  * New: Implemented workaround for invalid XML document (contains comments before XML header).
  * New: Added support for notes which are formatted as text instead of MIDI numbers.
  * Fixed: Groups were not detected.

## 6.2.0

* Added support for reading Native Instruments NKM files (Kontakt Multis) in Kontakt version 1-4.
* Native Instruments NKI files - Reading
  * For Kontakt 5+ NKI files the exact version number is displayed (but reading is still not supported).
* Native Instruments NKI files - Writing
  * New: Intensity of default envelopes is now set to 1 (was 0).
  * New: The default pitch envelope has now 0 for all parameters.
  * Fixed: Envelope hold and decay were flipped.

## 6.1.0

* Tabs are now ordered alphabetically.
* Bitwig Multisample
  * Fixed: If a loop was set to Off it was still applied.
* Native Instruments NKI files
  * New: Added support to write NKI files in Kontakt 1 format.
  * New: Added support for AIFF files (will be converted to WAV).
  * New: Added support for reading Kontakt NKI files stored in big-endian format. But could not test with any monolith file, therefore an error is shown.
  * New: Added support for pitch envelopes.
  * New: Added support for filter settings and cutoff envelope.
  * Fixed: High velocity crossover value did overwrite low velocity crossover.
* Korg KMP
  * Fixed: Extracting groups into single KMP files did overwrite the KSF sample files.

## 6.0.0

* New: Added option to rename multi-samples (thanks to Philip Stolz).
* New: Improved mapping of envelopes to MPC keygroups (thanks to Philip Stolz).
* New: Added support for reading Kontakt NKI files (only the format of the versions before Kontakt 4.2 are supported, thanks to Philip Stolz).
* Fixed: Added missing reading of panorama value.

## 5.2.1

* Fixed: Bitwig Multisample files with old layer formatting had duplicated layers as output.
* Fixed: Missing trigger types in Decent Sampler files did show an unnecessary error.

## 5.2

* New: Added support for trigger type (attack, release, first, legato) for SFZ, Decent Sampler, MPC Keygroups (only attack, release on instrument).

## 5.1

* New: WAV files are added as destination format e.g. in case you only want to extract WAV files from SF2 files.
* New: Store WAV ending in lower-case when converted from MPC Keygroups.
* Fixed: (Bitwig) Multisample files must not be compressed for faster access. Bitwig can also handle compressed files but other hosts supporting the format might fail. If you created Multisample files with this converter, simply run a new conversion on them with Multisample as source and destination to fix the issue.
* Fixed: Created (Bitwig) Multisample metadata file contained wrong group indices (off by 1).

## 5.0

* New: Added reading/writing of Korg KMP/KSF files.
* New: Added icons to the buttons.

## 4.7.1

* Fixed: Name detection was broken (if 'Prefer folder name' was off).
* Fixed: Akai XPM: Velocity range was not read correctly.

## 4.7

* New: WAV: Layer detection pattern fields are now checked to contain a '*'.
* Fixed: WAV: Having the layer detection pattern field empty led to undetectable MIDI notes.
* Fixed: WAV: The order of potential note names in file names could have been wrong and therefore a detection could fail.

## 4.6

* New: SF2, SFZ, MPC: Support for Pitch bend range settings.
* New: SF2, SFZ, Decent Sampler, MPC: Support for filter settings (incl. filter envelope).
* New: SF2, SFZ, MPC: Support for Pitch envelope settings.
* Fixed: SFZ: Logging of unsupported opcodes did add up.
* Fixed: SFZ: Sample paths in metadata now always use forward slash.
* Fixed: Decent Sampler: Sample files from dslibrary could not be written.
* Fixed: Decent Sampler: Tuning was not read correctly (off by factor 100).
* Fixed: Decent Sampler: Round-robin was not read and not written correctly.

## 4.5

* New: Support for amplitude envelope: Decent Sampler, MPC Keygroups, SFZ: read/write; SF2: read
* New: Decent Sampler: Support 'tuning' and 'groupTuning' on group tags as well as 'globalTuning' on the groups tag.
* New: SF2: Support initialAttenuation generator.
* Fixed: SF2: Sample files extracted from Sf2 were always set as 44.1kHz.
* Fixed: SFZ: Presets with illegal characters were corrected for the sample folder name but not in the SFZ file reference.
* Fixed: SFZ: Loop attributes were not read when loop_type was missing.
* Fixed: SFZ: Loop attribute alternative names loopstart, loopend were not read.
* Fixed: SFZ: Loop was not set to off when no loop was present.
* Fixed: MPC Keygroups: Loop end was not set correctly if different from sample end.
* Fixed: Decent Sampler: group name was wrongly reported as not supported.
* Fixed: WAV: Check of sample chunks when combining mono to stereo does now only require to have the same pitch.
* Fixed: Error message for left/right mono samples with different pitch was missing.

## 4.0

* New: Added reading/writing of Korg Wavestate (.korgmultisample) files.
* New: Added reading of Akai MPC Keygroup files.
* New: Added the WAV creator detector parameters to SFZ, Decent Sampler and MPC Keygroups as well.
* New: Added a dark mode.
* Fixed: WAV: Detection of root note from sample names could be wrong when multiple options apply and the last one was wrong.
* Fixed: SFZ: Ignore illegal characters in SFZ files.
* Fixed: Bitwig multisample: Key tune parameter was not stored correctly.

## 3.2

* New: Support WAV files in extensible format.
* New: SFZ: Create names for groups without a name.
* New: SFZ: Check for trigger opcode but only 'attack' is supported.
* Fixed: SFZ: Key values which did not use MIDI note numbers were not read (e.g. c#3).
* Fixed: Improved handling of large chunks in WAV files.
* Fixed: Fixed issues with sample paths created on different OS.
* Fixed: Fixed some issues with error message formatting.
* Fixed: Do not create the top source folder in the output folder (only the sub-folders).

## 3.1

* New: Akai MPC Keygroup - round-robin groups are now converted (up to 4).
* New: Akai MPC Keygroup - more than 4 groups can now be converted; this creates multiple keygroups.
* Fixed: Akai MPC Keygroup - root notes of samples were off by 1.

## 3.0

* New: Added writing of Akai MPC Keygroup files.

## 2.2.0

* New: DecentSampler creator got some options to choose which controls to create and to make the sound monophonic.
* Fixed: WAV detector: Upper group was not always 127.

## 2.1.1

* Fixed: WAV detector did not read loops from WAV files.

## 2.1

* Fixed: WAV detector did also deliver results for empty folders.
* Fixed: Setup for created DecentSampler Filter and Reverb is working now.

## 2.0

* New: Added reading and writing of DecentSampler preset and library files.
* New: Improved note detection from file names.
* Fixed: SFZ detector - global_label was not read.
* Fixed: SFZ parser - Comments at line end were not removed which conflicted with attribute values.
* Fixed: WAV detector - Crash if left and right mono sample had different lengths.
* Fixed: Creating folders for SFZ could raise an exception.
* Fixed: Source and destination tabs could be removed.
