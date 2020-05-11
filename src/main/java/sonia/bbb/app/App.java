package sonia.bbb.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import sonia.commons.bigbluebutton.client.Attendee;
import sonia.commons.bigbluebutton.client.BbbClient;
import sonia.commons.bigbluebutton.client.BbbClientFactory;
import sonia.commons.bigbluebutton.client.Meeting;
import sonia.commons.bigbluebutton.client.MeetingMetadata;
import sonia.commons.bigbluebutton.client.MeetingResponse;
import sonia.commons.bigbluebutton.client.Recording;
import sonia.commons.bigbluebutton.client.Statistics;

/**
 *
 * @author Thosten Ludewig <t.ludewig@ostfalia.de>
 */
public class App
{
  public static Options options;
  
  public static void main(String[] args) throws Exception
  {
    options = new Options();
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
      System.out.println("BBBgetMeetings usage:");
      parser.printUsage(System.out);
      System.exit(0);
    }

    BbbClient client = BbbClientFactory.createClient(
      options.getApiUrl(), options.getSecret());

    if (options.isRecordings())
    {
      List<Recording> recordings = client.getRecordings();

      int i = 1;
      for (Recording recording : recordings)
      {
        System.out.println("\n" + i + ". " + recording.getMetadata().
          getMeetingName());
        System.out.println("  start time=" + recording.getStartTime() + ", "
          + new Date(recording.getStartTime()));
        System.out.println("  end time=" + recording.getEndTime() + ", "
          + new Date(recording.getEndTime()));
        System.out.println("  recordID=" + recording.getRecordID());
        System.out.println("  meetingID=" + recording.getMeetingID());
        System.out.println("  participants=" + recording.getParticipants());
        System.out.println("  rawSize=" + recording.getRawSize());
        System.out.println("  size=" + recording.getRecordingFormats().get(0).
          getSize());
        System.out.println("  processingTime="
          + recording.getRecordingFormats().get(0).getProcessingTime());
        System.out.println("  url=" + recording.getRecordingFormats().get(0).
          getUrl());
        i++;
        MeetingResponse meeting = client.
          getMeetingInfo(recording.getMeetingID());
        if (meeting.found())
        {
          System.out.println("  - MEETING FOUND");
          System.out.println("    create date=" + meeting.getCreateDate());
          System.out.println("    start time=" + meeting.getStartTime() + ", "
            + new Date(meeting.getStartTime()));
          System.out.println("    end time=" + meeting.getEndTime() + ", "
            + new Date(meeting.getEndTime()));
          System.out.println("    attendees=" + meeting.getAttendees().size());
        }
      }
    }
    else if (options.isEnableInfluxDb())
    {
      TransferTask task = new TransferTask(client, options.getInfluxDbUrl(),
        options.getHostname());
      Timer timer = new Timer();
      System.out.println("Running task every " + options.getInterval()
        + " seconds.");
      timer.scheduleAtFixedRate(task, 0, options.getInterval() * 1000);
    }
    else if (options.isSummary())
    {
      
      Statistics statistics = client.getStatistics();
 
      if (options.getHealthThreshold() == Integer.MAX_VALUE)
      {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writerWithDefaultPrettyPrinter().writeValue( writer, statistics);
        System.out.println(writer.getBuffer().toString());
      }
      else
      {
        System.out.println((options.getHealthThreshold() - Math.abs(statistics.getHealthCheck())
          <= 0) ? "true" : "false");
      }
    }
    else
    {
      for (Meeting meeting : client.getMeetings())
      {
        System.out.println("\n" + meeting.getMeetingName() + " (" + meeting.
          getParticipantCount() + ", " + meeting.getCreateDate() + ")");

        MeetingMetadata metadata = meeting.getMetadata();
        if (metadata != null)
        {
          System.out.println("  + " + metadata);
        }

        for (Attendee attendee : meeting.getAttendees())
        {
          System.out.println("  - " + attendee.getFullName() + " / " + attendee.
            getClientType()
            + " (" + attendee.getRole()
            + (attendee.hasJoinedVoice() ? ", AUDIO" : "")
            +  (attendee.hasVideo() ? ", VIDEO" : "") 
            + (attendee.isListeningOnly() ? ", LISTENER" : "") + ")");
        }
      }
    }
  }
}
