
package com.stock.portfolio;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {



  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
    return new PortfolioManagerImpl(restTemplate);
    // return getPortfolioManager(restTemplate);
  }
  




}
