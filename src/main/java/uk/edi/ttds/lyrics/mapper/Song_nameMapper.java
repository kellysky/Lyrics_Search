package uk.edi.ttds.lyrics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import uk.edi.ttds.lyrics.entity.Song;

import java.util.List;

@Mapper
public interface Song_nameMapper {

    @Select("Select * from Song where song_name=#{song_name}")
    public List<Song> findSongsBySong_name(@Param("song_name")String song_name);
}

