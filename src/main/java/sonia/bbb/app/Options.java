/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sonia.bbb.app;

import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Option;

/**
 *
 * @author th
 */
public class Options
{

  @Setter
  @Getter
  @Option( name="--help", aliases="-h", usage="Display this help", required = false )
  private boolean help = false;
  
  @Getter
  @Option( name="--secret", usage="BBB server secret", required = true )
  private String secret;
  
  @Getter
  @Option( name="--api-url", usage="BBB server api url inclusive trailing slash", required = true )
  private String apiUrl;
  
  @Getter
  @Option( name="--summary", aliases="-s", usage="Print summary in JSON format", required = false )
  private boolean summary = false;
  
}
