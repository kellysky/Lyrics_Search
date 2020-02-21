package uk.edi.ttds.lyrics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import uk.edi.ttds.lyrics.entity.Song;

import java.util.List;

@Mapper
public interface SingerMapper {

    @Select("Select * from Song where singer=#{singer}")
    public List<Song> findSongsBySinger(@Param("singer")String singer);
}
