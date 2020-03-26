package sonia.bbb.app;

import sonia.commons.bigbluebutton.client.Attendee;
import sonia.commons.bigbluebutton.client.BbbClient;
import sonia.commons.bigbluebutton.client.BbbClientFactory;
import sonia.commons.bigbluebutton.client.Meeting;
import sonia.commons.bigbluebutton.client.Metadata;

/**
 *
 * @author th
 */
public class App
{

  /*
    arg 0 : Full BigBlueButton-API URL include the last '/' slash
         e.g.  https://bbb.sample.org/bigbluebutton/api/ 
  
    arg 1 : the server secret
   */
  public static void main(String[] args) throws Exception
  {
    BbbClient client = BbbClientFactory.createClient(
      args[0], args[1]);

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
