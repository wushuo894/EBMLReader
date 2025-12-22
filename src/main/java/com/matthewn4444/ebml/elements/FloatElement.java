package com.matthewn4444.ebml.elements;

import com.matthewn4444.ebml.EBMLParsingException;
import com.matthewn4444.ebml.node.FloatNode;
import com.matthewn4444.ebml.node.NodeBase;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;

@Slf4j
public class FloatElement extends ElementBase {
    private float mData;

    FloatElement(FloatNode nextNode, long position) {
        super(NodeBase.Type.FLOAT, nextNode.id(), position);
        mData = nextNode.getDefault();
    }

    /**
     * Get the float data from this element entry
     * @return float data
     */
    public float getData() {
        return mData;
    }

    @Override
    boolean read(RandomAccessFile raf) throws IOException {
        super.read(raf);

        if (mInnerLength == 4) {
            mData = raf.readFloat();
        } else {
            throw new EBMLParsingException("get float [id= " + hexId() + " @ 0x" +
                    Long.toHexString(raf.getFilePointer()) + "] with len = " + mInnerLength
                    + " is not supported");
        }
        return true;
    }

    @Override
    public StringBuilder output(int level) {
        StringBuilder sb = super.output(level);
        log.info(TAG+" "+ sb.toString() + "FLOAT [" + hexId() + "]: " + mData);
        return null;
    }
}
