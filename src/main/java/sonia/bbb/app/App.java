package sonia.bbb.app;

import java.util.List;
import sonia.commons.bigbluebutton.client.Attendee;
import sonia.commons.bigbluebutton.client.BbbClient;
import sonia.commons.bigbluebutton.client.BbbClientFactory;
import sonia.commons.bigbluebutton.client.Meeting;
import sonia.commons.bigbluebutton.client.Metadata;

/**
 *
 * @author Thosten Ludewig <t.ludewig@ostfalia.de>
 */
public class App
{

  /*
    arg 0 : Full BigBlueButton-API URL include the last '/' slash
         e.g.  https://bbb.sample.org/bigbluebutton/api/ 
  
    arg 1 : the server secret
  
    optional args 2:  -s  Print summary only.
   */
  public static void main(String[] args) throws Exception
  {
    BbbClient client = BbbClientFactory.createClient(
      args[0], args[1]);

    if (args.length == 3 && "-s".equalsIgnoreCase(args[2]))
    {
      List<Meeting> meetings = client.getMeetings();
      System.out.println( "meetings: " + meetings.size() );

      int numberOfUsers = 0;
      int numberOfAudioStreams = 0;
      int numberOfVideoStreams = 0;
      int numberOfListenOnlyStreams = 0;
      
      for (Meeting meeting : meetings )
      {
        numberOfUsers += meeting.getParticipantCount();
        for( Attendee attendee : meeting.getAttendees() )
        {
          numberOfAudioStreams += ( attendee.hasJoinedVoice() ? 1 : 0 );
          numberOfVideoStreams += ( attendee.hasVideo() ? 1 : 0 );
          numberOfListenOnlyStreams += ( attendee.isListeningOnly() ? 1 : 0 );
        }
      }
      System.out.println( "users: " + numberOfUsers );
      System.out.println( "audio: " + numberOfAudioStreams );
      System.out.println( "video: " + numberOfVideoStreams );
      System.out.println( "listen only: " + numberOfListenOnlyStreams );
    }
    else
    {
      for (Meeting meeting : client.getMeetings())
      {
        System.out.println("\n" + meeting.getMeetingName() + " (" + meeting.
          getParticipantCount() + ", " + meeting.getCreateDate() + ")");

        Metadata metadata = meeting.getMetadata();
        if (metadata != null)
        {
          System.out.println("  + " + metadata);
        }

        for (Attendee attendee : meeting.getAttendees())
        {
          System.out.println("  - " + attendee.getFullName()
            + " (" + attendee.getRole()
            + (attendee.hasJoinedVoice() ? ", AUDIO" : "")
            + (attendee.hasVideo() ? ", VIDEO" : "") + ")");
        }
      }
    }
  }
}
