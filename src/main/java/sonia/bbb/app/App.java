package sonia.bbb.app;

import sonia.commons.bigbluebutton.client.Attendee;
import sonia.commons.bigbluebutton.client.BbbClient;
import sonia.commons.bigbluebutton.client.BbbClientFactory;
import sonia.commons.bigbluebutton.client.Meeting;

/**
 *
 * @author th
 */
public class App
{

  public static void main(String[] args) throws Exception
  {
    BbbClient client = BbbClientFactory.createClient(
      args[0], args[1]);

    for (Meeting m : client.getMeetings())
    {
      System.out.println("\n" + m.getMeetingName() + " (" + m.
        getParticipantCount() + ", " + m.getCreateDate() + ")" );

      for (Attendee a : m.getAttendees())
      {
        System.out.println("  - " + a.getFullName() + " (" + a.getRole()
          + ( a.hasJoinedVoice() ? ", AUDIO" : "" ) 
          + ( a.hasVideo() ? ", VIDEO" : "" ) + ")" );
      }
    }
  }
}
