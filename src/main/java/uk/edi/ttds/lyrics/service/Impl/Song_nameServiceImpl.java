package uk.edi.ttds.lyrics.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.edi.ttds.lyrics.entity.Song;
import uk.edi.ttds.lyrics.mapper.Song_nameMapper;
import uk.edi.ttds.lyrics.service.Song_nameService;

import java.util.List;

@Service
public class Song_nameServiceImpl implements Song_nameService {
    @Autowired
    public Song_nameMapper song_nameMapper;


    @Override
    public List<Song> findSongsBySong_name(String Song_name) {
        return song_nameMapper.findSongsBySong_name(Song_name);
    }
}
