package com.matthewn4444.ebml.subtitles;

import com.matthewn4444.ebml.Tracks;
import com.matthewn4444.ebml.elements.BlockElement;
import com.matthewn4444.ebml.elements.IntElement;
import com.matthewn4444.ebml.elements.MasterElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class Subtitles extends Tracks {
    public static final String TAG = "Subtitles";
    public static final String SSA_CODEC_ID = "S_TEXT/ASS";
    public static final String SRT_CODEC_ID = "S_TEXT/UTF8";
    public static final String PGS_CODEC_ID = "S_HDMV/PGS";

    @AllArgsConstructor
    public enum Type {
        SSA("ass"), SRT("srt"), PGS("pgs");

        @Getter
        private final String extName;
    }

    protected final boolean mIsCompressed;
    protected final Type mType;

    protected final ArrayList<Caption> mUnreadCaptions;
    protected final ArrayList<Caption> mReadCaptions;

    /**
     * Creates the subtitles class from a blackgroup of data read from a cluster entry
     * Internal use only
     *
     * @param blockgroup MasterElement containing cluster blockgroup information
     * @return a subtitles object containing all readable data
     * @throws UnsupportedEncodingException
     */
    static public Subtitles CreateSubsFromBlockGroup(MasterElement blockgroup, boolean hasCompression) throws UnsupportedEncodingException {
        if (blockgroup.getValueInt(Tracks.TYPE) == Tracks.Type.SUBTITLE) {
            int trackNumber = blockgroup.getValueInt(Tracks.NUMBER);
            IntElement enableEl = (IntElement) blockgroup.getElement(Tracks.IS_ENABLED);
            IntElement defaultEl = (IntElement) blockgroup.getElement(Tracks.IS_DEFAULT);
            boolean isEnabled = enableEl == null || enableEl.getData() == 1;
            boolean isDefault = defaultEl == null || defaultEl.getData() == 1;
            String name = blockgroup.getValueString(Tracks.NAME);
            String language = blockgroup.getValueString(Tracks.LANGUAGE);
            String codecID = blockgroup.getValueString(Tracks.CODEC_ID);
            switch (codecID) {
                case SSA_CODEC_ID -> {
                    return new SSASubtitles(trackNumber, blockgroup.getFilePosition(),
                            blockgroup.getFileLength(), isEnabled, isDefault, name, language,
                            blockgroup.getValueString(Tracks.CODEC_PRIVATE), hasCompression);
                }
                case SRT_CODEC_ID -> {
                    return new SRTSubtitles(trackNumber, blockgroup.getFilePosition(),
                            blockgroup.getFileLength(), isEnabled, isDefault, name, language,
                            hasCompression);
                }
                case PGS_CODEC_ID -> {
                    return new PGSSubtitles(trackNumber, blockgroup.getFilePosition(),
                            blockgroup.getFileLength(), isEnabled, isDefault, name, language,
                            hasCompression);
                }
            }
            log.warn("{} Unable to parse subtitles codec id: {}", TAG, codecID);
        }
        return null;
    }

    Subtitles(Type type, int trackNumber, long position, long size,
              boolean isEnabled, boolean isDefault, String name, String language, boolean isCompressed) {
        super(trackNumber, position, size, isEnabled, isDefault, name, language);
        mIsCompressed = isCompressed;
        mType = type;
        mUnreadCaptions = new ArrayList<>();
        mReadCaptions = new ArrayList<>();
    }

    /**
     * Add a block of subtitle data with timecode and duration
     * The data appended is added to the unreadsubtitle list, use readUnreadSubtitles to move it
     * to read and return the subtitles back
     * Internal use only
     *
     * @param block    of subtitle data from the cluster entry
     * @param timecode the time when this subtitle is shown
     * @param duration the time of how long the subtitle is shown for
     */
    public abstract void appendBlock(BlockElement block, int timecode, int duration);

    /**
     * Move the unread subtitles that was appended and returns the new unread subtitles to be parsed
     * This function exists so that you can stream append and read subtitles instead of waiting to
     * finish reading all the subtitles from a file first
     *
     * @return a list of subtitles that were not read yet
     */
    public List<Caption> readUnreadSubtitles() {
        synchronized (mUnreadCaptions) {
            ArrayList<Caption> list = new ArrayList<>(mUnreadCaptions);

            // Transfer the unread captions to read
            mReadCaptions.addAll(mUnreadCaptions);
            mUnreadCaptions.clear();
            return list;
        }
    }

    /**
     * Get the read captions.
     * Once the captions have been appended internally, you should run readUnreadSubtitles() to read
     * all the new subtitles and then they get moved to read status.
     *
     * @return
     */
    public ArrayList<Caption> getAllReadCaptions() {
        return mReadCaptions;
    }

    /**
     * 获取内容并转换为 VTT
     *
     * @return VTT
     */
    public String getContentsToVTT() {
        StringBuilder sb = new StringBuilder();
        sb.append("WEBVTT\n\n");
        int n = 1;

        for (Caption caption : mReadCaptions) {
            String entry = caption.getFormattedVTT();
            if (entry != null) {
                sb.append(n++).append('\n')
                        .append(caption.getStartTime().format())
                        .append(" --> ")
                        .append(caption.getEndTime().format()).append('\n')
                        .append(caption.getFormattedVTT().replaceAll("(?i)\\\\n", "\n"))
                        .append("\n\n");
            }
        }

        for (Caption caption : mUnreadCaptions) {
            String entry = caption.getFormattedVTT();
            if (entry != null) {
                sb.append(n++).append('\n')
                        .append(caption.getStartTime().format())
                        .append(" --> ")
                        .append(caption.getEndTime().format()).append('\n')
                        .append(entry.replaceAll("(?i)\\\\n", "\n"))
                        .append("\n\n");
            }
        }
        return sb.toString();
    }

    /**
     * Get number of subtitles
     *
     * @return
     */
    public int getSubtitleCount() {
        synchronized (mUnreadCaptions) {
            return mReadCaptions.size() + mUnreadCaptions.size();
        }
    }

    /**
     * Get type of subtitles
     *
     * @return either SSA or SRT
     */
    public Type getType() {
        return mType;
    }

    /**
     * Get a more presentable entry name
     * Format: 'Name: [lang]'
     *
     * @return presentable name
     */
    public String getPresentableName() {
        StringBuilder sb = new StringBuilder();
        if (mName != null) {
            sb.append(mName);
        }
        if (mLanguage != null) {
            sb.append(" [").append(mLanguage).append(']');
        }
        return sb.toString();
    }

    @Override
    public int getTrackType() {
        return Tracks.Type.SUBTITLE;
    }

    public abstract String getContents();

    protected void appendCaption(Caption caption) {
        synchronized (mUnreadCaptions) {
            mUnreadCaptions.add(caption);
        }
    }
}
