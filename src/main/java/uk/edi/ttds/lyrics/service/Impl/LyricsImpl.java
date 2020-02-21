package uk.edi.ttds.lyrics.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.edi.ttds.lyrics.entity.Lyrics;
import uk.edi.ttds.lyrics.mapper.LyricsMapper;
import uk.edi.ttds.lyrics.service.LyricsService;
@Service
public class LyricsImpl implements LyricsService {
    @Autowired
    private LyricsMapper lyricsMapper;

    @Override
    public Lyrics findSong() {
        return lyricsMapper.find();
    }
}
