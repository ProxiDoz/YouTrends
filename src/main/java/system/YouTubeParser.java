package system;

import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.CharMatcher;
import com.sun.media.jfxmedia.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YouTubeParser implements Callable<Feed>
{
    private static final String TRENDING_URL = "https://www.youtube.com/feed/trending";
    private static final long YOUTUBE_WAIT_TIME = 10000; // ms

    @Override
    public Feed call() throws Exception
    {
        try
        {
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

                jsonResponse = jsonResponse.substring(0, jsonResponse.length()-1); // обрезаем ; в конце

                JSONObject response = new JSONObject(jsonResponse);

                // Начинаем разбор огроменного JSON объекта от google.
                // Слабонервным лучше не смотреть. На эту хуйню вообще лучше не смотреть.
                // Но если отважитесь то welcome в resources/example.json там пример этого ответа.
                JSONArray tabs = (JSONArray)response.query("/contents/twoColumnBrowseResultsRenderer/tabs");
                JSONArray contents = (JSONArray) tabs.getJSONObject(0).query("/tabRenderer/content/sectionListRenderer/contents");

                for(Object content: contents)
                {
                    JSONObject _content = (JSONObject)content;
                    JSONObject shelfRenderer = ((JSONArray) _content.query("/itemSectionRenderer/contents")).getJSONObject(0);
                    JSONArray items = (JSONArray) shelfRenderer.query("/shelfRenderer/content/expandedShelfContentsRenderer/items");

                    for (Object item: items)
                    {
                        try
                        {
                            JSONObject videoRenderer = ((JSONObject) item).getJSONObject("videoRenderer");

                            String videoId = videoRenderer.getString("videoId");
                            String name = videoRenderer.getJSONObject("title").getString("simpleText");
                            String imgUrl = videoRenderer.getJSONObject("thumbnail")
                                                         .getJSONArray("thumbnails")
                                                         .getJSONObject(0)
                                                         .getString("url");

                            Video video = new Video(videoId, name, imgUrl);
                            feed.getVideos().add(video);

                            // Отпусти меня ебучий спайс. Что в гугле курили, когда создавали такой JSON
                            JSONObject runs = (JSONObject) ((JSONArray) videoRenderer.query("/shortBylineText/runs")).get(0);

                            String channel = runs.getString("text");
                            video.setChannel(channel);

                            String old = videoRenderer.getJSONObject("publishedTimeText").getString("simpleText");
                            video.setOld(old);

                            String viewCountString = videoRenderer.getJSONObject("viewCountText").getString("simpleText");

                            long viewCount = Long.valueOf(CharMatcher.inRange('0','9').retainFrom(viewCountString));
                            video.setViewCount(viewCount);

                            // Парсим описание в конце, потому что его иногда не бывает
                            String description = videoRenderer.getJSONObject("descriptionSnippet").getString(
                                    "simpleText");
                            video.setDescription(description);
                        }
                        catch (JSONException e)
                        {
                            MyLogger.logWarn("JSON error.");
                            // Выводим последнее видео на котором выпал exception,
                            // чтобы потом по логам чекнуть где трабла
                            MyLogger.logWarn(feed.getVideos().get(feed.getVideos().size()-1).toString());
                        }
                    }
                }
            }

            return feed;
        }
        catch (Exception e)
        {
            Logger.logMsg(Logger.ERROR, "Ошибка парсинга");

            e.printStackTrace();

            return null;
        }
    }
}
