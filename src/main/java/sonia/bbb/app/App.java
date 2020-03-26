package sonia.bbb.app;

import java.util.List;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
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
  public static void main(String[] args) throws Exception
  {
    Options options = new Options();
    CmdLineParser parser = new CmdLineParser(options);

    try
    {
      parser.parseArgument(args);
    }
    catch (CmdLineException e)
    {
      System.out.println(e.getMessage());
      options.setHelp(true);
    }

    if (options.isHelp())
    {
      System.out.println( "BBBgetMeetings usage:");
      parser.printUsage(System.out);
      System.exit(0);
    }

    BbbClient client = BbbClientFactory.createClient(
      options.getApiUrl(), options.getSecret());

    if (options.isSummary())
    {
      List<Meeting> meetings = client.getMeetings();
      System.out.println("{\n  \"meetings\":" + meetings.size());

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
      System.out.println("  \"users\":" + numberOfUsers);
      System.out.println("  \"audio\":" + numberOfAudioStreams);
      System.out.println("  \"video\":" + numberOfVideoStreams);
      System.out.println("  \"listenOnly\":" + numberOfListenOnlyStreams + "\n}");
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
