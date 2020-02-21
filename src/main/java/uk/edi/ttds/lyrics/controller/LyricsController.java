package uk.edi.ttds.lyrics.controller;


import com.alibaba.fastjson.*;
import com.alibaba.fastjson.JSONObject;

import javafx.beans.binding.IntegerBinding;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import proximty.POJO.Querys;
import proximty.POJO.RankedDocuments;
import proximty.resource.Constants;
import proximty.resource.RetrievalModel;
import proximty.retrieval_module.SearchEngine;
import uk.edi.ttds.lyrics.entity.Song;
import uk.edi.ttds.lyrics.entity.User;
import uk.edi.ttds.lyrics.service.LyricsService;
import uk.edi.ttds.lyrics.mongo_entity.*;
import uk.edi.ttds.lyrics.service.SingerService;
import uk.edi.ttds.lyrics.service.Song_nameService;

import java.util.*;


@Controller
@RequestMapping("/search")
public class LyricsController {
    private String  preprocess_url="http://127.0.0.1:5000/api/preprocess";

    private String index_url="http://127.0.0.1:5000/api/index";

    private static long startTime = System.currentTimeMillis();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private  RestTemplate restTemplate;

    @Autowired
     public LyricsService lyricsService;

    @Autowired
    public SingerService singerService;

    @Autowired
    public Song_nameService song_nameService;

    public ArrayList<Song> songs;

    @RequestMapping("/insert")
    public String insert(){
        Doc d=new Doc();
        d.setDocument_id("001");
        d.setId("000");
        d=mongoTemplate.insert(d);
        if(d!=null){
            return "success";
        }else {
            return "false";
        }
    }

    @RequestMapping(value = "/query",method = RequestMethod.GET)
    @ResponseBody
    public void query(@RequestParam(value = "query")String query) throws JSONException {
        RestTemplate restTemplate=new RestTemplate();
        ArrayList<Song> song_set=new ArrayList<>();
        ArrayList<Song> singer_set=new ArrayList<>();
        ArrayList<Song> song_name_set=new ArrayList<>();

        ArrayList<Integer> non_lyrics_list=new ArrayList<>();

        ArrayList<String> lyrics_set=null;
        ArrayList<String> result=new ArrayList<>();
        //split query by space
        String[] list=query.split(" ");

        //iterate the list of query and  search it in the order of singer> song > lyrics

        for(int i=0;i<list.length;i++){

            songs= (ArrayList<Song>) singerService.findSongsBySinger(list[i]);
            if (!songs.isEmpty()){
                for(int j=0;j<songs.size();j++){
                    if (!singer_set.contains(songs.get(j)))singer_set.add(songs.get(j));
                }
                //song_set=collect_songs(song_set,songs);
                non_lyrics_list.add(i);

            }else {
                songs.clear();
                songs=(ArrayList<Song>)song_nameService.findSongsBySong_name(list[i]);
                if (!songs.isEmpty()){
                    for(int j=0;j<songs.size();j++){
                        if (!song_name_set.contains(songs.get(j)))song_name_set.add(songs.get(j));
                    }
                 //song_set=collect_songs(song_set,songs);
                 non_lyrics_list.add(i);

                }
            }
        }
        song_set=collect_songs(singer_set,song_name_set);


        /*
        * Search other terms by using BM25 and proximity search
        * */
        String temp_query="";

        for (int i=0;i<list.length;i++){
            if (!non_lyrics_list.contains(i)){
                temp_query+=list[i]+" ";
            }
        }


       if (temp_query!="") {
           System.out.println("path one");
           String term = query_preprocess(temp_query, preprocess_url);
           term = create_index(term, index_url);
           System.out.println(term);

           // Querys containing all the query present in the file at the location
           // specified by the Query_path constant.
           Querys querys = new Querys(Constants.QUERY_PATH);
           //System.out.println(term);
           lyrics_set = runSearchEngine(querys, RetrievalModel.PROXIMITY_SCORE, term);


           for (int i = 0; i < song_set.size(); i++) {
               if (lyrics_set.contains(Integer.toString(song_set.get(i).getId()))) {
                   result.add(Integer.toString(song_set.get(i).getId()));
               }
           }

           if (result.size() == 0) {
               result = lyrics_set;
           }
           for (int i = 0; i < result.size(); i++) {
               System.out.println(result.get(i));
           }
       }else {
           if (song_set.size()==0){
               if (singer_set.size()>song_name_set.size()){
                   for (int i=0;i<song_name_set.size();i++){
                       song_set.add(song_name_set.get(i));
                   }

                   for (int i=0;i<singer_set.size();i++){
                       song_set.add(singer_set.get(i));
                   }
               }else {
                   for (int i=0;i<singer_set.size();i++){
                       song_set.add(singer_set.get(i));
                   }
                   for (int i=0;i<song_name_set.size();i++){
                       song_set.add(song_name_set.get(i));
                   }

               }
           }

           for (int i = 0; i < song_set.size(); i++) {
               System.out.println(song_set.get(i).getSong_name()+" "+song_set.get(i).getSinger());
           }
       }
    }


    private ArrayList<Song> collect_songs(ArrayList<Song> songs1,ArrayList<Song> songs2){
        ArrayList<Song> common=new ArrayList<>();
        for (int i=0;i<songs2.size();i++){
           for (int j=0;j<songs1.size();j++){
               if(songs2.get(i).getSong_name().equals(songs1.get(j).getSong_name())){
                   common.add(songs2.get(i));
               }
           }
        }
            //songs1.addAll(songs2);
            return  common;
    }

    private String query_preprocess(String query,String url){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject param = new JSONObject();
        param.put("query" ,"["+query+"]");
       // System.out.println(param.toJSONString());
        HttpEntity<String> entity = new HttpEntity<>(param.toJSONString(), httpHeaders);
        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.POST , entity , String.class);
        JSONObject jsonObject=JSON.parseObject(response.getBody());
        String term=jsonObject.getString("ans");
        return term;
    }

    private String create_index(String term,String url){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String regex="[,\"\"]+";
        term=term.replaceAll(regex," ");
        term=term.replaceAll("[\\[\\]]","");
        term=term.trim();
        //System.out.println(term+" create_index");
        JSONObject param = new JSONObject();
        param.clear();
        param.put("query",term);
        HttpEntity<String>  entity=new HttpEntity<>(param.toJSONString(),httpHeaders);
        HttpEntity<String> response=restTemplate.exchange(url,HttpMethod.POST,entity,String.class);
        return  term;
    }

    private static ArrayList<String> runSearchEngine(Querys querys, RetrievalModel model,String term) {

        SearchEngine searchEngine = new SearchEngine(model);
        searchEngine.setDisplayResults(true);
        RankedDocuments rankedDocuments=searchEngine.search(querys,10,term);

        Set<String> keys = rankedDocuments.keySet();
        /*
        Iterator<String> iterator = keys.iterator();
        int count = 0;
        // System.out.println(keys.size());
        while (iterator.hasNext()) {
            count++;
            String docID = iterator.next();
            System.out.println(count + ". " + docID + " - " + rankedDocuments.get(docID));
        }
        displayTime();
        */
         return new ArrayList<String>(keys);
    }

    /**
     * Display the execution time till present.
     */
    private static void displayTime() {
        System.out.println();
        System.out.println("Execution time: " + ((long) System.currentTimeMillis() - startTime) / 1000f + " sec");
    }



}
