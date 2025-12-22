import com.matthewn4444.ebml.EBMLReader;
import com.matthewn4444.ebml.subtitles.Caption;
import com.matthewn4444.ebml.subtitles.SSASubtitles;
import com.matthewn4444.ebml.subtitles.Subtitles;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestEBMLReader {
    @Test
    public void subs() throws IOException {
        EBMLReader reader = new EBMLReader("/Users/wushuo/Movies/test/[LoliHouse] 蓝色管弦乐 S02E11.mkv");
        if (!reader.readHeader()) {
            return;
        }
        reader.readTracks();
        reader.readCues();

        for (int i = 0; i < reader.getCuesCount();i++) {
            reader.readSubtitlesInCueFrame(i);
        }

        List<Subtitles> subtitles = reader.getSubtitles();
        for (Subtitles subtitle : subtitles) {
            System.out.println(subtitle.getContentsToVTT());
        }
    }
}
