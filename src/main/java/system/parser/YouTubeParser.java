package system.parser;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.CharMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import system.parser.shared.ThumbnailItem;
import system.parser.shared.VideoRenderer;
import system.parser.shared.YouTubeVideoItem;
import system.shared.Feed;
import system.shared.Video;

public class YouTubeParser implements Callable<Feed>
{
    private static final Logger logger = LogManager.getLogger(YouTubeParser.class);

    private static final String TRENDING_URL = "https://www.youtube.com/feed/trending?hl=ru&gl=RU";
    private static final long YOUTUBE_WAIT_TIME = 10000; // ms

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Feed call() throws Exception
    {
        try
        {
            logger.info("Start YouTube parsing");
            WebClient webClient = new WebClient();

            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

            HtmlPage trendingPage = webClient.getPage(TRENDING_URL);

            webClient.waitForBackgroundJavaScript(YOUTUBE_WAIT_TIME);

            // Это регуляркой дергаем со страницы сайта JSON объект, который мы и анализируем. Да это жоска.
            Pattern pattern = Pattern.compile("ytInitialData\"] = (.*)");
            Matcher m = pattern.matcher(trendingPage.getWebResponse().getContentAsString());

            Feed feed = new Feed();

            if (m.find())
            {
                String jsonResponse = m.group(1);

                jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 1); // обрезаем ; в конце

                JSONObject response = new JSONObject(jsonResponse);

                // Начинаем разбор огроменного JSON объекта от google.
                // Слабонервным лучше не смотреть. На эту хуйню вообще лучше не смотреть.
                // Но если отважитесь то welcome в resources/example.json там пример этого ответа.
                JSONArray tabs = (JSONArray) response.query("/contents/twoColumnBrowseResultsRenderer/tabs");
                JSONArray contents = (JSONArray) tabs.getJSONObject(0).query(
                        "/tabRenderer/content/sectionListRenderer/contents");

                for (Object content : contents)
                {
                    try
                    {
                        JSONObject _content = (JSONObject) content;
                        JSONObject shelfRenderer = ((JSONArray) _content.query("/itemSectionRenderer/contents")).getJSONObject(
                                0);
                        JSONArray items = (JSONArray) shelfRenderer.query(
                                "/shelfRenderer/content/expandedShelfContentsRenderer/items");

                        for (Object item : items)
                        {
                            try
                            {
                                YouTubeVideoItem youTubeVideoItem = objectMapper.readValue(item.toString(),
                                                                                           YouTubeVideoItem.class);

                                VideoRenderer videoRenderer = youTubeVideoItem.getVideoRenderer();

                                String videoId = videoRenderer.getVideoId();
                                String name = videoRenderer.getTitle().getSimpleText();
                                String imgUrl = getFirstImageUrl(videoRenderer.getThumbnail().getThumbnails());

                                Video video = new Video(videoId, name, imgUrl);

                                String channelName = videoRenderer.getShortBylineText().getRuns().get(0).getText();
                                video.setChannel(channelName);

                                String old = videoRenderer.getPublishedTimeText().getSimpleText();
                                video.setOld(old);

                                String viewCountString = videoRenderer.getViewCountText().getSimpleText();

                                long viewCount = Long.valueOf(CharMatcher.inRange('0',
                                                                                  '9').retainFrom(viewCountString));
                                video.setViewCount(viewCount);

                                // Parse description in end because description often may not exist
                                if (videoRenderer.getDescriptionSnippet() != null)
                                {
                                    String description = videoRenderer.getDescriptionSnippet().getSimpleText();
                                    video.setDescription(description);
                                }

                                feed.getVideos().add(video);
                            }
                            catch (ImageUrlNotExistException e)
                            {
                                logger.warn("Image URL not found");
                            }
                            catch (JSONException e)
                            {
                                logger.error("JSON Error", e);
                                // Выводим последнее видео на котором выпал exception,
                                // чтобы потом по логам чекнуть где трабла
                                logger.warn(feed.getVideos().get(feed.getVideos().size() - 1).toString());
                            }
                            catch (Exception e)
                            {
                                logger.error("Item parsing exception", e);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Contents parsing exception", e);
                    }
                }
            }

            return feed;
        }
        catch (Exception e)
        {
            logger.error("Error", e);
            return null;
        }
    }

    private String getFirstImageUrl(List<ThumbnailItem> thumbnailItems) throws ImageUrlNotExistException
    {
        for (ThumbnailItem thumbnailItem : thumbnailItems)
        {
            Pattern pattern = Pattern.compile("(http.*\\.jpg).*");
            Matcher matcher = pattern.matcher(thumbnailItem.getUrl());

            if (matcher.matches())
            {
                return matcher.group(1);
            }
        }

        throw new ImageUrlNotExistException();
    }
}
