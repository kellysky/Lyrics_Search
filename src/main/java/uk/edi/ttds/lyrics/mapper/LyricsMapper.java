package uk.edi.ttds.lyrics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import uk.edi.ttds.lyrics.entity.Lyrics;

@Mapper
public interface LyricsMapper {
      @Select("select * from Song")
      public Lyrics find();
}
