package sonia.commons.bigbluebutton.client;

import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author th
 */
@XmlRootElement
@ToString
public class Statistics
{
  private static final HashMap<String, String> users = new HashMap();

  Statistics(List<Meeting> meetings)
  {
    numberOfMeetings = meetings.size();

    for (Meeting meeting : meetings)
    {
      int participantCount = meeting.getParticipantCount();
      numberOfUsers += participantCount;
      largestConference = Math.max(largestConference, participantCount);
      for (Attendee attendee : meeting.getAttendees())
      {
        if ( attendee.hasVideo() )
        {
          numberOfVideoStreams += 1;
        }
        else if ( attendee.hasJoinedVoice() )
        {
          numberOfAudioStreams += 1;
        }
        else if ( attendee.isListeningOnly() )
        {
          numberOfListenOnlyStreams += 1;
        }
        else
        {
          numberOfViewerOnlyStreams += 1;
        }

        users.put(attendee.getFullName().toLowerCase(), attendee.
          getClientType());
      }
    }
  }

  public int getNumberOfUniqueUsers()
  {
    return users.size();
  }

  public static void clear()
  {
    users.clear();
  }

  public int getHealthCheck()
  {
    return numberOfUsers - numberOfVideoStreams - numberOfAudioStreams
      - numberOfListenOnlyStreams - numberOfViewerOnlyStreams;
  }

  @Getter
  private int numberOfMeetings;

  @Getter
  private int largestConference;

  @Getter
  private int numberOfUsers;

  @Getter
  private int numberOfAudioStreams;

  @Getter
  private int numberOfVideoStreams;

  @Getter
  private int numberOfListenOnlyStreams;

  @Getter
  private int numberOfViewerOnlyStreams;

}
