package uk.edi.ttds.lyrics.service;

import org.springframework.stereotype.Service;
import uk.edi.ttds.lyrics.entity.Lyrics;

@Service
public interface LyricsService {

    public Lyrics findSong();
}
