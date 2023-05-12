
package com.stock.portfolio;

import com.stock.dto.AnnualizedReturn;
import com.stock.dto.PortfolioTrade;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioManager {


  //CHECKSTYLE:OFF


  List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
                                                   LocalDate endDate)
  ;
}

