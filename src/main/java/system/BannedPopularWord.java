package system;

public class BannedPopularWord
{
    private String word;

    public BannedPopularWord(String word)
    {
        this.word = word;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof BannedPopularWord && word.equalsIgnoreCase(((BannedPopularWord) obj).word);
    }
}
