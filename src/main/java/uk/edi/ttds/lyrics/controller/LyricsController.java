package uk.edi.ttds.lyrics.controller;


import com.alibaba.fastjson.*;
import com.alibaba.fastjson.JSONObject;

import javafx.beans.binding.IntegerBinding;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
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

import javax.servlet.http.HttpSession;
import java.util.*;


@Controller
@RequestMapping("/search")
public class LyricsController {
    private String  preprocess_url="http://127.0.0.1:5000/api/preprocess";

    private String index_url="http://127.0.0.1:5000/api/index";

    private String metadata_url="http://127.0.0.1:5000/api/metadata";

    private String similarity_url="http://172.20.10.8:5000/api/similarity";

    //private static long startTime = System.currentTimeMillis();

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


    public ModelAndView modelAndView;

    public ArrayList<Song> final_result;

    public ArrayList<Song> result_single_page;

    public ArrayList<Song> similarity_song;

    public int page_num;

    public int result_size;

    public String user_query;

    public String  metadata=null;

    public static HashMap<String,ArrayList<Song>> map=new HashMap<>();
    public static HashMap<String,ArrayList<Song>> similarity_map=new HashMap<>();



    @RequestMapping(value="/index")
    public String index(){
        return "mainpage";
    }

    @RequestMapping(value = "/query.action",method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView query(@RequestParam(value = "query")String query) throws JSONException {
        long startTime = System.currentTimeMillis();

        modelAndView=new ModelAndView();
        user_query=query;


        RestTemplate restTemplate=new RestTemplate();
        ArrayList<Song> song_set=new ArrayList<>();
        ArrayList<Song> singer_set=new ArrayList<>();
        ArrayList<Song> song_name_set=new ArrayList<>();

        ArrayList<Integer> non_lyrics_list=new ArrayList<>();

        ArrayList<String> lyrics_set=null;
        ArrayList<String> result=new ArrayList<>();
        final_result=new ArrayList<>();
        result_single_page=new ArrayList<>();
        similarity_song=new ArrayList<>();



        //split query by space
        String[] list=query.split(" ");

        //iterate the list of query and  search it in the order of singer> song > lyrics
        long start= System.currentTimeMillis();

        for(int i=0;i<list.length;i++){
            songs= (ArrayList<Song>) singerService.findSongsBySinger(list[i]);

            if (!songs.isEmpty()){
                for(int j=0;j<songs.size();j++){
                    if (!singer_set.contains(songs.get(j)))singer_set.add(songs.get(j));
                }
                non_lyrics_list.add(i);

            }else {
                songs.clear();
                songs=(ArrayList<Song>)song_nameService.findSongsBySong_name(list[i]);
                if (!songs.isEmpty()){
                    for(int j=0;j<songs.size();j++){
                        if (!song_name_set.contains(songs.get(j)))song_name_set.add(songs.get(j));
                    }
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



           String term = query_preprocess(temp_query, preprocess_url);


           String regex="[,\"\"]+";
           term=term.replaceAll(regex," ");
           term=term.replaceAll("[\\[\\]]","");
           term=term.trim();

           start=System.currentTimeMillis();
           //index list
           String[] term_list = create_index(term, index_url);


           //metada list
           if (metadata==null) {
               metadata = create_metadata(metadata_url);
           }



           // Querys containing all the query present in the file at the location
           // specified by the Query_path constant.
           Querys querys = new Querys(Constants.QUERY_PATH);



           lyrics_set = runSearchEngine(querys, RetrievalModel.PROXIMITY_SCORE, term,term_list,metadata);

           for (int i = 0; i < song_set.size(); i++) {
               if (lyrics_set.contains(Integer.toString(song_set.get(i).getId()))) {
                   result.add(Integer.toString(song_set.get(i).getId()));
               }
           }
           if (result.size() == 0) {
               result = lyrics_set;
           }

//           start=System.currentTimeMillis();

           if (result.size()!=0) {
               final_result = singerService.findSongById(result);
           }


       }else {
           start=System.currentTimeMillis();
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
               final_result.add(song_set.get(i));
           }

       }




       //get similarity songs
        if (final_result.size()!=0) {
            similarity_song = get_Similarity_Song(String.valueOf(final_result.get(0).id), similarity_url);
        }
       //System.out.println(final_result.get(0).id);


       page_num=final_result.size()/4+1;
       if (final_result.size()>4) {
           for (int i = 0; i < 4; i++) {
               result_single_page.add(final_result.get(i));
           }
           modelAndView.addObject("song_list",result_single_page);
       }else {
           modelAndView.addObject("song_list",final_result);
       }




       result_size=final_result.size();
       modelAndView.addObject("similarity_song",similarity_song);
       modelAndView.addObject("page_num",page_num);
        modelAndView.addObject("result_size",result_size);
        modelAndView.addObject("user_query",user_query);
       modelAndView.setViewName("resultpage");
       displayTime(startTime,"total time");

        if (map.containsKey(user_query)) {
            map.remove(user_query);

        }
        map.put(user_query, final_result);

        if (similarity_map.containsKey(user_query)){
            similarity_map.remove(user_query);
        }
        similarity_map.put(user_query,similarity_song);
       return modelAndView;
    }

    @RequestMapping(value="transferpage.action",method =RequestMethod.POST)
    @ResponseBody
    public ModelAndView transfer_page(@RequestParam(value = "page")String page,@RequestParam(value ="truequery")String truequery){
        modelAndView=new ModelAndView();
        final_result=map.get(truequery);
        similarity_song=similarity_map.get(truequery);



        int num= Integer.valueOf(page);
        int total_page_num=final_result.size()/4+1;

        result_single_page=new ArrayList<>();
        for (int i=0;i<4;i++){
            if (((num-1)*4+i)<=(final_result.size()-1)) {

                result_single_page.add(final_result.get((num - 1) * 4 + i));
            }
        }

        modelAndView.addObject("similarity_song",similarity_song);
        modelAndView.addObject("song_list",result_single_page);
        modelAndView.addObject("page_num",page_num);
        modelAndView.addObject("user_query",truequery);
        modelAndView.addObject("result_size",String.valueOf(final_result.size()));
        modelAndView.setViewName("resultpage");
        return modelAndView;
    }

    @RequestMapping(value = "showSimilarity.action",method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView showSimilarity(@RequestParam(value = "similarity_id")String similarity_id,@RequestParam(value = "postquery_id")String postquery_id){
        modelAndView=new ModelAndView();
        similarity_song=new ArrayList<>();
        //System.out.println(similarity_id+postquery_id);
        similarity_song=similarity_map.get(postquery_id);
       // System.out.println(similarity_song.size());
        for (int i=0;i<similarity_song.size();i++){

            if (similarity_song.get(i).getId()==Integer.valueOf(similarity_id)) {
                String[] lyrics = similarity_song.get(i).getReal_content().split("\\s+");
                //System.out.println(similarity_song.get(i).getReal_content());
                modelAndView.addObject("Lyrics", lyrics);
                modelAndView.addObject("singer", similarity_song.get(i).getSinger());
                modelAndView.addObject("song_name", similarity_song.get(i).getSong_name());
                break;
            }
        }
        modelAndView.setViewName("lyric");
        return modelAndView;
    }

    @RequestMapping(value = "showLyrics.action",method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView showLyrics(@RequestParam(value = "song_id")String song_id,@RequestParam(value ="postquery")String postquery){
        modelAndView=new ModelAndView();
        final_result=new ArrayList<>();
        final_result=map.get(postquery);

        for (int i=0;i<final_result.size();i++){
            if (final_result.get(i).getId()==Integer.parseInt(song_id)){
                System.out.println(final_result.get(i).getSong_name());
                String[] lyrics=final_result.get(i).getReal_content().split(" ");
                modelAndView.addObject("Lyrics",lyrics);
                modelAndView.addObject("singer",final_result.get(i).getSinger());
                modelAndView.addObject("song_name",final_result.get(i).getSong_name());
                break;
            }
        }

        modelAndView.setViewName("lyric");
        return modelAndView;
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

        HttpEntity<String> entity = new HttpEntity<>(param.toJSONString(), httpHeaders);
        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.POST , entity , String.class);
        JSONObject jsonObject=JSON.parseObject(response.getBody());

        String term=jsonObject.getString("ans");
        return term;
    }

    private String[] create_index(String term,String url){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject param = new JSONObject();
        param.clear();
        param.put("query",term);
        HttpEntity<String>  entity=new HttpEntity<>(param.toJSONString(),httpHeaders);
        HttpEntity<String> response=restTemplate.exchange(url,HttpMethod.POST,entity,String.class);

        JSONObject jsonObject=JSON.parseObject(response.getBody());

        String term_response=jsonObject.getString("ans");
        term_response=term_response.replace("\"","");
        String[] term_list=term_response.split(",");

        return  term_list;
    }

    //this fucntion is to generate all metadata list from python api
    private String create_metadata(String metadata_url){
        String metadata=null;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject param = new JSONObject();
        param.clear();
        param.put("query","metadata");
        HttpEntity<String>  entity=new HttpEntity<>(param.toJSONString(),httpHeaders);
        HttpEntity<String> response=restTemplate.exchange(metadata_url,HttpMethod.POST,entity,String.class);

        JSONObject jsonObject=JSON.parseObject(response.getBody());
        metadata=jsonObject.getString("ans");
        metadata=metadata.replace("\"","");

        return metadata;
    }

    //this function can provide some similarity songs
    private ArrayList<Song> get_Similarity_Song(String song_id,String url){
        ArrayList<Song> similarity_songs=new ArrayList<>();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject param = new JSONObject();
        param.put("query" ,song_id);

        HttpEntity<String> entity = new HttpEntity<>(param.toJSONString(), httpHeaders);
        HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.POST , entity , String.class);
        JSONObject jsonObject=JSON.parseObject(response.getBody());

        String term=jsonObject.getString("ans");
        String[] list=term.split("\\n");
        //System.out.println(term);
        for (int i=1;i<list.length;i++){
            String lyric=list[i].replace("\\n","");
            String[] properties=lyric.split("\\s+");
            if (!properties[1].equals(song_id)) {
                Song song = new Song();
                song.setId(Integer.valueOf(properties[1]));
                song.setSong_name(properties[3]);
                song.setSinger(properties[2]);
                lyric = "";
                for (int j = 4; j < properties.length; j++) {
                    lyric += properties[j] + " ";
                }
                song.setReal_content(lyric);
                song.setContent(properties[4]);
                similarity_songs.add(song);
            }
        }

        return similarity_songs;
    }

    private static ArrayList<String> runSearchEngine(Querys querys, RetrievalModel model,String term,String[] term_list,String metadata) {

        SearchEngine searchEngine = new SearchEngine(model,term_list,metadata);
        searchEngine.setDisplayResults(true);
        RankedDocuments rankedDocuments=searchEngine.search(querys,5,term);

        Set<String> keys = rankedDocuments.keySet();
         return new ArrayList<String>(keys);
    }

    /**
     * Display the execution time till present.
     */
    private static void displayTime(long startTime,String name) {
        System.out.println();
        long timeConsume = System.currentTimeMillis() - startTime;
        System.out.println(name+" Execution time: " + ((long) System.currentTimeMillis() - startTime) / 1000f + " sec");
    }



}
