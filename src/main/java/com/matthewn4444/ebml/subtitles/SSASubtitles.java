package com.matthewn4444.ebml.subtitles;

import com.matthewn4444.ebml.elements.BlockElement;

public class SSASubtitles extends Subtitles {
    private final String mHeaderData;

    SSASubtitles(int trackNumber, long position, long size, boolean isEnabled, boolean isDefault,
            String name, String language, String headerData, boolean isCompressed) {
        super(Subtitles.Type.SSA, trackNumber, position, size, isEnabled, isDefault, name,
                language, isCompressed);
        mHeaderData = headerData;
    }

    @Override
    public void appendBlock(BlockElement block, int timecode, int duration) {
        appendCaption(new SSACaption(block, timecode, duration, mIsCompressed));
    }

    @Override
    public String getContents() {
        StringBuilder sb = new StringBuilder();
        sb.append(mHeaderData);
        for (Caption mReadCaption : mReadCaptions) {
            sb.append(mReadCaption.getFormattedText()).append("\n");
        }
        for (Caption mUnreadCaption : mUnreadCaptions) {
            sb.append(mUnreadCaption.getFormattedText()).append("\n");
        }
        return sb.toString();
    }

    public String getHeader() {
        return mHeaderData;
    }
}
