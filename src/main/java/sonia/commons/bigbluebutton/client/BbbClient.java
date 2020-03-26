package sonia.commons.bigbluebutton.client;

import java.util.List;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author th
 */
public class BbbClient {
  
  private final int apiUrlLength;
  private final String secret;
  private final WebTarget target;
  
  BbbClient( WebTarget target, int apiUrlLength, String secret )
  {
    this.target = target;
    this.apiUrlLength = apiUrlLength;
    this.secret = secret;
  }
  
  private WebTarget appendChecksum( WebTarget target )
  {
    String uri = target.getUri().toString().substring(apiUrlLength);
    return target.queryParam("checksum", DigestUtils.sha1Hex( uri + secret ));
  }
  
  public List<Meeting> getMeetings()
  {
    MeetingsResponse response = appendChecksum(target.path("getMeetings"))
            .request().accept(MediaType.APPLICATION_XML).get(MeetingsResponse.class);
        
    return response.getMeetings();
  }
}
