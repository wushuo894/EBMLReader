package com.matthewn4444.ebml.subtitles;

import com.matthewn4444.ebml.elements.BlockElement;

public class SRTCaption extends Caption {
    private String mCachedData;

    public SRTCaption(BlockElement block, int timecode, int duration, boolean isCompressed) {
        super(Subtitles.Type.SRT, block, timecode, duration, isCompressed);
    }

    @Override
    public String getFormattedText() {
        if (mCachedData == null) {
            StringBuilder sb = new StringBuilder();
            formatTimePoint(getStartTime(), sb);
            sb.append(" --> ");
            formatTimePoint(getEndTime(), sb);
            sb.append('\n')
                .append(getStringData())
                .append("\n\n");
            mCachedData = sb.toString();
        }
        return mCachedData;
    }

    @Override
    public String getFormattedVTT() {
        return getStringData();
    }
}
