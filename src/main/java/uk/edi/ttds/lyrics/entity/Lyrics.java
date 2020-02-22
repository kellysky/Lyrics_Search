package uk.edi.ttds.lyrics.entity;

public class Lyrics {
    public int id;
    public String singer;
    public String real_content;

    public String getReal_content() {
        return real_content;
    }

    public void setReal_content(String real_content) {
        this.real_content = real_content;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String song_name;
    public String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
