package uk.edi.ttds.lyrics.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.edi.ttds.lyrics.entity.Song;
import uk.edi.ttds.lyrics.mapper.SingerMapper;
import uk.edi.ttds.lyrics.service.SingerService;

import java.util.List;

@Service
public class SingerServiceImpl implements SingerService {
    @Autowired
    public SingerMapper singerMapper;

    @Override
    public List<Song> findSongsBySinger(String singer) {
        return singerMapper.findSongsBySinger(singer);
    }
}
