package com.matthewn4444.ebml.elements;

import com.matthewn4444.ebml.EBMLException;
import com.matthewn4444.ebml.node.NodeBase;
import com.matthewn4444.ebml.node.StringNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

@Slf4j
public class StringElement extends ElementBase {
    private String mData;

    StringElement(StringNode node, long position) throws UnsupportedEncodingException {
        super(NodeBase.Type.STRING, node.id(), position);
        if (node.getDefault() != null) {
            mData = node.getDefault();
        }
    }

    /**
     * Get the string data in this element
     * @return string
     */
    public String getString() {
        return mData;
    }

    @Override
    boolean read(RandomAccessFile raf) throws IOException {
        super.read(raf);

        int size = (int) mInnerLength;
        if (size != mInnerLength) {
            throw new EBMLException("Cannot use length as buffer because it is too long!");
        }
        byte[] buffer = new byte[(int) mInnerLength];
        raf.read(buffer);
        mData = new String(buffer, "utf8");
        return true;
    }

    @Override
    public StringBuilder output(int level) {
        StringBuilder sb = super.output(level);
        String data = mData.length() > 20 ? mData.substring(0, 20) + "..." : mData;
        log.info(TAG+" "+ sb.toString() + "STR [" + hexId() + "]: '" + data + "'");
        return null;
    }
}
