package system.parser.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoRenderer
{
    private String videoId;
    private Title title;
    private Thumbnail thumbnail;
    private ShortBylineText shortBylineText;
    private PublishedTimeText publishedTimeText;
    private ViewCountText viewCountText;
    private DescriptionSnippet descriptionSnippet;
}
