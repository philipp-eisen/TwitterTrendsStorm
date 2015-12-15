package com.gpjpe.spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.gpjpe.domain.reader.KafkaStreamReader;
import com.gpjpe.helpers.Utils;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class KafkaTweetsSpout extends BaseRichSpout {

    private final static Logger LOGGER = Logger.getLogger(KafkaTweetsSpout.class.getName());

    private SpoutOutputCollector _collector;
    private Set<String> languagesToWatch;
    private long firstTweetTimestamp;
    private static final long UNSET = -1;
    private KafkaStreamReader streamReader;
    private String zookeeperURI;
    private String topic;

    public KafkaTweetsSpout(String[] languagesToWatch, String zookeeperURI, String topic) {
        this.languagesToWatch = new HashSet<>();
        this.languagesToWatch.addAll(Arrays.asList(languagesToWatch));
        this.firstTweetTimestamp = UNSET;
        this.zookeeperURI = zookeeperURI;
        this.topic = topic;
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("lang", "hashtag", "timestamp", "initTimestamp"));
    }

    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this._collector = spoutOutputCollector;
        //must be initialized here and not in constructor or will cause errors
        this.streamReader = new KafkaStreamReader(zookeeperURI, this.getClass().getName(), topic);
        this.streamReader.run(1);
    }

    public void close() {
        this.streamReader.shutdown();
    }

    public void nextTuple() {

        try {
//            String tweetString = streamReader.nextTweet();
//
//            if (tweetString != null) {
//                String[] tuple = tweetString.split(",");
//                String tweetLanguage = tuple[0].trim();
//                long timestamp = Long.parseLong(tuple[1].trim());
//                String hashTag = tuple[2].trim();
//
//                if(this.firstTweetTimestamp == UNSET){
//                    this.firstTweetTimestamp = timestamp;
//                }
//
//                if (this.languagesToWatch.contains(tweetLanguage)) {
//                    this._collector.emit(new Values(tweetLanguage, hashTag, timestamp, this.firstTweetTimestamp));
//                    LOGGER.debug(tuple);
//                } else {
//                    LOGGER.debug("Tweet is not of interest");
//                }
//            } else {
//                //no work, put CPU to sleep for a spell
//                Thread.sleep(1);
//            }


            Values tweet = Utils.tweet(this.firstTweetTimestamp);

            if(this.firstTweetTimestamp == UNSET){
                this.firstTweetTimestamp = System.currentTimeMillis()/1000;
            }


            if (this.languagesToWatch.contains((String)tweet.get(0))){
                this._collector.emit(tweet);
            }

        } catch (Exception e) {
            _collector.reportError(e);
            LOGGER.error(e);
        }
    }
}
