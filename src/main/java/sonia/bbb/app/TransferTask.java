/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sonia.bbb.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import sonia.commons.bigbluebutton.client.Attendee;
import sonia.commons.bigbluebutton.client.BbbClient;
import sonia.commons.bigbluebutton.client.Meeting;

/**
 *
 * @author th
 */
public class TransferTask extends TimerTask
{

  private final static SimpleDateFormat FORMAT = new SimpleDateFormat(
    "YYYY-MM-dd");

  private static final HashMap<String, String> users = new HashMap();

  private static String currentDate;

  static
  {
    currentDate = FORMAT.format(new Date());
    System.out.println(currentDate);
  }

  private final BbbClient client;

  private final String influxDbUrl;

  private final String hostname;

  public TransferTask(BbbClient client, String influxDbUrl, String hostname)
  {
    this.client = client;
    this.influxDbUrl = influxDbUrl;
    this.hostname = hostname;
  }

  @Override
  public void run()
  {
    System.out.println("\nRun task for host = " + hostname + " (" + new Date()
      + ")");

    try
    {
      String now = FORMAT.format(new Date());

      if (!currentDate.equalsIgnoreCase(now))
      {
        System.out.println("*** new day - clearing users hashset");
        users.clear();
        currentDate = new String(now);
      }

      List<Meeting> meetings = client.getMeetings();

      String message = "meetings,host=" + hostname + " value=" + meetings.size()
        + "\n";

      int numberOfUsers = 0;
      int numberOfAudioStreams = 0;
      int numberOfVideoStreams = 0;
      int numberOfListenOnlyStreams = 0;

      for (Meeting meeting : meetings)
      {
        numberOfUsers += meeting.getParticipantCount();
        for (Attendee attendee : meeting.getAttendees())
        {
          numberOfAudioStreams += (attendee.hasJoinedVoice() ? 1 : 0);
          numberOfVideoStreams += (attendee.hasVideo() ? 1 : 0);
          numberOfListenOnlyStreams += (attendee.isListeningOnly() ? 1 : 0);

          users.put(attendee.getFullName().toLowerCase(), attendee.
            getClientType());
        }
      }

      message += "users,host=" + hostname + " value=" + numberOfUsers + "\n";
      message += "audio,host=" + hostname + " value=" + numberOfAudioStreams
        + "\n";
      message += "video,host=" + hostname + " value=" + numberOfVideoStreams
        + "\n";
      message += "listenOnly,host=" + hostname + " value="
        + numberOfListenOnlyStreams + "\n";
      message += "unique_users,host=" + hostname + " value="
        + users.size() + "\n";

      ///////
      System.out.print(message);

      HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(20))
        .build();

      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(influxDbUrl))
        .timeout(Duration.ofMinutes(1))
        .header("Content-Type", "text/plain")
        .POST(BodyPublishers.ofString(message))
        .build();

      HttpResponse<String> response;

      response = httpClient.send(request, BodyHandlers.ofString());
      System.out.println("response code : " + response.statusCode());
    }
    catch (Exception ex)
    {
      Logger.getLogger(TransferTask.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    System.gc();
  }
}
