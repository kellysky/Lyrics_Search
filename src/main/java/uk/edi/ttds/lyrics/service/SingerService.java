package uk.edi.ttds.lyrics.service;

import org.springframework.stereotype.Service;
import uk.edi.ttds.lyrics.entity.Song;

import java.util.List;

@Service
public interface SingerService {
    public List<Song> findSongsBySinger(String singer);
}
