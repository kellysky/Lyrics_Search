package uk.edi.ttds.lyrics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import uk.edi.ttds.lyrics.entity.Song;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface SingerMapper {

    @Select("Select * from Song where singer=#{singer}")
    public List<Song> findSongsBySinger(@Param("singer")String singer);

//    @Select("Select * from Song where id=#{id}")
//    public Song findSongById(@Param("id")String id);

    @Select("<script> " +
            "select * from Song where id in " +
            "<foreach item='item' index='index' collection='list_id' open='(' separator=',' close=')'> " +
            "   #{item} " +
            "</foreach>" +
            "</script> ")
    public ArrayList<Song>  findSongById(@Param("list_id") ArrayList<String> list_id);
}
