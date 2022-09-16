
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;


import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    //to instantiate an object in our implementation we have to pass a resttemplate object to the constructor . the protected keyword tells us that the constructor can only be used only by child classes or classes in
    //the same pakage the latter will come in handy for us to creat an ibject from the factor class later 
    this.restTemplate = restTemplate;
  }
  
  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF




  

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        //this method has to utilize this method for creating the api call url , make the api call and return the results as a list 
    // String tiingoRestURL = buildUri(symbol, from, to);
    // TiingoCandle[] tiingoCandleArray = restTemplate.getForObject(tiingoRestURL, TiingoCandle[].class);

    
    if(from.compareTo(to)>=0){
      throw new RuntimeException();
    }
    String url = buildUri(symbol, from, to);
    TiingoCandle[] stocksStartToEndDate = restTemplate.getForObject(url, TiingoCandle[].class );
    List<Candle> stockloList = Arrays.asList(stocksStartToEndDate);
    return stockloList;
  }

  

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = "f41c580abd49a7dbd51f05b969d499eba0ca7f74";
       String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    String url = uriTemplate.replace("$APIKEY",token).replace("$SYMBOL",symbol)
    .replace("$ENDDATE",endDate.toString()).replace("$STARTDATE", startDate.toString());
      return uriTemplate;
  }
   private Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }

  private Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }

  private AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {
    double total_num_years = DAYS.between(trade.getPurchaseDate(), endDate) / 365.2422;
    double totalReturns = (sellPrice - buyPrice) / buyPrice;
    double annualized_returns = Math.pow((1.0 + totalReturns), (1.0 / total_num_years)) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
  }

  //we used @override annotation to specify that we are implimenting a method we had inherited . now this is enough to satisfy the interface implementation but
  //we will have to imprt the logic to calculate the annualized return value
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate){
    // List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
    // for (PortfolioTrade obj : portfolioTrades) {
    //   List<Candle> candleList = new ArrayList<>();
      
    //   TiingoCandle candleObj = (TiingoCandle) candleList.get(candleList.size() - 1);
    //   double buyPrice = candleList.get(0).getOpen();
    //   double sellPrice = candleObj.getClose();
    //   double totalReturn = (sellPrice - buyPrice) / buyPrice; 
    //   double totalNoOfYears = ChronoUnit.DAYS.between(obj.getPurchaseDate(),endDate) / 365.2422;
    //   double annualizedReturn = Math.pow((1 + totalReturn),(1.0 / totalNoOfYears)) - 1;
    //   AnnualizedReturn anRet =  new AnnualizedReturn(obj.getSymbol(),annualizedReturn,totalReturn);
    //   annualizedReturns.add(anRet);
    // }
    // Collections.sort(annualizedReturns,getComparator());
    // return annualizedReturns;
    AnnualizedReturn annualizedReturn;
    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
    for(int i=0 ; i<portfolioTrades.size() ; i++){
      //we are iterting thorught the list of portfolio trades we are given and for each we are calling the function getannualizedreturn 
      //which calculates and returns for us an annualizedReturn object . all these are stored to alsit annualizedReturns which we sort in descending order based
      // on the value given by calling getAnnualizedReturn method
      annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i),endDate);
      annualizedReturns.add(annualizedReturn);
    }
    Comparator<AnnualizedReturn>SortByAnnReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    Collections.sort(annualizedReturns,SortByAnnReturn);
    return annualizedReturns;
}
// private Comparator<AnnualizedReturn> getComparator() {
//   return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
// }

  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endLocalDate) {
    AnnualizedReturn annualizedReturn;
    String symbol = trade.getSymbol();
    LocalDate startlLocalDate = trade.getPurchaseDate();

    try{
      List<Candle> stocksStartToEndDate;
      stocksStartToEndDate = getStockQuote(symbol, startlLocalDate, endLocalDate);
      Candle stockStartDate = stocksStartToEndDate.get(0);
      Candle stockLatest = stocksStartToEndDate.get(stocksStartToEndDate.size()-1);

      Double buyPrice = stockStartDate.getOpen();
      Double sellPrice = stockLatest.getClose();

      Double totalReturn = (sellPrice - buyPrice)/buyPrice;

      Double numYears = (double)ChronoUnit.DAYS.between(startlLocalDate, endLocalDate)/365;

      Double annualizedReturns = Math.pow((1+totalReturn),(1/numYears))-1;
      annualizedReturn = new AnnualizedReturn(symbol, annualizedReturns, totalReturn);
    }catch(JsonProcessingException e){
      annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }
    return annualizedReturn;
  }








  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }




}
