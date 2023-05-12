
package com.stock;


import com.stock.dto.*;
import com.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.stock.portfolio.PortfolioManager;
import com.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
// import java.util.stream.Stream;
import com.stock.dto.*;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File tradesFile = resolveFileFromResources(args[0]);
    ObjectMapper om=getObjectMapper();
    List<String>symbols = new ArrayList<String>();
    PortfolioTrade[] trades = om.readValue(tradesFile, PortfolioTrade[].class);
    for(PortfolioTrade trade : trades){
      symbols.add(trade.getSymbol());
    }
    return symbols;
  }

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>
  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

  public static List<TotalReturnsDto>mainReadQoutesHelper(String[] args , List<PortfolioTrade>trades)throws IOException,URISyntaxException{
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto>tests = new ArrayList<TotalReturnsDto>();
    for(PortfolioTrade t : trades){
      String uri = "https://api.tiingo.com/tiingo/daily/" 
      + t.getSymbol() + "/prices?startDate=" + t.getPurchaseDate() + "&endDate=" + args[1] 
      + "&token=" + "f41c580abd49a7dbd51f05b969d499eba0ca7f74";
      TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);
      if(results!=null){
        tests.add(new TotalReturnsDto(t.getSymbol(), results[results.length-1].getClose()));
      }
    }
    return tests;

  }
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    //solution :-
    /*
     * first deserialise
     *      portfoliotrade mei daalna ha 
     *          list portfolio trde ki and usme daalna ha 
     * now list mei saari cheeze aa gyi
     *
     * then traverse in the list 
     *      getforobject (url,string.class)
     *      now again deserialise
    */
    File reader = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    //  RestTemplate restTemplate = new RestTemplate();
    // return new RestTemplate().getForObject("https://api.tiingo.com/tiingo/daily/" + symbol.getSymbol() + "/prices?startDate=" + symbol.getPurchaseDate() + "&endDate=" + args[1] + "&token=" + "f41c580abd49a7dbd51f05b969d499eba0ca7f74", PortfolioTrade[].class );
    //getforentity() -> will return your entity . from entity you will get the response 
    //postforentity()
    //all the http verbs are present for the rest template 
    
    PortfolioTrade[] portfolios = om.readValue(reader, PortfolioTrade[].class);
    
    List<PortfolioTrade> list = new ArrayList<PortfolioTrade>();
    for (PortfolioTrade portfolio : portfolios){
      list.add(portfolio);
    }

    TreeMap <Double, String> CollectionOfValues = new TreeMap<>();

    for (PortfolioTrade symbol : list) {
      String result = restTemplate.getForObject( "https://api.tiingo.com/tiingo/daily/" 
          + symbol.getSymbol() + "/prices?startDate=" + symbol.getPurchaseDate() + "&endDate=" + args[1] 
          + "&token=" + "f41c580abd49a7dbd51f05b969d499eba0ca7f74",String.class);
      List<TiingoCandle> collection = om.readValue(result, new TypeReference<ArrayList<TiingoCandle>>(){});
      CollectionOfValues.put(collection.get(collection.size() - 1).getClose(), symbol.getSymbol());
    }

    return CollectionOfValues.values().stream().collect(Collectors.toList());

  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    ObjectMapper om = getObjectMapper();
     PortfolioTrade[] pf = om.readValue(resolveFileFromResources(filename), PortfolioTrade[].class);
     List<PortfolioTrade> ls = Arrays.asList(pf);
     return ls;
 }

  


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
 }


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    String tiingoRestURL = prepareUrl(trade, endDate, token);
    TiingoCandle[] tiingoCandleArray =
        restTemplate.getForObject(tiingoRestURL, TiingoCandle[].class);
    return Arrays.stream(tiingoCandleArray).collect(Collectors.toList());
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
    //  return Collections.emptyList();
      List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
      List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
      LocalDate localDate = LocalDate.parse(args[1]);
      for (PortfolioTrade portfolioTrade : portfolioTrades) {
        List<Candle> candles = fetchCandles(portfolioTrade, localDate, getToken());
        AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(localDate, portfolioTrade, getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles));
        annualizedReturns.add(annualizedReturn);
      }
      return annualizedReturns.stream()
          .sorted((a1, a2) -> Double.compare(a2.getAnnualizedReturn(), a1.getAnnualizedReturn()))
          .collect(Collectors.toList());
  }


  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        double total_num_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.2422;
        double totalReturns = (sellPrice - buyPrice) / buyPrice;
        double annualized_returns = Math.pow((1.0 + totalReturns), (1.0 / total_num_years)) - 1;
        return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
  }



  public static List<String> debugOutputs() {
 
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/projectworks1225-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@2f9f7dcf";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";
 
 
   return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
       toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
       lineNumberFromTestFileInStackTrace});
 }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
        String file = args[0];
        LocalDate endDate = LocalDate.parse(args[1]);
        String contents = readFileAsString(file);
        ObjectMapper objectMapper = getObjectMapper();
        // PortfolioManager portfolioManager =
        //     PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
        // List<PortfolioTrade> portfolioTrades =
        //     objectMapper.readValue(contents, new TypeReference<List<PortfolioTrade>>() {});
        PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
        return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);

  }
  private static String readFileAsString(String file) {
    return null;
  }
  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    // List<String> l = new ArrayList(Arrays.asList("one", "two"));
    //  Stream<String> sl = l.stream();
    //  l.add("three");
    //  String s = sl.collect(Collectors.joining(" "));
    //  System.out.println(s);
    // printJsonObject(mainReadQuotes(args));
    // printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
    
  }

  public static String getToken() {
      return "f41c580abd49a7dbd51f05b969d499eba0ca7f74";

  }
  
}

