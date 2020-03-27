/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sonia.bbb.app;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Date;
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
  private final BbbClient client;

  private final String influxDbUrl;

  private final String hostname;
  
  private static HttpClient httpClient = HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_1_1)
          .build();

  public TransferTask(BbbClient client, String influxDbUrl, String hostname)
  {
    this.client = client;
    this.influxDbUrl = influxDbUrl;
    this.hostname = hostname;
  }

  @Override
  public void run()
  {
    System.out.println("\nRun task for host = " + hostname + " (" + new Date() + ")");

    
    List<Meeting> meetings = client.getMeetings();
    
    String message = "meetings,host=" + hostname + " value=" + meetings.size() + "\n"; 

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
      }
    }
    
    message += "users,host=" + hostname + " value=" + numberOfUsers + "\n"; 
    message += "audio,host=" + hostname + " value=" + numberOfAudioStreams + "\n"; 
    message += "video,host=" + hostname + " value=" + numberOfVideoStreams + "\n"; 
    message += "listenOnly,host=" + hostname + " value=" + numberOfListenOnlyStreams + "\n"; 

    ///////
    
    System.out.print( message );
    
    HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(influxDbUrl))
              .timeout(Duration.ofMinutes(1))
              .header("Content-Type", "text/plain")
              .POST(BodyPublishers.ofString(message))
              .build();

    HttpResponse<String> response;

    try
    {
      response = httpClient.send(request, BodyHandlers.ofString());
      System.out.println( "response code : " + response.statusCode() );
    }
    catch (IOException ex)
    {
      Logger.getLogger(TransferTask.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (InterruptedException ex)
    {
      Logger.getLogger(TransferTask.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
