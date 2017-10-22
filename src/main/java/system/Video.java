package system;

import java.awt.image.BufferedImage;

public class Video
{
    /* Id видосека - можно использовать для получения ссылки для просмотра видео */
    private String id;
    private String name;
    private String description;
    private String channel;
    private String imgUrl;
    private String old;
    private long viewCount;
    private BufferedImage image;

    @Override
    public String toString()
    {
        return "Video{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", channel='" + channel + '\'' +
               ", imgUrl='" + imgUrl + '\'' +
               ", old='" + old + '\'' +
               ", viewCount='" + viewCount + '\'' +
               ", image=" + image +
               '}';
    }

    public Video(String id, String name, String imgUrl)
    {
        this.id = id;
        this.name = name;
        this.imgUrl = imgUrl;
    }

    public BufferedImage getImage()
    {
        return image;
    }

    public void setImage(BufferedImage image)
    {
        this.image = image;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getImgUrl()
    {
        return imgUrl;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setImgUrl(String imgUrl)
    {
        this.imgUrl = imgUrl;
    }

    public String getOld()
    {
        return old;
    }

    public void setOld(String old)
    {
        this.old = old;
    }

    public long getViewCount()
    {
        return viewCount;
    }

    public void setViewCount(long viewCount)
    {
        this.viewCount = viewCount;
    }
}
