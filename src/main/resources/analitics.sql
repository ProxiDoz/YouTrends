# Count hours in trends for video on specific channel
SELECT
  channel,
  COUNT(channel) AS countHours
FROM video
WHERE date BETWEEN '2018-02-11 00:00:00' AND now()
GROUP BY channel
ORDER BY countHours DESC;

# Count videos in trends on specific channel
SELECT
  channel,
  COUNT(channel) count
FROM (
       SELECT DISTINCT
         videoid,
         channel
       FROM video
       WHERE date BETWEEN '2018-02-11 00:00:00' AND now()
     ) AS uniqueVideosOnChannels
GROUP BY channel
ORDER BY count DESC;